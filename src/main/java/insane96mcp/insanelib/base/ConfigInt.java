package insane96mcp.insanelib.base;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
// Requires @Label to work
public @interface ConfigInt {
    int defaultValue() default 0;
    int min() default Integer.MIN_VALUE;
    int max() default Integer.MAX_VALUE;
}
