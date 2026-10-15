package insane96mcp.insanelib.mixin.accessor;

import net.minecraft.world.level.ServerExplosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerExplosion.class)
public interface ExplosionAccessor {
    @Accessor
    @Mutable
    void setFire(boolean fire);
}
