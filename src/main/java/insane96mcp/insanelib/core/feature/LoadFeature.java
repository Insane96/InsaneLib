package insane96mcp.insanelib.core.feature;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface LoadFeature {
    String module();
    String name() default "";
    String description() default "";
    boolean enabledByDefault() default true;
    boolean canBeDisabled() default true;
    String[] requiresMods() default "";
}
