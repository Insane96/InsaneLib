package insane96mcp.insanelib.mixin;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.event.ILEventFactory;
import insane96mcp.insanelib.module.base.betterfallingblocks.BetterFallingBlockAccessor;
import insane96mcp.insanelib.module.base.betterfallingblocks.BetterFallingBlocks;
import insane96mcp.insanelib.util.LogHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.Arrays;

@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockEntityMixin extends Entity implements BetterFallingBlockAccessor {
    @Unique
    private Entity insanelib$source;
    @Unique
    public Direction insanelib$directionFalling;
    @Unique
    public Direction insanelib$movedFrom;

    @Shadow
    private BlockState blockState;

    @Shadow
    public int time;

    @Shadow
    public boolean dropItem;

    @Shadow
    private boolean cancelDrop;

    @Shadow
    public abstract void callOnBrokenAfterFall(Block p_149651_, BlockPos p_149652_);

    @Shadow
    @Nullable
    public CompoundTag blockData;

    public FallingBlockEntityMixin(EntityType<?> p_19870_, Level p_19871_) {
        super(p_19870_, p_19871_);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/FallingBlockEntity;discard()V", ordinal = 2))
    private void insanelib$onDiscardOnLand(CallbackInfo ci) {
        ILEventFactory.onFallingBlockLand((FallingBlockEntity) (Object) this);
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void insanelib$replaceTick(CallbackInfo ci) {
        if (!Feature.isEnabled(BetterFallingBlocks.class))
            return;
        ci.cancel();
        //Fixes duping exploit through dimensions
        if (this.isRemoved() && BetterFallingBlocks.fixDupeExploit)
            return;
        if (this.blockState.isAir()) {
            this.discard();
        } else {
            Block block = this.blockState.getBlock();
            ++this.time;
            if (!this.isNoGravity()) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.04D, 0.0D));
            }

            this.move(MoverType.SELF, this.getDeltaMovement());
            //Fixes duping exploit through dimensions
            if (this.isRemoved())
                return;
            if (!this.level().isClientSide) {
                BlockPos blockPos = this.blockPosition();
                boolean isConcretePowder = this.blockState.getBlock() instanceof ConcretePowderBlock;
                boolean canBeHydrated = isConcretePowder && this.blockState.canBeHydrated(this.level(), blockPos, this.level().getFluidState(blockPos), blockPos);
                double d0 = this.getDeltaMovement().lengthSqr();
                if (isConcretePowder && d0 > 1.0D) {
                    BlockHitResult blockhitresult = this.level().clip(new ClipContext(new Vec3(this.xo, this.yo, this.zo), this.position(), ClipContext.Block.COLLIDER, ClipContext.Fluid.SOURCE_ONLY, this));
                    if (blockhitresult.getType() != HitResult.Type.MISS && this.blockState.canBeHydrated(this.level(), blockPos, this.level().getFluidState(blockhitresult.getBlockPos()), blockhitresult.getBlockPos())) {
                        blockPos = blockhitresult.getBlockPos();
                        canBeHydrated = true;
                    }
                }

                if (!this.onGround() && !canBeHydrated) {
                    if (!this.level().isClientSide && (this.time > 100 && (blockPos.getY() <= this.level().getMinBuildHeight() || blockPos.getY() > this.level().getMaxBuildHeight()) || this.time > 600)) {
                        if (this.dropItem && this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                            this.spawnAtLocation(block);
                        }

                        this.discard();
                    }
                }
                else {
                    BlockState blockstate = this.level().getBlockState(blockPos);
                    BlockState blockStateBelow = this.level().getBlockState(blockPos.below());
                    this.setDeltaMovement(this.getDeltaMovement().multiply(0.7D, -0.5D, 0.7D));
                    if (!blockstate.is(Blocks.MOVING_PISTON)) {
                        if (!this.cancelDrop) {
                            boolean canBeReplaced = blockstate.canBeReplaced(new DirectionalPlaceContext(this.level(), blockPos, Direction.DOWN, ItemStack.EMPTY, Direction.UP));
                            boolean canBreak = blockstate.getDestroySpeed(this.level(), blockPos) == 0f && BetterFallingBlocks.breakInstabreakBlocks;
                            boolean canBreakBelow = blockStateBelow.getDestroySpeed(this.level(), blockPos.below()) == 0f && BetterFallingBlocks.breakInstabreakBlocks;
                            boolean isFreeBelow = (FallingBlock.isFree(blockStateBelow) || canBreakBelow) && (!isConcretePowder || !canBeHydrated);
                            if (isFreeBelow) {
                                BlockPos posOn = this.getOnPos();
                                this.move(MoverType.SELF, new Vec3((this.blockPosition().getX() - posOn.getX()) * this.getBbWidth() * 0.5f, 0d, (this.blockPosition().getZ() - posOn.getZ()) * this.getBbWidth() * 0.5d));
                            }
                            else if (canBeReplaced || canBreak)
                                this.insanelib$place(blockstate, block, blockPos, canBreak);
                            else
                                this.insanelib$tryStackAboveOrMove(blockPos);
                        }
                        else {
                            this.discard();
                            this.callOnBrokenAfterFall(block, blockPos);
                        }
                    }
                }
            }

            this.setDeltaMovement(this.getDeltaMovement().scale(0.98D));
        }
    }

    @Unique
    public void insanelib$tryStackAboveOrMove(BlockPos pos) {
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos(pos.getX(), pos.getY(), pos.getZ());
        boolean maxStackReached = false;
        while (true) {
            if (this.insanelib$tryPlace(blockPos))
                break;
            blockPos.set(blockPos.above());
            if (blockPos.getY() - pos.getY() > 3) {
                maxStackReached = true;
                break;
            }
        }
        if (maxStackReached) {
            Direction dir;
            if (this.insanelib$directionFalling != null)
                dir = this.random.nextBoolean() ? this.insanelib$directionFalling.getClockWise() : this.insanelib$directionFalling.getCounterClockWise();
            else
                dir = Arrays.stream(Direction.values()).filter((direction) -> direction.getAxis().isHorizontal() && direction != this.insanelib$movedFrom).skip(this.random.nextInt(4)).findFirst().get();
            //blockPos.set(pos.relative(dir));
            this.insanelib$directionFalling = dir;
            this.insanelib$movedFrom = dir.getOpposite();
            this.setPos(this.position().relative(dir, 1d).relative(Direction.UP, 1));
        }
    }

    @Unique
    public boolean insanelib$tryPlace(BlockPos blockPos) {
        BlockState stateAt = this.level().getBlockState(blockPos);
        BlockState stateOn = this.level().getBlockState(blockPos.below());
        boolean canBeReplaced = stateAt.canBeReplaced(new DirectionalPlaceContext(this.level(), blockPos, Direction.DOWN, ItemStack.EMPTY, Direction.UP));
        boolean canBreak = stateAt.getDestroySpeed(this.level(), blockPos) == 0f && BetterFallingBlocks.breakInstabreakBlocks;
        boolean canSurvive = this.blockState.canSurvive(this.level(), blockPos);
        boolean isFree = FallingBlock.isFree(this.level().getBlockState(blockPos.below()));
        if ((canBeReplaced || canBreak) && /*isHarderThanInside && */canSurvive) {
            if (isFree && this.blockState.canSurvive(this.level(), blockPos.below())) {
                BlockPos posOn = this.getOnPos();
                this.move(MoverType.SELF, new Vec3((this.blockPosition().getX() - posOn.getX()) * 0.5d, 0d, (this.blockPosition().getZ() - posOn.getZ()) * 0.5d));
            }
            else if (canBreak)
                this.insanelib$place(stateOn, this.blockState.getBlock(), blockPos, true);
            else return false;
            return true;
        }
        return false;
    }

    @Unique
    public void insanelib$place(BlockState stateOn, Block block, BlockPos pos, boolean breakBlock) {
        if (this.blockState.hasProperty(BlockStateProperties.WATERLOGGED) && this.level().getFluidState(pos).getType() == Fluids.WATER) {
            this.blockState = this.blockState.setValue(BlockStateProperties.WATERLOGGED, Boolean.TRUE);
        }

        if (breakBlock) {
            BlockState stateToBreak = this.level().getBlockState(pos);
            ServerLevel serverlevel = (ServerLevel)this.level();
            BlockEntity blockEntity = stateToBreak.hasBlockEntity() ? serverlevel.getBlockEntity(pos) : null;
            LootParams.Builder lootparams$builder = (new LootParams.Builder((ServerLevel) this.level())).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos)).withParameter(LootContextParams.TOOL, ItemStack.EMPTY).withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity).withOptionalParameter(LootContextParams.THIS_ENTITY, this);

            stateToBreak.spawnAfterBreak(serverlevel, pos, ItemStack.EMPTY, false);
            stateToBreak.getDrops(lootparams$builder).forEach(stack ->
                    serverlevel.addFreshEntity(new ItemEntity(serverlevel, pos.getCenter().x, pos.getCenter().y + 0.6f, pos.getCenter().z, stack))
            );
            this.level().destroyBlock(pos, false);
        }
        if (this.level().setBlockAndUpdate(pos, this.blockState)) {
            Block.updateFromNeighbourShapes(this.blockState, this.level(), pos);
            ((ServerLevel)this.level()).getChunkSource().chunkMap.broadcast(this, new ClientboundBlockUpdatePacket(pos, this.level().getBlockState(pos)));
            this.discard();
            if (block instanceof Fallable) {
                ((Fallable)block).onLand(this.level(), pos, this.blockState, stateOn, (FallingBlockEntity) (Object) this);
            }

            if (this.blockData != null && this.blockState.hasBlockEntity()) {
                BlockEntity blockentity = this.level().getBlockEntity(pos);
                if (blockentity != null) {
                    CompoundTag compoundtag = blockentity.saveWithoutMetadata();

                    for(String s : this.blockData.getAllKeys()) {
                        compoundtag.put(s, this.blockData.get(s).copy());
                    }

                    try {
                        blockentity.load(compoundtag);
                    }
                    catch (Exception exception) {
                        LogHelper.error("Failed to load block entity from falling block", exception);
                    }

                    blockentity.setChanged();
                }
            }
        }
        else if (this.dropItem && this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            this.discard();
            this.callOnBrokenAfterFall(block, pos);
            this.spawnAtLocation(block);
        }
    }
}
