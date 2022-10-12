package insane96mcp.insanelib.base;

import insane96mcp.insanelib.util.LogHelper;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.lang.reflect.Method;

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

    public void loadConfig() {
        if (canBeDisabled)
            this.enabled = enabledConfig.get();
        else
            this.enabled = true;
    }

    public void pushConfig(final ForgeConfigSpec.Builder builder) {
        if (!description.equals(""))
            builder.comment(this.getDescription()).push(this.getName());
        else
            builder.push(this.getName());
    }

    public void registerEvents() {
        for (Method method : this.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(SubscribeEvent.class))
                continue;

            MinecraftForge.EVENT_BUS.register(this);
        }
    }

    public void push(String name) {
        this.module.builder.push(name);
    }

    public void pop() {
        this.module.builder.pop();
    }

    public void pop(int count) {
        this.module.builder.pop(count);
    }

    public ForgeConfigSpec.BooleanValue defineBool(String name, String comment, boolean defaultValue) {
        return this.module.builder.comment(comment).define(name, defaultValue);
    }

    public ForgeConfigSpec.DoubleValue defineDouble(String name, String comment, double defaultValue) {
        return this.defineDouble(name, comment, defaultValue, Double.MIN_VALUE, Double.MAX_VALUE);
    }

    public ForgeConfigSpec.DoubleValue defineDouble(String name, String comment, double defaultValue, double min, double max) {
        return this.module.builder.comment(comment).defineInRange(name, defaultValue, min, max);
    }

    public ForgeConfigSpec.IntValue defineInt(String name, String comment, int defaultValue) {
        return this.defineInt(name, comment, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public ForgeConfigSpec.IntValue defineInt(String name, String comment, int defaultValue, int min, int max) {
        return this.module.builder.comment(comment).defineInRange(name, defaultValue, min, max);
    }
}
