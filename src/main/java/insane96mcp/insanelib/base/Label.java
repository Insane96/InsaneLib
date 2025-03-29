package insane96mcp.insanelib.base;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Deprecation: Use {@link insane96mcp.insanelib.base.config.Config} or {@link insane96mcp.insanelib.base.LoadFeature}
 */
@Deprecated(since = "1.19.0", forRemoval = true)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface Label {
    String name() default "";
    String description() default "";
}
