package insane96mcp.insanelib.base;

import insane96mcp.insanelib.util.LogHelper;
import net.minecraftforge.common.ForgeConfigSpec;

public class Module {
    private final ForgeConfigSpec.ConfigValue<Boolean> enabledConfig;

    protected final ForgeConfigSpec.Builder builder;

    private boolean enabled;

    private final boolean canBeDisabled;

    private final String name;
    private final String description;

    public Module(final ForgeConfigSpec.Builder builder, boolean enabledByDefault, boolean canBeDisabled) {
        this.builder = builder;
        if (!this.getClass().isAnnotationPresent(Label.class))
            LogHelper.error("%s is missing the Label Annotation.".formatted(this.getClass().getName()));
        this.name = this.getClass().getAnnotation(Label.class).name();
        this.description = this.getClass().getAnnotation(Label.class).description();
        this.canBeDisabled = canBeDisabled;
        if (canBeDisabled)
            if (!description.equals(""))
                enabledConfig = this.builder.comment(this.description).define("Enable " + this.name + " module", enabledByDefault);
            else
                enabledConfig = this.builder.define("Enable " + this.name, enabledByDefault);
        else
            enabledConfig = null;
    }

    public Module(final ForgeConfigSpec.Builder builder, boolean enabledByDefault) {
        this(builder, enabledByDefault, true);
    }

    public Module(final ForgeConfigSpec.Builder builder) {
        this(builder, true);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getName() {
        return this.name;
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

    public void pushConfig() {
        if (description.equals("")) {
            this.builder.push(this.getName());
        }
        else {
            this.builder.comment(this.getDescription()).push(this.getName());
        }
    }

    protected void popConfig() {
        this.builder.pop();
    }
}
