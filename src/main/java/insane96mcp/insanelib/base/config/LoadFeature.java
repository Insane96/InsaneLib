package insane96mcp.insanelib.base.config;

import insane96mcp.insanelib.base.Module;

public @interface LoadFeature {
    Module module();
    boolean enabledByDefault() default true;
    boolean canBeDisabled() default true;
}
