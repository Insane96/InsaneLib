package insane96mcp.insanelib.base.config;

public @interface LoadFeature {
    String module();
    boolean enabledByDefault() default true;
    boolean canBeDisabled() default true;
}
