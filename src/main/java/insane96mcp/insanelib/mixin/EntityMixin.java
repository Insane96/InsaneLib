package insane96mcp.insanelib.mixin;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.module.base.betterfallingblocks.BetterFallingBlockAccessor;
import insane96mcp.insanelib.module.base.betterfallingblocks.BetterFallingBlocks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "spawnAtLocation(Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At("HEAD"), cancellable = true)
    public void insanelib$dummySpawnAtLocation(ItemLike itemLike, CallbackInfoReturnable<ItemEntity> cir) {
        if (!Feature.isEnabled(BetterFallingBlocks.class)
                || !(((Object) this) instanceof FallingBlockEntity fallingBlockEntity)
                || !(itemLike instanceof Block))
            return;

        cir.setReturnValue(null);

        if (self().level().isClientSide)
            return;

        LootParams.Builder lootParams$Builder = (new LootParams.Builder((ServerLevel) self().level())).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(self().blockPosition())).withParameter(LootContextParams.TOOL, ItemStack.EMPTY).withOptionalParameter(LootContextParams.THIS_ENTITY, ((BetterFallingBlockAccessor)fallingBlockEntity).insanelib$getSource());

        List<ItemStack> drops = self().getBlockState().getDrops(lootParams$Builder);

        if (drops.isEmpty())
            return;
        for (ItemStack stack : drops) {
            ItemEntity itemEntity = new ItemEntity(self().level(), self().getX(), self().getY(), self().getZ(), stack);
            itemEntity.setDefaultPickUpDelay();
            if (self().captureDrops() != null)
                self().captureDrops().add(itemEntity);
            else
                self().level().addFreshEntity(itemEntity);
        }
    }

    private FallingBlockEntity self() {
        return (FallingBlockEntity) (Object) this;
    }
}
