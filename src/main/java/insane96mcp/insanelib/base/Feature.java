package insane96mcp.insanelib.base;

import insane96mcp.insanelib.base.config.ConfigOpt;
import insane96mcp.insanelib.base.config.ConfigOption;
import insane96mcp.insanelib.config.MinMax;
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

    HashMap<Field, ConfigOpt<?>> configOptions = new HashMap<>();

    private void loadConfigOptions() {
        this.pushConfig();
        try {
            for (Field field : this.getClass().getDeclaredFields())
            {
                if (!field.isAnnotationPresent(ConfigOption.class))
                    continue;

                if (!field.isAnnotationPresent(Label.class)) {
                    LogHelper.error("%s config option is missing the Label Annotation.".formatted(field.getName()));
                    continue;
                }

                String name = field.getAnnotation(Label.class).name();
                String description = field.getAnnotation(Label.class).description();
                double min = field.getAnnotation(ConfigOption.class).min();
                double max = field.getAnnotation(ConfigOption.class).max();

                if (field.getType().isAssignableFrom(Double.class))
                {
                    double defaultValue = (double) field.get(null);
                    ConfigOpt.Double doubleValue = new ConfigOpt.Double(this.getBuilder(), name, description, defaultValue, min, max);
                    this.configOptions.put(field, doubleValue);
                }
                else if (field.getType().isAssignableFrom(Integer.class))
                {
                    int defaultValue = (int) field.get(null);
                    if (min == Double.MIN_VALUE) min = Integer.MIN_VALUE;
                    if (max == Double.MAX_VALUE) min = Integer.MAX_VALUE;
                    ConfigOpt.Int intValue = new ConfigOpt.Int(this.getBuilder(), name, description, defaultValue, (int) min, (int) max);
                    this.configOptions.put(field, intValue);
                }
                else if (field.getType().isAssignableFrom(Boolean.class))
                {
                    boolean defaultValue = (boolean) field.get(null);
                    ConfigOpt.Bool booleanValue = new ConfigOpt.Bool(this.getBuilder(), name, description, defaultValue);
                    this.configOptions.put(field, booleanValue);
                }
                else if (field.getType().isAssignableFrom(List.class))
                {
                    List<String> defaultValue = (List<String>) field.get(null);
                    ConfigOpt.StringList listValue = new ConfigOpt.StringList(this.getBuilder(), name, description, defaultValue);
                    this.configOptions.put(field, listValue);
                }
                else if (field.getType().isAssignableFrom(MinMax.class))
                {
                    MinMax defaultValue = (MinMax) field.get(null);
                    MinMax.Config minMaxConfig = new MinMax.Config(this.getBuilder(), name, description, defaultValue, min, max);
                    this.configOptions.put(field, minMaxConfig);
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

        String curField = "";
        try {
            for(Field field : this.getClass().getDeclaredFields())
            {
                if (!field.isAnnotationPresent(ConfigOption.class)) {
                    continue;
                }

                curField = this.configOptions.get(field).toString();
                field.set(this, this.configOptions.get(field).get());
            }
        }
        catch (Exception e) {
            LogHelper.error("Failed to set config option for %s", curField);
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
