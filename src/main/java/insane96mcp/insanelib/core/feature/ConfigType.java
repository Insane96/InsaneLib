package insane96mcp.insanelib.core.feature;

import net.neoforged.fml.config.ModConfig;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The config (file) type of a {@link LoadFeature} feature. Without it the feature uses the config type of its module.<br>
 * A module can have features of different config types: it appears in the config of each type with its own section and
 * "Enable" toggle, containing only the features of that type. The mod must create an ILModConfig for each type used.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ConfigType {
    ModConfig.Type value();
}
