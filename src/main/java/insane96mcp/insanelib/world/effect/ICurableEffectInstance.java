package insane96mcp.insanelib.world.effect;

/**
 * Implemented on {@link net.minecraft.world.effect.MobEffectInstance} via mixin. Allows marking single effect instances
 * as non-curable (e.g. by milk or totems). Note that the flag is not saved nor synced.
 */
public interface ICurableEffectInstance {
    boolean insanelib$isCurable();

    void insanelib$setCurable(boolean curable);
}
