package insane96mcp.insanelib.core.feature;

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

    Module(ResourceLocation id, String name, ModConfig.Type modConfigType, ModConfigSpec.Builder configBuilder, IEventBus modEventBus) {
        this.id = id;
        this.name = name;
        this.enabled = true;
        this.canBeDisabled = true;
        this.modId = id.getNamespace();
        this.modConfigType = modConfigType;
        this.configBuilder = configBuilder;

        modEventBus.addListener(this::readConfig);
    }

    static final Object _lock = new Object();

    public static class Builder {
        private final Module module;

        private Builder(ResourceLocation id, String name, ModConfig.Type modConfigType, ModConfigSpec.Builder configBuilder, IEventBus modEventBus) {
            this.module = new Module(id, name, modConfigType, configBuilder, modEventBus);
        }

        public static Builder create(ResourceLocation id, String name, ModConfig.Type modConfigType, ModConfigSpec.Builder configBuilder, IEventBus modEventBus) {
            return new Builder(id, name, modConfigType, configBuilder, modEventBus);
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
            if (!this.description.isEmpty()) {
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
                        handleFeatureAnnotation(annotation, modConfigType, modId, classLoader, modulesToLoad);
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
                                                String modId,
                                                ClassLoader classLoader,
                                                Set<Module> modulesToLoad) throws Exception {
        Map<String, Object> annotationDataMap = annotationData.annotationData();
        String moduleStr = (String) annotationDataMap.get("module");

        Module module;
        if (moduleStr == null || moduleStr.isEmpty()) {
            List<Module> modModules = Module.modules.values().stream()
                    .filter(m -> m.getId().getNamespace().equals(modId))
                    .toList();
            if (modModules.size() == 1) {
                module = modModules.getFirst();
            } else if (modModules.isEmpty()) {
                InsaneLib.LOGGER.warn("No module found for mod {}", modId);
                return;
            } else {
                InsaneLib.LOGGER.warn("@LoadFeature on {} must specify 'module' — multiple modules registered for mod {}", annotationData.clazz().getClassName(), modId);
                return;
            }
        } else {
            ResourceLocation moduleId = ResourceLocation.parse(moduleStr);
            module = Module.modules.get(moduleId);
            if (module == null) {
                InsaneLib.LOGGER.warn("No module found with ID {}", moduleId);
                return;
            }
        }
        if (module.modConfigType != modConfigType)
            return;

        Type type = annotationData.clazz();
        Class<?> clazz = Class.forName(type.getClassName(), false, classLoader);
        if (!Feature.class.isAssignableFrom(clazz))
            throw new RuntimeException("Class %s is annotated with @LoadFeature but does not extend Feature".formatted(type.getClassName()));
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

        @SuppressWarnings("unchecked")
        List<String> requiredMods = (List<String>) annotationDataMap.get("requiresMods");
        for (String modId : requiredMods) {
            if (!ModList.get().isLoaded(modId)) {
                InsaneLib.LOGGER.info("Feature {} not loaded because {} is not present", className, modId);
                return false;
            }
        }
        return true;
    }

    private static Feature instantiateFeature(Class<?> clazz, Module module, boolean enabledByDefault, boolean canBeDisabled) {
        try {
            Feature feature = (Feature) clazz.getDeclaredConstructor().newInstance();
            feature.init(module, enabledByDefault, canBeDisabled);
            return feature;
        }
        catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to instantiate Feature: " + clazz.getName(), e);
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
