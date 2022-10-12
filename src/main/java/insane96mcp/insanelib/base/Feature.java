package insane96mcp.insanelib.base;

import insane96mcp.insanelib.base.config.ConfigBool;
import insane96mcp.insanelib.base.config.ConfigDouble;
import insane96mcp.insanelib.base.config.ConfigInt;
import insane96mcp.insanelib.base.config.ConfigOption;
import insane96mcp.insanelib.util.LogHelper;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

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

    HashMap<Field, ForgeConfigSpec.ConfigValue<?>> configOptions = new HashMap<>();

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
                if (field.isAnnotationPresent(ConfigDouble.class))
                {
                    double defaultValue = (double) field.get(null);
                    double min = field.getAnnotation(ConfigDouble.class).min();
                    double max = field.getAnnotation(ConfigDouble.class).max();
                    if (!description.equals("")) {
                        ForgeConfigSpec.DoubleValue doubleValue = this.module.builder.comment(description).defineInRange(name, defaultValue, min, max);
                        this.configOptions.put(field, doubleValue);
                    }
                }
                else if (field.isAnnotationPresent(ConfigInt.class))
                {
                    int defaultValue = (int) field.get(null);
                    int min = field.getAnnotation(ConfigInt.class).min();
                    int max = field.getAnnotation(ConfigInt.class).max();
                    if (!description.equals("")) {
                        ForgeConfigSpec.IntValue intValue = this.module.builder.comment(description).defineInRange(name, defaultValue, min, max);
                        this.configOptions.put(field, intValue);
                    }
                }
                else if (field.isAnnotationPresent(ConfigBool.class))
                {
                    boolean defaultValue = (boolean) field.get(null);
                    if (!description.equals("")) {
                        ForgeConfigSpec.BooleanValue booleanValue = this.module.builder.comment(description).define(name, defaultValue);
                        this.configOptions.put(field, booleanValue);
                    }
                }
            /*else if (field.isAnnotationPresent(ConfigMinMax.class))
            {
                double min = field.getAnnotation(ConfigMinMax.class).defaultMinValue();
                double max = field.getAnnotation(ConfigMinMax.class).defaultMaxValue();
                if (!description.equals("")) {
                    ForgeConfigSpec.BooleanValue booleanValue = this.module.builder.comment(description).define(name, defaultValue);
                    this.configOptions.put(field, booleanValue);
                }
            }*/
            }
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to load Feature " + this.name, e);
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

                curField = String.join(".", this.configOptions.get(field).getPath());
                field.set(this, this.configOptions.get(field).get());
            }
        }
        catch (IllegalAccessException e) {
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
