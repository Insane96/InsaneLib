package insane96mcp.insanelib.mixin;

import insane96mcp.insanelib.event.ILEventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FireBlock.class)
public class FireBlockMixin {
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;", ordinal = 1), method = "tryCatchFire")
    private void onBlockBurnt(Level level, BlockPos pos, int p_53434_, RandomSource random, int p_53436_, Direction face, CallbackInfo ci) {
        ILEventFactory.onBlockBurnt(level, pos, level.getBlockState(pos));
    }
}
