package insane96mcp.insanelib.mixin;

import insane96mcp.insanelib.InsaneLib;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.event.ILEventFactory;
import insane96mcp.insanelib.module.base.betterfallingblocks.BetterFallingBlockExtensor;
import insane96mcp.insanelib.module.base.betterfallingblocks.BetterFallingBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
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
import net.minecraft.world.level.gamerules.GameRules;
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
public abstract class FallingBlockEntityMixin extends Entity implements BetterFallingBlockExtensor {
    @Unique
    private static final double GRAVITY_ACCELERATION = 0.04D;
    @Unique
    private static final double HORIZONTAL_DRAG = 0.7D;
    @Unique
    private static final double VERTICAL_BOUNCE = -0.5D;
    @Unique
    private static final double AIR_RESISTANCE = 0.98D;
    @Unique
    private static final int MAX_TIME_OUTSIDE_WORLD = 100;
    @Unique
    private static final int ABSOLUTE_MAX_TIME = 600;
    @Unique
    private static final int MAX_STACK_HEIGHT = 3;
    @Unique
    private static final double CONCRETE_POWDER_VELOCITY_THRESHOLD = 1.0D;

    @Unique
    private Entity insanelib$source;
    @Unique
    public Direction insanelib$directionFalling;
    @Unique
    public Direction insanelib$movedFrom;
    /**
     * If true, the falling block has been moved up to try and stack
     */
    @Unique
    public boolean insanelib$tryingToStack;

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
        if (!Feature.isEnabled(BetterFallingBlocks.class)
                || this.blockState.is(BetterFallingBlocks.BLACKLISTED_BLOCKS))
            return;
        ci.cancel();

        if (this.insanelib$shouldDiscardEntity())
            return;

        this.insanelib$applyPhysics();
        this.insanelib$handleServerSideLogic();
    }

    @Unique
    private boolean insanelib$shouldDiscardEntity() {
        //Fixes duping exploit through dimensions
        if (BetterFallingBlocks.fixDupeExploit && this.isRemoved())
            return true;

        if (this.blockState.isAir()) {
            this.discard();
            return true;
        }
        return false;
    }

    @Unique
    private void insanelib$applyPhysics() {
        ++this.time;
        if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -GRAVITY_ACCELERATION, 0.0D));
        }
        this.move(MoverType.SELF, this.getDeltaMovement());
        this.setDeltaMovement(this.getDeltaMovement().scale(AIR_RESISTANCE));
    }

    @Unique
    private void insanelib$handleServerSideLogic() {
        if (this.level().isClientSide())
            return;

        BlockPos blockPos = this.blockPosition();
        boolean isConcretePowder = this.blockState.getBlock() instanceof ConcretePowderBlock;
        boolean canBeHydrated = this.insanelib$checkConcreteHydration(blockPos, isConcretePowder);

        if (!this.onGround() && !canBeHydrated)
            this.insanelib$handleFalling(blockPos);
        else
            this.insanelib$handleLanded(blockPos, isConcretePowder, canBeHydrated);
    }

    @Unique
    private boolean insanelib$checkConcreteHydration(BlockPos blockPos, boolean isConcretePowder) {
        if (!isConcretePowder)
            return false;

        boolean canBeHydrated = this.blockState.canBeHydrated(this.level(), blockPos, this.level().getFluidState(blockPos), blockPos);
        double velocitySquared = this.getDeltaMovement().lengthSqr();

        if (velocitySquared > CONCRETE_POWDER_VELOCITY_THRESHOLD) {
            BlockHitResult hitResult = this.level().clip(new ClipContext(
                new Vec3(this.xo, this.yo, this.zo), this.position(),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.SOURCE_ONLY, this
            ));

            if (hitResult.getType() != HitResult.Type.MISS) {
                BlockPos hitPos = hitResult.getBlockPos();
                if (this.blockState.canBeHydrated(this.level(), blockPos, this.level().getFluidState(hitPos), hitPos)) {
                    return true;
                }
            }
        }
        return canBeHydrated;
    }

    @Unique
    private void insanelib$handleFalling(BlockPos blockPos) {
        Block block = this.blockState.getBlock();
        boolean isOutsideWorld = blockPos.getY() <= this.level().getMinY() || blockPos.getY() > this.level().getMaxY() + 1;

        if ((this.time > MAX_TIME_OUTSIDE_WORLD && isOutsideWorld) || this.time > ABSOLUTE_MAX_TIME) {
            if (this.dropItem && this.level() instanceof ServerLevel serverLevel && serverLevel.getGameRules().get(GameRules.ENTITY_DROPS)) {
                this.spawnAtLocation(serverLevel, block);
            }
            this.discard();
        }
    }

    @Unique
    private void insanelib$handleLanded(BlockPos blockPos, boolean isConcretePowder, boolean canBeHydrated) {
        Block block = this.blockState.getBlock();
        BlockState stateAt = this.level().getBlockState(blockPos);
        BlockState blockStateBelow = this.level().getBlockState(blockPos.below());

        this.setDeltaMovement(this.getDeltaMovement().multiply(HORIZONTAL_DRAG, VERTICAL_BOUNCE, HORIZONTAL_DRAG));

        if (stateAt.is(Blocks.MOVING_PISTON))
            return;

        if (this.cancelDrop) {
            this.discard();
            this.callOnBrokenAfterFall(block, blockPos);
            return;
        }

        boolean canReplaceAtPos = stateAt.canBeReplaced(new DirectionalPlaceContext(this.level(), blockPos, Direction.DOWN, ItemStack.EMPTY, Direction.UP));
        boolean breakInstaBreak = BetterFallingBlocks.breakInstabreakBlocks;
        boolean isInstaBreak = stateAt.getDestroySpeed(this.level(), blockPos) == 0f;
        boolean isInstaBreakBelow = blockStateBelow.getDestroySpeed(this.level(), blockPos.below()) == 0f;
        boolean canBreakAtPos = isInstaBreak && breakInstaBreak;
        boolean canBreakBelow = isInstaBreakBelow && breakInstaBreak;
        boolean shouldHydrate = isConcretePowder && canBeHydrated;
        boolean isFreeBelow = (FallingBlock.isFree(blockStateBelow) || canBreakBelow) && !shouldHydrate;

        if (isFreeBelow) {
            if (this.getDeltaMovement().length() <= 1e-5f) {
                this.move(MoverType.SELF, new Vec3(
                        Vec3.atCenterOf(this.blockPosition()).x - this.position().x,
                        0d,
                        Vec3.atCenterOf(this.blockPosition()).z - this.position().z
                ));
            }
        } else if (canReplaceAtPos || canBreakAtPos) {
            this.insanelib$place(stateAt, block, blockPos, canBreakAtPos);
        }
        else if (!breakInstaBreak && isInstaBreak) {
            this.insanelib$handleFailedPlacement(block, blockPos);
        }
        else {
            this.insanelib$tryStackAboveOrMove(blockPos);
        }
    }

    @Unique
    public void insanelib$tryStackAboveOrMove(BlockPos pos) {
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos(pos.getX(), pos.getY(), pos.getZ());
        boolean maxStackReached = false;
        while (true) {
            if (this.insanelib$canPlace(blockPos))
                break;
            blockPos.set(blockPos.above());
            if (blockPos.getY() - pos.getY() > MAX_STACK_HEIGHT) {
                maxStackReached = true;
                break;
            }
        }
        if (maxStackReached || insanelib$tryingToStack) {
            Direction dir = this.insanelib$selectRandomHorizontalDirection();
            this.insanelib$directionFalling = dir;
            this.insanelib$movedFrom = dir.getOpposite();
            this.setPos(this.position().relative(dir, 1d));
            insanelib$tryingToStack = false;
        }
        else {
            this.setPos(this.getX(), (blockPos.getY() - this.getBlockY()) + this.getY(), this.getZ());
            boolean breakInstaBreak = BetterFallingBlocks.breakInstabreakBlocks;
            BlockState stateAt = this.level().getBlockState(this.blockPosition());
            boolean isInstaBreak = stateAt.getDestroySpeed(this.level(), blockPos) == 0f;
            boolean canBreakAtPos = isInstaBreak && breakInstaBreak;
            this.insanelib$place(stateAt, this.blockState.getBlock(), blockPos, canBreakAtPos);
            //insanelib$tryingToStack = true;
        }
    }

    @Unique
    public boolean insanelib$canPlace(BlockPos blockPos) {
        BlockState stateAt = this.level().getBlockState(blockPos);

        if (!this.insanelib$canPlaceBlock(blockPos, stateAt))
            return false;

        // Check if block below is free - if so, center the entity horizontally
        BlockState stateBelow = this.level().getBlockState(blockPos.below());
        if (FallingBlock.isFree(stateBelow) && this.blockState.canSurvive(this.level(), blockPos.below()))
            this.insanelib$centerHorizontally();

        return true;
    }

    @Unique
    private void insanelib$centerHorizontally() {
        BlockPos posOn = this.getOnPos();
        double deltaX = (this.blockPosition().getX() - posOn.getX()) * 0.5d;
        double deltaZ = (this.blockPosition().getZ() - posOn.getZ()) * 0.5d;
        this.move(MoverType.SELF, new Vec3(deltaX, 0d, deltaZ));
    }

    @Unique
    private boolean insanelib$canPlaceBlock(BlockPos blockPos, BlockState stateAt) {
        boolean canBeReplaced = stateAt.canBeReplaced(new DirectionalPlaceContext(this.level(), blockPos, Direction.DOWN, ItemStack.EMPTY, Direction.UP));
        boolean canBreak = stateAt.getDestroySpeed(this.level(), blockPos) == 0f && BetterFallingBlocks.breakInstabreakBlocks;
        boolean canSurvive = this.blockState.canSurvive(this.level(), blockPos);
        return (canBeReplaced || canBreak) && canSurvive;
    }

    @Unique
    public void insanelib$place(BlockState stateOn, Block block, BlockPos pos, boolean breakBlock) {
        if (breakBlock) {
            this.insanelib$breakBlockAndDropLoot(pos);
        }

        this.insanelib$applyWaterlogging(pos);

        if (this.insanelib$placeBlockState(pos)) {
            this.insanelib$handleSuccessfulPlacement(stateOn, block, pos);
        } else {
            this.insanelib$handleFailedPlacement(block, pos);
        }
    }

    @Unique
    private void insanelib$breakBlockAndDropLoot(BlockPos pos) {
        ServerLevel serverLevel = (ServerLevel)this.level();
        BlockState stateToBreak = serverLevel.getBlockState(pos);
        BlockEntity blockEntity = stateToBreak.hasBlockEntity() ? serverLevel.getBlockEntity(pos) : null;

        LootParams.Builder lootParamsBuilder = new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
                .withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity)
                .withOptionalParameter(LootContextParams.THIS_ENTITY, this);

        stateToBreak.spawnAfterBreak(serverLevel, pos, ItemStack.EMPTY, false);
        stateToBreak.getDrops(lootParamsBuilder).forEach(stack ->
                serverLevel.addFreshEntity(new ItemEntity(serverLevel, pos.getCenter().x, pos.getCenter().y + 0.6f, pos.getCenter().z, stack))
        );
        serverLevel.destroyBlock(pos, false);
    }

    @Unique
    private void insanelib$applyWaterlogging(BlockPos pos) {
        if (this.blockState.hasProperty(BlockStateProperties.WATERLOGGED)
                && this.level().getFluidState(pos).getType() == Fluids.WATER) {
            this.blockState = this.blockState.setValue(BlockStateProperties.WATERLOGGED, Boolean.TRUE);
        }
    }

    @Unique
    private boolean insanelib$placeBlockState(BlockPos pos) {
        return this.level().setBlockAndUpdate(pos, this.blockState);
    }

    @Unique
    private void insanelib$handleSuccessfulPlacement(BlockState stateOn, Block block, BlockPos pos) {
        ServerLevel serverLevel = (ServerLevel)this.level();
        Block.updateFromNeighbourShapes(this.blockState, this.level(), pos);
        serverLevel.getChunkSource().chunkMap.sendToTrackingPlayers(this, new ClientboundBlockUpdatePacket(pos, this.level().getBlockState(pos)));
        this.discard();

        if (block instanceof Fallable fallable) {
            fallable.onLand(this.level(), pos, this.blockState, stateOn, (FallingBlockEntity) (Object) this);
        }

        this.insanelib$restoreBlockEntityData(pos);
    }

    @Unique
    private void insanelib$restoreBlockEntityData(BlockPos pos) {
        if (this.blockData == null || !this.blockState.hasBlockEntity()) {
            return;
        }

        BlockEntity blockEntity = this.level().getBlockEntity(pos);
        if (blockEntity == null) {
            return;
        }

        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(blockEntity.problemPath(), InsaneLib.LOGGER)) {
            RegistryAccess registryAccess = this.level().registryAccess();
            TagValueOutput output = TagValueOutput.createWithContext(reporter, registryAccess);
            blockEntity.saveWithoutMetadata(output);
            CompoundTag merged = output.buildResult();
            this.blockData.forEach((name, tag) -> merged.put(name, tag.copy()));
            blockEntity.loadWithComponents(TagValueInput.create(reporter, registryAccess, merged));
        } catch (Exception exception) {
            InsaneLib.LOGGER.error("Failed to load block entity from falling block", exception);
        }

        blockEntity.setChanged();
    }

    @Unique
    private void insanelib$handleFailedPlacement(Block block, BlockPos pos) {
        if (this.dropItem && this.level() instanceof ServerLevel serverLevel && serverLevel.getGameRules().get(GameRules.ENTITY_DROPS)) {
            this.discard();
            this.callOnBrokenAfterFall(block, pos);
            this.spawnAtLocation(serverLevel, block);
        }
    }

    @Unique
    private Direction insanelib$selectRandomHorizontalDirection() {
        if (this.insanelib$directionFalling != null) {
            return this.getRandom().nextBoolean()
                    ? this.insanelib$directionFalling.getClockWise()
                    : this.insanelib$directionFalling.getCounterClockWise();
        }

        Direction[] horizontalDirections = Arrays.stream(Direction.values())
                .filter(dir -> dir.getAxis().isHorizontal() && dir != this.insanelib$movedFrom)
                .toArray(Direction[]::new);

        return horizontalDirections[this.getRandom().nextInt(horizontalDirections.length)];
    }

    public Entity insanelib$getSource() {
        return this.insanelib$source;
    }

    public void insanelib$setSource(Entity entity) {
        this.insanelib$source = entity;
    }
}
