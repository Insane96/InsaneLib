package insane96mcp.insanelib.base.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Config {
    double min() default Double.MIN_VALUE;
    double max() default Double.MAX_VALUE;
}
