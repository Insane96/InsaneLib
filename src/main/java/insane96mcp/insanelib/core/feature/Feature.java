package insane96mcp.insanelib.core.feature;

import insane96mcp.insanelib.InsaneLib;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.core.feature.config.ConfigOption;
import insane96mcp.insanelib.core.feature.config.DifficultyBasedConfig;
import insane96mcp.insanelib.core.feature.config.MinMaxConfig;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForge;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class Feature {
    private String name;
    private String dataKeyPath = null;
    private String description;
    private ModConfigSpec.ConfigValue<Boolean> enabledConfig;
    private Module module;

    private boolean enabledByDefault;
    private boolean canBeDisabled;

    private boolean enabled;

    private static final Map<Class<?>, ConfigOption.ConfigOptionFactory> CONFIG_OPTION_FACTORIES = new LinkedHashMap<>();

    static {
        registerConfigType(Double.class, (builder, name, annotation, defaultValue) ->
                new ConfigOption.DoubleCOption(builder, name, annotation.description(), (double) defaultValue, annotation.min(), annotation.max()));
        registerConfigType(Integer.class, (builder, name, annotation, defaultValue) -> {
            double min = annotation.min() == -Double.MAX_VALUE ? Integer.MIN_VALUE : annotation.min();
            double max = annotation.max() == Double.MAX_VALUE ? Integer.MAX_VALUE : annotation.max();
            return new ConfigOption.IntCOption(builder, name, annotation.description(), (int) defaultValue, (int) min, (int) max);
        });
        registerConfigType(Boolean.class, (builder, name, annotation, defaultValue) ->
                new ConfigOption.BoolCOption(builder, name, annotation.description(), (boolean) defaultValue));
        registerConfigType(String.class, (builder, name, annotation, defaultValue) ->
                new ConfigOption.StringCOption(builder, name, annotation.description(), (String) defaultValue));
        //noinspection unchecked
        registerConfigType(List.class, (builder, name, annotation, defaultValue) ->
                new ConfigOption.StringListCOption(builder, name, annotation.description(), (List<String>) defaultValue));
        registerConfigType(MinMaxConfig.class, (builder, name, annotation, defaultValue) ->
                new MinMaxConfig.COption(builder, name, annotation.description(), (MinMaxConfig) defaultValue, annotation.min(), annotation.max()));
        registerConfigType(DifficultyBasedConfig.class, (builder, name, annotation, defaultValue) ->
                new DifficultyBasedConfig.COption(builder, name, annotation.description(), (DifficultyBasedConfig) defaultValue, annotation.min(), annotation.max()));
    }

    public static void registerConfigType(Class<?> type, ConfigOption.ConfigOptionFactory factory) {
        CONFIG_OPTION_FACTORIES.put(type, factory);
    }

    protected Feature() {}

    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        this.module = module;
        this.enabledByDefault = enabledByDefault;
        this.canBeDisabled = canBeDisabled;
        this.name = extractName();
        this.description = extractDescription();
        this.registerEvents();
    }

    private String extractName() {
        String name = this.getClass().getAnnotation(LoadFeature.class).name();
        if (!name.isBlank())
            return name;

        return fieldNameToConfigOption(this.getClass().getSimpleName()).replaceAll("(?i)feature", "").trim();
    }

    private String extractDescription() {
        return this.getClass().getAnnotation(LoadFeature.class).description();
    }

    /**
     * @return true if both the feature and the module are enabled
     */
    public boolean isEnabled() {
        return enabled && this.isModuleEnabled();
    }

    public boolean isModuleEnabled() {
        return this.module.isEnabled();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    public void setEnabledConfig(boolean enabled) {
        if (this.enabledConfig == null) {
            InsaneLib.LOGGER.warn("Could not set enabled {} feature. The config option is null", this.name);
            return;
        }
        this.enabledConfig.set(enabled);
    }

    public Module getModule() {
        return module;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ModConfigSpec.Builder getBuilder() {
        return this.module.configBuilder;
    }

    HashMap<Field, ConfigOption<?>> configOptions = new HashMap<>();

    public void loadConfigOptions() {
        try {
            for (Field field : this.getClass().getDeclaredFields()) {
                if (!field.isAnnotationPresent(Config.class))
                    continue;

                if (!Modifier.isStatic(field.getModifiers()))
                    throw new UnsupportedOperationException("Failed to load %s field. The field is not static".formatted(field));

                Config annotation = field.getAnnotation(Config.class);
                String name = annotation.name().isBlank() ? fieldNameToConfigOption(field.getName()) : annotation.name();
                Object defaultValue = field.get(null);

                ConfigOption.ConfigOptionFactory factory = findFactory(field.getType());
                if (factory != null) {
                    this.configOptions.put(field, factory.create(this.getBuilder(), name, annotation, defaultValue));
                } else {
                    this.configOptions.put(field, new ConfigOption.GenericOption(this.getBuilder(), name, annotation.description(), defaultValue));
                }
            }
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to load Feature '%s'".formatted(this.name), e);
        }
    }

    @Nullable
    private static ConfigOption.ConfigOptionFactory findFactory(Class<?> type) {
        // Exact match first
        ConfigOption.ConfigOptionFactory factory = CONFIG_OPTION_FACTORIES.get(type);
        if (factory != null)
            return factory;

        // Check assignability (for subclasses like custom MinMax extensions)
        for (Map.Entry<Class<?>, ConfigOption.ConfigOptionFactory> entry : CONFIG_OPTION_FACTORIES.entrySet()) {
            if (entry.getKey().isAssignableFrom(type))
                return entry.getValue();
        }

        // Enum special case
        if (type.isEnum()) {
            //noinspection unchecked,rawtypes
            return (builder, name, annotation, defaultValue) ->
                    new ConfigOption.EnumCOption(builder, name, annotation.description(), (Enum) defaultValue);
        }

        return null;
    }

    public final void loadConfig() {
        if (this.canBeDisabled) {
            if (!this.description.isEmpty())
                this.module.configBuilder.comment(getDescription());
            enabledConfig = this.module.configBuilder.define("Enable " + getName(), enabledByDefault);
        }
        else
            enabledConfig = null;
        this.pushConfig();
        this.loadConfigOptions();
        this.popConfig();
    }

    public void readConfig(final ModConfigEvent event) {
        if (canBeDisabled)
            this.enabled = enabledConfig.get();
        else
            this.enabled = true;

        readConfigOptions();
    }

    public void postReadConfig(final ModConfigEvent event) {

    }

    private void readConfigOptions() {
        if (this.configOptions.isEmpty())
            return;
        String curField = "";
        try {
            for(Field field : this.getClass().getDeclaredFields())
            {
                if (!field.isAnnotationPresent(Config.class)) {
                    continue;
                }

                curField = this.configOptions.get(field).toString();
                field.set(this, this.configOptions.get(field).get());
            }
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to set config option for %s".formatted(curField), e);
        }
    }

    public void setConfigOption(String configName, Object value) {
        ConfigOption<?> configOption = getConfigOption(configName);
        if (configOption == null) {
            InsaneLib.LOGGER.warn("Feature#setConfig failed as %s was not found".formatted(configName));
            return;
        }

        configOption.set(value);
    }

    @Nullable
    public ConfigOption<?> getConfigOption(String configName) {
        Optional<ConfigOption<?>> configOptionOptional = this.configOptions.values()
                .stream()
                .filter(configOption -> configOption.getName().equals(configName))
                .findFirst();
        return configOptionOptional.orElse(null);
    }

    public void pushConfig() {
        if (!description.isEmpty())
            this.module.configBuilder.comment(this.getDescription());

        this.module.configBuilder.push(this.getName());
    }

    protected void popConfig() {
        this.module.configBuilder.pop();
    }

    public void registerEvents() {
        for (Method method : this.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(SubscribeEvent.class))
                continue;

            NeoForge.EVENT_BUS.register(this);
            return;
        }
    }

    public static Feature get(Class<? extends Feature> feature) {
        return Module.getFeature(feature);
    }

    public static boolean isEnabled(Class<? extends Feature> feature) {
        return get(feature).isEnabled();
    }

    public static boolean isEnabled(String featureName) {
        return Module.getFeature(featureName)
                .map(Feature::isEnabled)
                .orElse(false);
    }

    public static String fieldNameToConfigOption(String camelCase) {
        camelCase = camelCase.replace('$', '.');
        String[] words = camelCase.split("(?<!^)(?=[A-Z])");
        String sentence = String.join(" ", words).toLowerCase();
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true; // capitalize the 1st char
        for (int i = 0; i < sentence.length(); i++) {
            char current = sentence.charAt(i);
            if (capitalizeNext && Character.isLetter(current)) {
                result.append(Character.toUpperCase(current));
                capitalizeNext = false;
            }
            else {
                result.append(current);
            }
            if (current == '.')
                capitalizeNext = true;
        }
        return result.toString();
    }

    public String getDataKeyPath() {
        if (this.dataKeyPath != null)
            return this.dataKeyPath;
        String replaced = this.name.replace(" ", "_");

        this.dataKeyPath = replaced.toLowerCase().replaceAll("[^a-z0-9/._-]", "");
        return dataKeyPath;
    }

    public ResourceLocation createDataKey(String key) {
        return ResourceLocation.fromNamespaceAndPath(this.module.getId().getNamespace(), this.getDataKeyPath() + "/" + key);
    }
}
