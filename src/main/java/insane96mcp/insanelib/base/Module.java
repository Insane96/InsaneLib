package insane96mcp.insanelib.base;

import insane96mcp.insanelib.base.config.LoadFeature;
import insane96mcp.insanelib.util.LogHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.objectweb.asm.Type;

import java.util.*;

public class Module {

    static final HashMap<ResourceLocation, Module> modules = new HashMap<>();

    private ForgeConfigSpec.ConfigValue<Boolean> enabledConfig;

    ForgeConfigSpec.Builder builder;

    private boolean enabled;
    private boolean canBeDisabled;

    private final ResourceLocation id;
    private final String name;
    private String description = "";

    private final List<Feature> features = new ArrayList<>();

    Module(String id, String name) {
        this.id = new ResourceLocation(id);
        this.name = name;
        this.enabled = true;
        this.canBeDisabled = true;

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::loadConfig);
    }

    static final Object _lock = new Object();

    public static class Builder {
        private final Module module;

        public Builder(String id, String name) {
            this.module = new Module(id, name);
        }

        public static Builder create(String id, String name) {
            return new Builder(id, name);
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
            if (module.canBeDisabled) {
                if (!module.description.equals("")) {
                    module.enabledConfig = module.builder.comment(module.description).define("Enable %s".formatted(module.name), module.enabled);
                }
                else {
                    module.enabledConfig = module.builder.define("Enable %s".formatted(module.id), module.enabled);
                }
            }
            else {
                module.enabledConfig = null;
            }

            synchronized (_lock) {
                Module.modules.putIfAbsent(module.id, module);
            }

            return module;
        }
    }

    public void setConfigBuilder(final ForgeConfigSpec.Builder builder) {
        if (this.builder == null)
            this.builder = builder;
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

    public List<Feature> getFeatures() {
        return this.features;
    }

    public void loadConfig(final ModConfigEvent event) {
        if (canBeDisabled)
            this.enabled = enabledConfig.get();
        else
            this.enabled = true;
        this.features.forEach(feature -> feature.readConfig(event));
    }

    public void pushConfig() {
        if (this.canBeDisabled) {
            if (this.description.equals("")) {
                this.builder.push(this.getName());
            }
            else {
                this.builder.comment(this.description).push(this.getName());
            }
        }
    }

    public void popConfig() {
        this.builder.pop();
    }

    private static final Type LOAD_FEATURE_TYPE = Type.getType(LoadFeature.class);

    public static void loadFeatures(String modId, ClassLoader classLoader, ForgeConfigSpec.Builder builder) {
        ArrayList<Module> moduleToLoad = new ArrayList<>();
        ModFileScanData modFileScanData = ModList.get().getModFileById(modId).getFile().getScanResult();
        modFileScanData.getAnnotations().stream()
                .filter(annotationData -> LOAD_FEATURE_TYPE.equals(annotationData.annotationType()))
                .sorted(Comparator.comparing(d -> d.getClass().getName()))
                .forEach(annotationData -> {
                    try {
                        Type type = annotationData.clazz();
                        Class<?> clazz = Class.forName(type.getClassName(), false, classLoader);
                        LogHelper.info("Found InsaneLib Feature class " + type.getClassName());

                        Map<String, Object> annotationDataMap = annotationData.annotationData();
                        String moduleString = (String) annotationDataMap.get("module");
                        ResourceLocation moduleId = new ResourceLocation(moduleString);
                        Module module = Module.modules.get(moduleId);
                        module.setConfigBuilder(builder);

                        boolean enabledByDefault = true;
                        if (annotationDataMap.containsKey("enabledByDefault"))
                            enabledByDefault = (Boolean) annotationDataMap.get("enabledByDefault");

                        boolean canBeDisabled = true;
                        if (annotationDataMap.containsKey("canBeDisabled"))
                            canBeDisabled = (Boolean) annotationDataMap.get("canBeDisabled");

                        Feature feature = (Feature) clazz.getDeclaredConstructor(Module.class, boolean.class, boolean.class).newInstance(module, enabledByDefault, canBeDisabled);
                        module.features.add(feature);
                        if (!moduleToLoad.contains(module))
                            moduleToLoad.add(module);
                    }
                    catch (Exception e) {
                        throw new RuntimeException("Failed to load Module %s".formatted(annotationData), e);
                    }
                });
        moduleToLoad.forEach(m -> {
            m.pushConfig();
            m.getFeatures().forEach(Feature::loadConfig);
            m.popConfig();
        });
    }
}
