package insane96mcp.insanelib.base;

import insane96mcp.insanelib.base.config.Blacklist;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.Difficulty;
import insane96mcp.insanelib.base.config.MinMax;
import insane96mcp.insanelib.data.IdTagMatcher;
import insane96mcp.insanelib.util.LogHelper;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class Feature {
    private final String name;
    private final String description;
    private ForgeConfigSpec.ConfigValue<Boolean> enabledConfig;
    private final Module module;

    private final boolean enabledByDefault;
    private final boolean canBeDisabled;

    private boolean enabled;

    public Feature(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        if (!this.getClass().isAnnotationPresent(Label.class))
            LogHelper.error("%s is missing the Label Annotation.".formatted(this.getClass().getName()));
        this.name = this.getClass().getAnnotation(Label.class).name();
        this.description = this.getClass().getAnnotation(Label.class).description();
        this.module = module;
        this.enabledByDefault = enabledByDefault;
        this.canBeDisabled = canBeDisabled;
        this.registerEvents();
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
            LogHelper.warn("Could not set enabled %s feature. The config option is null", this.name);
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

    public ForgeConfigSpec.Builder getBuilder() {
        return this.module.configBuilder;
    }

    HashMap<Field, ConfigOption<?>> configOptions = new HashMap<>();

    /**
     * Override to add custom config options
     */
    public void loadConfigOptions() {
        try {
            for (Field field : this.getClass().getDeclaredFields())
            {
                if (!field.isAnnotationPresent(Config.class))
                    continue;

                if (!field.isAnnotationPresent(Label.class)) {
                    LogHelper.error("%s config option is missing the Label Annotation.".formatted(field.getName()));
                    continue;
                }

                if (!Modifier.isStatic(field.getModifiers()))
                {
                    throw new UnsupportedOperationException("Failed to load %s field. The field is not static".formatted(field));
                }

                String name = field.getAnnotation(Label.class).name();
                String description = field.getAnnotation(Label.class).description();
                double min = field.getAnnotation(Config.class).min();
                double max = field.getAnnotation(Config.class).max();

                if (field.getType().isAssignableFrom(Double.class))
                {
                    double defaultValue = (double) field.get(null);
                    ConfigOption.DoubleOption doubleOption = new ConfigOption.DoubleOption(this.getBuilder(), name, description, defaultValue, min, max);
                    this.configOptions.put(field, doubleOption);
                }
                else if (field.getType().isAssignableFrom(Integer.class))
                {
                    int defaultValue = (int) field.get(null);
                    if (min == Double.MIN_VALUE) min = Integer.MIN_VALUE;
                    if (max == Double.MAX_VALUE) max = Integer.MAX_VALUE;
                    ConfigOption.IntOption intOption = new ConfigOption.IntOption(this.getBuilder(), name, description, defaultValue, (int) min, (int) max);
                    this.configOptions.put(field, intOption);
                }
                else if (field.getType().isAssignableFrom(List.class))
                {
                    List<String> defaultValue = (List<String>) field.get(null);
                    ConfigOption.StringListOption listOption = new ConfigOption.StringListOption(this.getBuilder(), name, description, defaultValue);
                    this.configOptions.put(field, listOption);
                }
                else if (field.getType().isEnum())
                {
                    Enum defaultValue = (Enum) field.get(null);
                    ConfigOption.EnumOption enumOption = new ConfigOption.EnumOption(this.getBuilder(), name, description, defaultValue);
                    this.configOptions.put(field, enumOption);
                }
                else if (field.getType().isAssignableFrom(MinMax.class))
                {
                    MinMax defaultValue = (MinMax) field.get(null);
                    MinMax.Config minMaxConfig = new MinMax.Config(this.getBuilder(), name, description, defaultValue, min, max);
                    this.configOptions.put(field, minMaxConfig);
                }
                else if (field.getType().isAssignableFrom(Difficulty.class))
                {
                    Difficulty defaultValue = (Difficulty) field.get(null);
                    Difficulty.Config difficultyConfig = new Difficulty.Config(this.getBuilder(), name, description, defaultValue, min, max);
                    this.configOptions.put(field, difficultyConfig);
                }
                else if (field.getType().isAssignableFrom(Blacklist.class))
                {
                    Blacklist defaultValue = (Blacklist) field.get(null);
                    Blacklist.Config blacklistConfig = new Blacklist.Config(this.getBuilder(), name, description, defaultValue);
                    this.configOptions.put(field, blacklistConfig);
                }
                else if (field.getType().isAssignableFrom(IdTagMatcher.class))
                {
                    IdTagMatcher defaultValue = (IdTagMatcher) field.get(null);
                    IdTagMatcher.Config idTagMatcherConfig = new IdTagMatcher.Config(this.getBuilder(), name, description, defaultValue);
                    this.configOptions.put(field, idTagMatcherConfig);
                }
                else {
                    Object defaultValue = field.get(null);
                    ConfigOption.GenericOption genericOption = new ConfigOption.GenericOption(this.getBuilder(), name, description, defaultValue);
                    this.configOptions.put(field, genericOption);
                }
            }
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to load Feature '%s'".formatted(this.name), e);
        }
    }

    public final void loadConfig() {
        if (canBeDisabled) {
            if (!description.isEmpty())
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
            LogHelper.warn("Feature#setConfig failed as %s was not found".formatted(configName));
            return;
        }

        configOption.set(value);
    }

    @Nullable
    public ConfigOption<?> getConfigOption(String configName) {
        Optional<ConfigOption<?>> configOptionOptional = this.configOptions.values()
                .stream()
                .filter(configOption -> configOption.name.equals(configName))
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

            MinecraftForge.EVENT_BUS.register(this);
        }
    }

    public static Feature get(Class<? extends Feature> feature) {
        return Module.getFeature(feature);
    }

    public static boolean isEnabled(Class<? extends Feature> feature) {
        return get(feature).isEnabled();
    }
}
