package insane96mcp.insanelib.base;

import insane96mcp.insanelib.utils.LogHelper;
import net.minecraftforge.common.ForgeConfigSpec;

public class Module {
    private final ForgeConfigSpec.ConfigValue<Boolean> enabledConfig;

    private boolean enabled;

    private final String name;
    private final String description;

    public Module(final ForgeConfigSpec.Builder builder, boolean enabledByDefault) {
        if (!this.getClass().isAnnotationPresent(Label.class))
            LogHelper.error(String.format("%s is missing the Label Annotation.", this.getClass().getName()));
        this.name = this.getClass().getAnnotation(Label.class).name();
        this.description = this.getClass().getAnnotation(Label.class).description();
        if (!description.equals(""))
            enabledConfig = builder.comment(this.description).define("Enable " + this.name + " module", enabledByDefault);
        else
            enabledConfig = builder.define("Enable " + this.name, enabledByDefault);
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
        this.enabled = enabledConfig.get();
    }

    public void pushConfig(final ForgeConfigSpec.Builder builder) {
        if (!description.equals(""))
            builder.comment(this.getDescription()).push(this.getName());
        else
            builder.push(this.getName());
    }
}
