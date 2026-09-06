package insane96mcp.insanelib.mixin.accessor;

import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Explosion.class)
public interface ExplosionAccessor {
    @Accessor
    void setFire(boolean fire);
    @Accessor
    @Mutable
    void setExplosionSound(Holder<SoundEvent> explosionSound);
}
