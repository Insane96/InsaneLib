package insane96mcp.insanelib.base;

public @interface LoadFeature {
    String module();
    boolean enabledByDefault() default true;
    boolean canBeDisabled() default true;
    String[] requiresMods() default "";
}
