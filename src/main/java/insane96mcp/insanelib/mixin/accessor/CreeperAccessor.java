package insane96mcp.insanelib.mixin.accessor;

import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Creeper.class)
public interface CreeperAccessor {
    @Accessor
    int getMaxSwell();

    @Accessor
    void setMaxSwell(int maxSwell);

    @Accessor
    int getExplosionRadius();

    @Accessor
    void setExplosionRadius(int explosionRadius);
}
