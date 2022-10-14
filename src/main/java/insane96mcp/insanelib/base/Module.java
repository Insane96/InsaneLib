package insane96mcp.insanelib.base;

import insane96mcp.insanelib.setup.Config;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.IExtensibleEnum;

public enum Module implements IExtensibleEnum {
    BASE(Config.builder, "base", "", true, false);

    private final ForgeConfigSpec.ConfigValue<Boolean> enabledConfig;

    protected final ForgeConfigSpec.Builder builder;

    private boolean enabled;

    private final boolean canBeDisabled;

    private final String name;
    private final String description;

    Module(final ForgeConfigSpec.Builder builder, String moduleName, String description, boolean enabledByDefault, boolean canBeDisabled) {
        this.builder = builder;
        this.name = moduleName;
        this.description = description;
        this.canBeDisabled = canBeDisabled;
        if (canBeDisabled)
            if (!description.equals(""))
                enabledConfig = this.builder.comment(description).define("Enable " + this.name + " module", enabledByDefault);
            else
                enabledConfig = this.builder.define("Enable " + this.name, enabledByDefault);
        else
            enabledConfig = null;
    }

    Module(final ForgeConfigSpec.Builder builder, String moduleName, String description, boolean enabledByDefault) {
        this(builder, moduleName, description, enabledByDefault, true);
    }

    Module(final ForgeConfigSpec.Builder builder, String moduleName, String description) {
        this(builder, moduleName, description, true);
    }

    Module(final ForgeConfigSpec.Builder builder, String moduleName) {
        this(builder, moduleName, "", true);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getName() {
        return this.name;
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
            this.builder.comment(this.description).push(this.getName());
        }
    }

    public void popConfig() {
        this.builder.pop();
    }

    public static void loadFeatures() {

    }

    public static Module create(String name, final ForgeConfigSpec.Builder builder, String moduleName)
    {
        throw new IllegalStateException("Enum not extended");
    }

    public static Module create(String name, final ForgeConfigSpec.Builder builder, String moduleName, String description)
    {
        throw new IllegalStateException("Enum not extended");
    }

    public static Module create(String name, final ForgeConfigSpec.Builder builder, String moduleName, String description, boolean enabledByDefault)
    {
        throw new IllegalStateException("Enum not extended");
    }

    public static Module create(String name, final ForgeConfigSpec.Builder builder, String moduleName, String description, boolean enabledByDefault, boolean canBeDisabled)
    {
        throw new IllegalStateException("Enum not extended");
    }
}
