package insane96mcp.insanelib.base;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
// Requires @Label to work
public @interface ConfigDouble {
    double defaultValue() default 0d;
    double min() default Double.MIN_VALUE;
    double max() default Double.MAX_VALUE;
}
