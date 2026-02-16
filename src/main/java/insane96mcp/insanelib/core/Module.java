package insane96mcp.insanelib.core;

import insane96mcp.insanelib.InsaneLib;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforgespi.language.ModFileScanData;
import org.objectweb.asm.Type;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.util.*;

public class Module {
    static final HashMap<ResourceLocation, Module> modules = new HashMap<>();

    private ModConfigSpec.ConfigValue<Boolean> enabledConfig;

    ModConfigSpec.Builder configBuilder;

    private boolean enabled;
    private boolean canBeDisabled;

    private final ResourceLocation id;
    private final String name;
    private String description = "";

    private final String modId;
    private final ModConfig.Type modConfigType;

    private static final Map<Class<? extends Feature>, Feature> loadedFeatures = new HashMap<>();
    private final Map<Class<? extends Feature>, Feature> features = new HashMap<>();

    Module(String modId, String moduleId, String name, ModConfig.Type modConfigType, ModConfigSpec.Builder configBuilder, IEventBus modEventBus) {
        this.id = ResourceLocation.fromNamespaceAndPath(modId, moduleId);
        this.name = name;
        this.enabled = true;
        this.canBeDisabled = true;
        this.modId = modId;
        this.modConfigType = modConfigType;
        this.configBuilder = configBuilder;

        modEventBus.addListener(this::readConfig);
    }

    static final Object _lock = new Object();

    public static class Builder {
        private final Module module;

        private Builder(String modId, String id, String name, ModConfig.Type modConfigType, ModConfigSpec.Builder configBuilder, IEventBus modEventBus) {
            this.module = new Module(modId, id, name, modConfigType, configBuilder, modEventBus);
        }

        public static Builder create(String modId, String id, String name, ModConfig.Type modConfigType, ModConfigSpec.Builder configBuilder, IEventBus modEventBus)  {
            return new Builder(modId, id, name, modConfigType, configBuilder, modEventBus);
        }

        public static Builder create(String id, String name, ModConfig.Type modConfigType, ModConfigSpec.Builder configBuilder, IEventBus modEventBus)  {
            String[] split = id.split(":");
            if (split.length != 2)
                throw new IllegalArgumentException("id seems to not be a valid Resource Location. Must be modid:module_id");
            return new Builder(split[0], split[1], name, modConfigType, configBuilder, modEventBus);
        }

        public Builder setDescription(String description) {
            module.description = description;
            return this;
        }

        public Builder canBeDisabled(boolean canBeDisabled) {
            module.canBeDisabled = canBeDisabled;
            return this;
        }

        public Builder enabledByDefault(boolean enabledByDefault) {
            module.enabled = enabledByDefault;
            return this;
        }

        public Module build() {
            module.loadConfig();

            synchronized (_lock) {
                Module.modules.putIfAbsent(module.id, module);
            }

            return module;
        }
    }

    public void setConfigBuilder(final ModConfigSpec.Builder builder) {
        if (this.configBuilder == null)
            this.configBuilder = builder;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public Map<Class<? extends Feature>, Feature> getFeatures() {
        return this.features;
    }

    public void loadConfig() {
        if (this.canBeDisabled) {
            if (!this.description.equals("")) {
                this.enabledConfig = this.configBuilder.comment(this.description).define("Enable %s".formatted(this.name), this.enabled);
            }
            else {
                this.enabledConfig = this.configBuilder.define("Enable %s".formatted(this.name), this.enabled);
            }
        }
        else {
            this.enabledConfig = null;
        }
    }

    public void readConfig(final ModConfigEvent event) {
        if (event.getConfig().getType() != this.modConfigType
                || !event.getConfig().getModId().equals(this.modId))
            return;

        if (canBeDisabled)
            this.enabled = enabledConfig.get();
        else
            this.enabled = true;
        this.features.forEach((clazz, feature) -> feature.readConfig(event));
        this.features.forEach((clazz, feature) -> feature.postReadConfig(event));
    }

    public void pushConfig() {
        if (this.canBeDisabled) {
            if (this.description.isEmpty()) {
                this.configBuilder.push(this.getName());
            }
            else {
                this.configBuilder.comment(this.description).push(this.getName());
            }
        }
    }

    public void popConfig() {
        if (this.canBeDisabled) {
            this.configBuilder.pop();
        }
    }

    private static final Type LOAD_FEATURE_TYPE = Type.getType(LoadFeature.class);

    public static void loadFeatures(ModConfig.Type modConfigType, String modId, ClassLoader classLoader) {
        Set<Module> modulesToLoad = new HashSet<>();
        ModFileScanData modFileScanData = ModList.get().getModFileById(modId).getFile().getScanResult();
        modFileScanData.getAnnotations().stream()
                .filter(annotation -> LOAD_FEATURE_TYPE.equals(annotation.annotationType()))
                .sorted(Comparator.comparing(a -> a.clazz().getClassName()))
                .forEach(annotation -> {
                    try {
                        handleFeatureAnnotation(annotation, modConfigType, classLoader, modulesToLoad);
                    }
                    catch (Exception e) {
                        throw new RuntimeException("Failed to load Module %s".formatted(annotation), e);
                    }
                });
        modulesToLoad.forEach(m -> {
            m.pushConfig();
            m.getFeatures().forEach((clazz, feature) -> feature.loadConfig());
            m.popConfig();
        });
    }

    private static void handleFeatureAnnotation(ModFileScanData.AnnotationData annotationData,
                                                ModConfig.Type modConfigType,
                                                ClassLoader classLoader,
                                                Set<Module> modulesToLoad) throws Exception {
        Map<String, Object> annotationDataMap = annotationData.annotationData();
        ResourceLocation moduleId = ResourceLocation.parse((String) annotationDataMap.get("module"));

        Module module = Module.modules.get(moduleId);
        if (module == null) {
            InsaneLib.LOGGER.warn("No module found with ID {}", moduleId);
            return;
        }
        if (module.modConfigType != modConfigType)
            return;

        Type type = annotationData.clazz();
        Class<?> clazz = Class.forName(type.getClassName(), false, classLoader);
        @SuppressWarnings("unchecked")
        Class<? extends Feature> featureClazz = (Class<? extends Feature>) clazz;

        if (!areRequiredModsLoaded(annotationDataMap, type.getClassName()))
            return;

        boolean enabledByDefault = (Boolean) annotationDataMap.getOrDefault("enabledByDefault", true);
        boolean canBeDisabled = (Boolean) annotationDataMap.getOrDefault("canBeDisabled", true);

        InsaneLib.LOGGER.info("Found ({}) InsaneLib Feature class {}", modConfigType, type.getClassName());

        Feature feature = instantiateFeature(clazz, module, enabledByDefault, canBeDisabled);
        module.getFeatures().put(featureClazz, feature);
        Module.getAllLoadedFeatures().put(featureClazz, feature);
        modulesToLoad.add(module);
    }

    private static boolean areRequiredModsLoaded(Map<String, Object> annotationDataMap, String className) {
        if (!annotationDataMap.containsKey("requiresMods"))
            return true;

        List<String> requiredMods = (List<String>) annotationDataMap.get("requiresMods");
        for (String modId : requiredMods) {
            if (!ModList.get().isLoaded(modId)) {
                InsaneLib.LOGGER.info("Feature %s not loaded because %s is not present".formatted(className, modId));
                return false;
            }
        }
        return true;
    }

    private static Feature instantiateFeature(Class<?> clazz, Module module, boolean enabledByDefault, boolean canBeDisabled) {
        try {
            Constructor<?> ctor = clazz.getDeclaredConstructor(Module.class, boolean.class, boolean.class);
            return (Feature) ctor.newInstance(module, enabledByDefault, canBeDisabled);
        }
        catch (NoSuchMethodException e) {
            try {
                Feature feature = (Feature) clazz.getDeclaredConstructor().newInstance();
                feature.init(module, enabledByDefault, canBeDisabled);
                return feature;
            }
            catch (ReflectiveOperationException ex) {
                throw new RuntimeException("Failed to instantiate Feature (no valid constructor): " + clazz.getName(), ex);
            }
        }
        catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to instantiate Feature with full constructor: " + clazz.getName(), e);
        }
    }

    public static Map<Class<? extends Feature>, Feature> getAllLoadedFeatures() {
        return loadedFeatures;
    }

    @Nullable
    public static Feature getFeature(Class<? extends Feature> featureClazz) {
        return loadedFeatures.get(featureClazz);
    }

    public static Optional<Feature> getFeature(String name) {
        return loadedFeatures.values()
                .stream()
                .filter(feature -> feature.getName().equalsIgnoreCase(name))
                .findFirst();
    }
}
