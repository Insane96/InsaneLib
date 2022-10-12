package insane96mcp.insanelib.base.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
// Requires @Label and @ConfigOption to work
public @interface ConfigBool {
    boolean defaultValue();
}
