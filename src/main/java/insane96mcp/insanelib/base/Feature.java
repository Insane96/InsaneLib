package insane96mcp.insanelib.base;

import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.MinMax;
import insane96mcp.insanelib.util.LogHelper;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

public class Feature {
    private final String name;
    private final String description;
    private final ForgeConfigSpec.ConfigValue<Boolean> enabledConfig;
    private final Module module;

    private final boolean canBeDisabled;

    private boolean enabled;

    public Feature(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        if (!this.getClass().isAnnotationPresent(Label.class))
            LogHelper.error("%s is missing the Label Annotation.".formatted(this.getClass().getName()));
        this.name = this.getClass().getAnnotation(Label.class).name();
        this.description = this.getClass().getAnnotation(Label.class).description();
        this.module = module;
        this.canBeDisabled = canBeDisabled;
        if (canBeDisabled)
            if (!description.equals(""))
                enabledConfig = this.module.builder.comment(getDescription()).define("Enable " + getName(), enabledByDefault);
            else
                enabledConfig = this.module.builder.define("Enable " + getName(), enabledByDefault);
        else
            enabledConfig = null;
        this.registerEvents();
        this.loadConfigOptions();
    }

    public Feature(Module module, boolean enabledByDefault) {
        this(module, enabledByDefault, true);
    }

    public Feature(Module module) {
        this(module, true);
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
        return this.module.builder;
    }

    HashMap<Field, ConfigOption<?>> configOptions = new HashMap<>();

    private void loadConfigOptions() {
        this.pushConfig();
        try {
            for (Field field : this.getClass().getDeclaredFields())
            {
                if (!field.isAnnotationPresent(Config.class))
                    continue;

                if (!field.isAnnotationPresent(Label.class)) {
                    LogHelper.error("%s config option is missing the Label Annotation.".formatted(field.getName()));
                    continue;
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
                    if (max == Double.MAX_VALUE) min = Integer.MAX_VALUE;
                    ConfigOption.IntOption intOption = new ConfigOption.IntOption(this.getBuilder(), name, description, defaultValue, (int) min, (int) max);
                    this.configOptions.put(field, intOption);
                }
                else if (field.getType().isAssignableFrom(Boolean.class))
                {
                    boolean defaultValue = (boolean) field.get(null);
                    ConfigOption.BoolOption boolOption = new ConfigOption.BoolOption(this.getBuilder(), name, description, defaultValue);
                    this.configOptions.put(field, boolOption);
                }
                else if (field.getType().isAssignableFrom(String.class))
                {
                    String defaultValue = (String) field.get(null);
                    ConfigOption.StringOption stringOption = new ConfigOption.StringOption(this.getBuilder(), name, description, defaultValue);
                    this.configOptions.put(field, stringOption);
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
        this.popConfig();
    }

    public void loadConfig() {
        if (canBeDisabled)
            this.enabled = enabledConfig.get();
        else
            this.enabled = true;

        readConfigOptions();
    }

    private void readConfigOptions() {
        if (this.configOptions.size() == 0)
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

    public void pushConfig() {
        if (!description.equals(""))
            this.module.builder.comment(this.getDescription()).push(this.getName());
        else
            this.module.builder.push(this.getName());
    }

    protected void popConfig() {
        this.module.builder.pop();
    }

    public void registerEvents() {
        for (Method method : this.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(SubscribeEvent.class))
                continue;

            MinecraftForge.EVENT_BUS.register(this);
        }
    }
}
