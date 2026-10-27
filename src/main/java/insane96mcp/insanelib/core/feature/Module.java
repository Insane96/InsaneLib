package insane96mcp.insanelib.core.feature;

import insane96mcp.insanelib.InsaneLib;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.modscan.ModAnnotation;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforgespi.language.ModFileScanData;
import org.objectweb.asm.Type;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class Module {
    static final HashMap<ResourceLocation, Module> modules = new LinkedHashMap<>();

    private boolean enabledByDefault;
    private boolean canBeDisabled;

    private final ResourceLocation id;
    private final String name;
    private String description = "";

    private final String modId;
    // Config type of the features without @ConfigType
    private final ModConfig.Type defaultConfigType;
    // The config types in which the module has features. Each one has its own "Enable" toggle in the config of that type
    private final Map<ModConfig.Type, TypeConfig> typeConfigs = new EnumMap<>(ModConfig.Type.class);

    private static final Map<Class<? extends Feature>, Feature> loadedFeatures = new LinkedHashMap<>();
    private final Map<Class<? extends Feature>, Feature> features = new LinkedHashMap<>();

    // Every @LoadFeature processed (by class name), checked in checkFeatureModules to report the ones not loaded
    private static final Map<String, FeatureAnnotation> featureAnnotations = new LinkedHashMap<>();
    // The config types for which each mod (by id) created an ILModConfig
    private static final Map<String, Set<ModConfig.Type>> loadedConfigTypes = new HashMap<>();
    // @LoadFeature (by class name) intentionally not loaded: required mods missing or CLIENT features on dedicated servers
    private static final Set<String> skippedFeatures = new HashSet<>();

    private record FeatureAnnotation(String modId, ModFileScanData.AnnotationData annotationData, @Nullable ModConfig.Type configType) {
        @Nullable
        String module() {
            String module = (String) this.annotationData.annotationData().get("module");
            return module == null || module.isEmpty() ? null : module;
        }
    }

    private static class TypeConfig {
        private final ModConfigSpec.Builder builder;
        @Nullable
        private ModConfigSpec.ConfigValue<Boolean> enabledConfig;
        private boolean enabled;

        private TypeConfig(ModConfigSpec.Builder builder, boolean enabled) {
            this.builder = builder;
            this.enabled = enabled;
        }
    }

    Module(ResourceLocation id, String name, ModConfig.Type defaultConfigType, IEventBus modEventBus) {
        this.id = id;
        this.name = name;
        this.enabledByDefault = true;
        this.canBeDisabled = true;
        this.modId = id.getNamespace();
        this.defaultConfigType = defaultConfigType;

        modEventBus.addListener(this::readConfig);
    }

    static final Object _lock = new Object();

    public static class Builder {
        private final Module module;

        private Builder(ResourceLocation id, String name, ModConfig.Type defaultConfigType, IEventBus modEventBus) {
            this.module = new Module(id, name, defaultConfigType, modEventBus);
        }

        /**
         * Creates a module. Modules can be created before the ILModConfigs, as each ILModConfig adds to its config the
         * modules of the mod having features of its type.
         * @param defaultConfigType the config type of the module's features without {@link ConfigType}
         */
        public static Builder create(ResourceLocation id, String name, ModConfig.Type defaultConfigType, IEventBus modEventBus) {
            return new Builder(id, name, defaultConfigType, modEventBus);
        }

        /**
         * @deprecated the config builder is now provided by the ILModConfig loading the features, use {@link #create(ResourceLocation, String, ModConfig.Type, IEventBus)}
         */
        @Deprecated
        public static Builder create(ResourceLocation id, String name, ModConfig.Type modConfigType, ModConfigSpec.Builder configBuilder, IEventBus modEventBus) {
            return create(id, name, modConfigType, modEventBus);
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
            module.enabledByDefault = enabledByDefault;
            return this;
        }

        public Module build() {
            synchronized (_lock) {
                Module.modules.putIfAbsent(module.id, module);
            }

            return module;
        }
    }

    /**
     * @deprecated the config builder is now provided by the ILModConfig loading the features. Does nothing
     */
    @Deprecated
    public void setConfigBuilder(final ModConfigSpec.Builder builder) {

    }

    /**
     * Whether the module is enabled in the config of its default config type
     */
    public boolean isEnabled() {
        return this.isEnabled(this.defaultConfigType);
    }

    /**
     * Whether the module is enabled in the config of the given type
     */
    public boolean isEnabled(ModConfig.Type configType) {
        TypeConfig typeConfig = this.typeConfigs.get(configType);
        return typeConfig == null ? this.enabledByDefault : typeConfig.enabled;
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public ModConfig.Type getDefaultConfigType() {
        return this.defaultConfigType;
    }

    public Map<Class<? extends Feature>, Feature> getFeatures() {
        return this.features;
    }

    /**
     * The config builder of the given type, null if the module has no features of that type
     */
    @Nullable
    public ModConfigSpec.Builder getConfigBuilder(ModConfig.Type configType) {
        TypeConfig typeConfig = this.typeConfigs.get(configType);
        return typeConfig == null ? null : typeConfig.builder;
    }

    private void loadConfig(ModConfig.Type configType, ModConfigSpec.Builder builder) {
        TypeConfig typeConfig = new TypeConfig(builder, this.enabledByDefault);
        if (this.canBeDisabled) {
            if (!this.description.isEmpty()) {
                typeConfig.enabledConfig = builder.comment(this.description).define("Enable %s".formatted(this.name), this.enabledByDefault);
            }
            else {
                typeConfig.enabledConfig = builder.define("Enable %s".formatted(this.name), this.enabledByDefault);
            }
        }
        this.typeConfigs.put(configType, typeConfig);
    }

    public void readConfig(final ModConfigEvent event) {
        // SERVER configs get unloaded when the server stops; the values can no longer be read, so the last ones read are kept
        if (event instanceof ModConfigEvent.Unloading
                || !event.getConfig().getModId().equals(this.modId))
            return;
        ModConfig.Type configType = event.getConfig().getType();
        TypeConfig typeConfig = this.typeConfigs.get(configType);
        if (typeConfig == null)
            return;

        if (canBeDisabled)
            typeConfig.enabled = typeConfig.enabledConfig.get();
        else
            typeConfig.enabled = true;
        this.features.forEach((clazz, feature) -> {
            if (feature.getConfigType() == configType)
                feature.readConfig(event);
        });
        this.features.forEach((clazz, feature) -> {
            if (feature.getConfigType() == configType)
                feature.postReadConfig(event);
        });
    }

    private void pushConfig(ModConfigSpec.Builder builder) {
        if (this.canBeDisabled) {
            if (this.description.isEmpty()) {
                builder.push(this.getName());
            }
            else {
                builder.comment(this.description).push(this.getName());
            }
        }
    }

    private void popConfig(ModConfigSpec.Builder builder) {
        if (this.canBeDisabled) {
            builder.pop();
        }
    }

    private static final Type LOAD_FEATURE_TYPE = Type.getType(LoadFeature.class);
    private static final Type CONFIG_TYPE_TYPE = Type.getType(ConfigType.class);

    /**
     * Loads in the builder the features of the given config type of the mod's modules registered so far, and the
     * modules' "Enable" toggles and sections. Modules without features of the given type are not added to the config.
     */
    public static void loadFeatures(ModConfig.Type configType, String modId, ClassLoader classLoader, ModConfigSpec.Builder builder) {
        synchronized (_lock) {
            loadedConfigTypes.computeIfAbsent(modId, k -> EnumSet.noneOf(ModConfig.Type.class)).add(configType);
        }
        Set<Module> modulesToLoad = new LinkedHashSet<>();
        ModFileScanData modFileScanData = ModList.get().getModFileById(modId).getFile().getScanResult();
        Map<String, ModConfig.Type> featureConfigTypes = getFeatureConfigTypes(modFileScanData);
        modFileScanData.getAnnotations().stream()
                .filter(annotation -> LOAD_FEATURE_TYPE.equals(annotation.annotationType()))
                .sorted(Comparator.comparing(a -> a.clazz().getClassName()))
                .forEach(annotation -> {
                    try {
                        handleFeatureAnnotation(annotation, configType, modId, classLoader, featureConfigTypes.get(annotation.clazz().getClassName()), modulesToLoad);
                    }
                    catch (Exception e) {
                        throw new RuntimeException("Failed to load Module %s".formatted(annotation), e);
                    }
                });
        // The toggles are defined first, in the order the modules are registered, then the sections
        List<Module> modModules;
        synchronized (_lock) {
            modModules = getModModules(modId);
        }
        Set<Module> orderedModules = new LinkedHashSet<>();
        modModules.stream()
                .filter(modulesToLoad::contains)
                .forEach(orderedModules::add);
        orderedModules.addAll(modulesToLoad);
        orderedModules.forEach(m -> m.loadConfig(configType, builder));
        modulesToLoad.forEach(m -> {
            m.pushConfig(builder);
            m.getFeatures().forEach((clazz, feature) -> {
                if (feature.getConfigType() == configType)
                    feature.loadConfig();
            });
            m.popConfig(builder);
        });
    }

    /**
     * The config types specified with @ConfigType, by class name
     */
    private static Map<String, ModConfig.Type> getFeatureConfigTypes(ModFileScanData modFileScanData) {
        return modFileScanData.getAnnotations().stream()
                .filter(annotation -> CONFIG_TYPE_TYPE.equals(annotation.annotationType()))
                .collect(Collectors.toMap(a -> a.clazz().getClassName(),
                        a -> ModConfig.Type.valueOf(((ModAnnotation.EnumHolder) a.annotationData().get("value")).value()),
                        (a, b) -> a));
    }

    private static void handleFeatureAnnotation(ModFileScanData.AnnotationData annotationData,
                                                ModConfig.Type configType,
                                                String modId,
                                                ClassLoader classLoader,
                                                @Nullable ModConfig.Type featureConfigType,
                                                Set<Module> modulesToLoad) throws Exception {
        Map<String, Object> annotationDataMap = annotationData.annotationData();
        String className = annotationData.clazz().getClassName();
        FeatureAnnotation featureAnnotation = new FeatureAnnotation(modId, annotationData, featureConfigType);
        synchronized (_lock) {
            featureAnnotations.putIfAbsent(className, featureAnnotation);
        }

        // CLIENT features are skipped on dedicated servers also when the mod doesn't create the CLIENT config there
        if (featureConfigType == ModConfig.Type.CLIENT && FMLEnvironment.dist.isDedicatedServer()) {
            markSkipped(className);
            return;
        }
        if (featureConfigType != null && featureConfigType != configType)
            return;

        // Errors are not logged here as the mod might create more ILModConfigs or modules after this one, they're reported by checkFeatureModules
        Module module;
        synchronized (_lock) {
            module = resolveModule(featureAnnotation);
        }
        if (module == null)
            return;
        if (featureConfigType == null)
            featureConfigType = module.defaultConfigType;
        if (featureConfigType == ModConfig.Type.CLIENT && FMLEnvironment.dist.isDedicatedServer()) {
            markSkipped(className);
            return;
        }
        if (featureConfigType != configType)
            return;

        Type type = annotationData.clazz();
        Class<?> clazz = Class.forName(type.getClassName(), false, classLoader);
        if (!Feature.class.isAssignableFrom(clazz))
            throw new RuntimeException("Class %s is annotated with @LoadFeature but does not extend Feature".formatted(type.getClassName()));
        @SuppressWarnings("unchecked")
        Class<? extends Feature> featureClazz = (Class<? extends Feature>) clazz;

        if (!areRequiredModsLoaded(annotationDataMap, type.getClassName())) {
            markSkipped(className);
            return;
        }

        boolean enabledByDefault = (Boolean) annotationDataMap.getOrDefault("enabledByDefault", true);
        boolean canBeDisabled = (Boolean) annotationDataMap.getOrDefault("canBeDisabled", true);

        InsaneLib.LOGGER.info("Found ({}) InsaneLib Feature class {}", configType, type.getClassName());

        Feature feature = instantiateFeature(clazz, module, configType, enabledByDefault, canBeDisabled);
        module.getFeatures().put(featureClazz, feature);
        Module.getAllLoadedFeatures().put(featureClazz, feature);
        modulesToLoad.add(module);
    }

    /**
     * The module of the feature, or null if it doesn't exist (yet) or the feature doesn't specify one and the mod
     * doesn't have exactly one module. Must be called while holding _lock
     */
    @Nullable
    private static Module resolveModule(FeatureAnnotation featureAnnotation) {
        String moduleStr = featureAnnotation.module();
        if (moduleStr == null) {
            List<Module> modModules = getModModules(featureAnnotation.modId());
            return modModules.size() == 1 ? modModules.getFirst() : null;
        }
        return Module.modules.get(ResourceLocation.parse(moduleStr));
    }

    private static boolean areRequiredModsLoaded(Map<String, Object> annotationDataMap, String className) {
        Optional<String> missingMod = getMissingRequiredMod(annotationDataMap);
        missingMod.ifPresent(modId -> InsaneLib.LOGGER.info("Feature {} not loaded because {} is not present", className, modId));
        return missingMod.isEmpty();
    }

    private static Optional<String> getMissingRequiredMod(Map<String, Object> annotationDataMap) {
        if (!annotationDataMap.containsKey("requiresMods"))
            return Optional.empty();

        @SuppressWarnings("unchecked")
        List<String> requiredMods = (List<String>) annotationDataMap.get("requiresMods");
        return requiredMods.stream()
                .filter(modId -> !ModList.get().isLoaded(modId))
                .findFirst();
    }

    private static void markSkipped(String className) {
        synchronized (_lock) {
            skippedFeatures.add(className);
        }
    }

    /**
     * Whether the feature has intentionally not been loaded (its required mods are missing or it's a CLIENT feature on a dedicated server)
     */
    public static boolean isFeatureSkipped(Class<? extends Feature> featureClazz) {
        synchronized (_lock) {
            return skippedFeatures.contains(featureClazz.getName());
        }
    }

    /**
     * Whether the mod has registered at least one module
     */
    public static boolean hasModules(String modId) {
        synchronized (_lock) {
            return !getModModules(modId).isEmpty();
        }
    }

    private static List<Module> getModModules(String modId) {
        return Module.modules.values().stream()
                .filter(m -> m.getId().getNamespace().equals(modId))
                .toList();
    }

    /**
     * Reports the @LoadFeature not loaded because of a wrong {@code module} (one never registered, or none while the mod
     * registers more than one module), a config type for which the mod didn't create an ILModConfig, or a module
     * registered after the ILModConfig of the feature's config type was created. Features whose required mods are missing are ignored.
     * Also reports the modules without features, that don't appear in any config.<br>
     * A mod can create multiple ILModConfigs and while loading one the modules of the ones created later don't exist yet,
     * so the check can be done only once all the mods have been constructed.
     */
    public static void checkFeatureModules() {
        synchronized (_lock) {
            Set<String> loadedFeatureNames = loadedFeatures.keySet().stream().map(Class::getName).collect(Collectors.toSet());
            Set<Module> modulesWithFeatures = new HashSet<>();
            featureAnnotations.forEach((className, featureAnnotation) -> {
                Module module = resolveModule(featureAnnotation);
                List<Module> modModules = getModModules(featureAnnotation.modId());
                if (loadedFeatureNames.contains(className) || skippedFeatures.contains(className)) {
                    if (module != null)
                        modulesWithFeatures.add(module);
                    if (featureAnnotation.module() == null && modModules.size() > 1) {
                        Feature loadedFeature = loadedFeatures.entrySet().stream()
                                .filter(entry -> entry.getKey().getName().equals(className))
                                .map(Map.Entry::getValue)
                                .findFirst()
                                .orElse(null);
                        if (loadedFeature != null)
                            InsaneLib.LOGGER.warn("@LoadFeature on {} must specify 'module' — multiple modules registered for mod {}. The feature has been loaded in {} only because it was the sole module when the feature was processed, which depends on the order the ILModConfigs are created", className, featureAnnotation.modId(), loadedFeature.getModule().getId());
                    }
                    return;
                }
                if (module != null)
                    modulesWithFeatures.add(module);
                if (getMissingRequiredMod(featureAnnotation.annotationData().annotationData()).isPresent())
                    return;

                if (featureAnnotation.module() == null && modModules.isEmpty()) {
                    InsaneLib.LOGGER.warn("@LoadFeature on {} doesn't specify 'module' and no module is registered for mod {}. The feature has not been loaded", className, featureAnnotation.modId());
                    return;
                }
                if (featureAnnotation.module() == null && modModules.size() > 1) {
                    InsaneLib.LOGGER.warn("@LoadFeature on {} must specify 'module' — multiple modules registered for mod {}. The feature has not been loaded", className, featureAnnotation.modId());
                    return;
                }
                if (module == null) {
                    InsaneLib.LOGGER.warn("@LoadFeature on {} specifies module {}, but no module with that id has been registered. The feature has not been loaded", className, featureAnnotation.module());
                    return;
                }
                ModConfig.Type configType = featureAnnotation.configType() != null ? featureAnnotation.configType() : module.defaultConfigType;
                if (!loadedConfigTypes.getOrDefault(featureAnnotation.modId(), Set.of()).contains(configType))
                    InsaneLib.LOGGER.warn("Feature {} has config type {}, but mod {} didn't create an ILModConfig of that type. The feature has not been loaded", className, configType, featureAnnotation.modId());
                else
                    InsaneLib.LOGGER.warn("Feature {} has not been loaded because its module {} has been registered after the {} ILModConfig of mod {} was created. Create the modules before the ILModConfigs that load their features", className, module.getId(), configType, featureAnnotation.modId());
            });
            Module.modules.values().stream()
                    .filter(m -> !modulesWithFeatures.contains(m))
                    .forEach(m -> InsaneLib.LOGGER.warn("Module {} has no features, so it doesn't appear in any config", m.getId()));
            featureAnnotations.clear();
        }
    }

    private static Feature instantiateFeature(Class<?> clazz, Module module, ModConfig.Type configType, boolean enabledByDefault, boolean canBeDisabled) {
        try {
            Feature feature = (Feature) clazz.getDeclaredConstructor().newInstance();
            feature.setConfigType(configType);
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
