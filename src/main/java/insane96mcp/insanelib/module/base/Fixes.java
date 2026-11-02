package insane96mcp.insanelib.module.base;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.network.message.CreeperDataSyncMessage;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@LoadFeature(description = "Various fixes and improvements")
public class Fixes extends Feature {
	private static final ResourceLocation LEADER_ZOMBIE_BONUS_ID = ResourceLocation.withDefaultNamespace("leader_zombie_bonus");

	@Config(description = "If true, mobs will have their follow range fixed. https://bugs.mojang.com/browse/MC-145656. Only affects entities in `insanelib:fix_follow_range` entity type tag (all vanilla mobs by default) and entities that use the NearestAttackableTargetGoal goal.")
	public static Boolean fixFollowRange = true;

	@Config(description = "Makes drowned and fishes use the swim speed attribute (neoforge:swim_speed) instead of movement speed.")
	public static Boolean fixSwimmersSwimmingAttribute = true;

	@Config(description = "Removes the random bonus health given to Leader Zombies. In vanilla it's useless since doesn't work. https://minecraft.wiki/Attribute#Vanilla_modifiers")
	public static Boolean removeZombiesBonusHealth = true;

	@Config(description = "When affected by slowness the player can still jump really far away. When true, jumps length will be calculated based off player's movement speed.")
	public static Boolean fixAirSpeed$enabled = true;

	@Config(description = "The fix for Air Speed is applied only when the player is slowed down. If false, the player will jump really farther when going faster.")
	public static Boolean fixAirSpeed$slowdownOnly = true;

	@Config(description = "If true, player's sprinting jump impulse will be reduced based off slowdown (basically, when you have 0 movement speed, if this is true, you can no longer jump forward while sprinting).")
	public static Boolean fixAirSpeed$sprintingJumpSlowdown = true;

	@Config(description = "If true, https://bugs.mojang.com/browse/MC/issues/MC-145114 will be fixed.")
	public static Boolean fixMobCrossbowFireworkShooting = true;

	@Config(description = "If true, falling blocks going through an End portal will no longer be duplicated.")
	public static Boolean fixFallingBlockDupe = true;

	@Config(description = "Prevents endermen from teleporting inside full blocks that can be passed through (e.g. cobwebs, powder snow, or leaves with mods that make them pass through), where they can get stuck. Also prevents them from teleporting on top of such blocks if they would fall through them.")
	public static Boolean fixEndermanTeleportingInsideBlocks = true;

	public static boolean shouldFixFallingBlockDupe() {
		return Feature.isEnabled(Fixes.class) && fixFallingBlockDupe;
	}

	public static boolean shouldFixMobCrossbowFireworkShooting() {
		return Feature.isEnabled(Fixes.class) && fixMobCrossbowFireworkShooting;
	}

	public static boolean shouldFixFollowRange() {
		return Feature.isEnabled(Fixes.class) && fixFollowRange;
	}

	public static boolean shouldFixSwimmersSwimmingAttribute() {
		return Feature.isEnabled(Fixes.class) && fixSwimmersSwimmingAttribute;
	}

	@SubscribeEvent
	public void onStartTracking(PlayerEvent.StartTracking event) {
		if (event.getEntity().level().isClientSide)
			return;
		if (!(event.getTarget() instanceof Creeper creeper))
			return;
		CreeperDataSyncMessage.syncCreeperToPlayer(creeper, (ServerPlayer) event.getEntity());
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onSpawn(EntityJoinLevelEvent event) {
		if (!this.isEnabled())
			return;

		removeZombiesBonusHealth(event.getEntity());
	}

	private void removeZombiesBonusHealth(Entity entity) {
		if (!removeZombiesBonusHealth
				|| !(entity instanceof Zombie zombie))
			return;

		AttributeInstance attributeInstance = zombie.getAttribute(Attributes.MAX_HEALTH);
		if (attributeInstance == null)
			return;
		attributeInstance.removeModifier(LEADER_ZOMBIE_BONUS_ID);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onEndermanTeleport(EntityTeleportEvent.EnderEntity event) {
		if (!this.isEnabled()
				|| !fixEndermanTeleportingInsideBlocks
				|| !(event.getEntity() instanceof EnderMan enderMan))
			return;

		if (wouldLandInPassThroughBlock(enderMan, event.getTargetX(), event.getTargetY(), event.getTargetZ()))
			event.setCanceled(true);
	}

	/**
	 * Follows {@link net.minecraft.world.entity.LivingEntity#randomTeleport} to find where the enderman would land.
	 * Full blocks with collision are ignored as vanilla already rejects them, so without pass through blocks nothing changes for solid blocks.
	 */
	private static boolean wouldLandInPassThroughBlock(EnderMan enderMan, double x, double y, double z) {
		Level level = enderMan.level();
		BlockPos.MutableBlockPos floorPos = BlockPos.containing(x, y, z).mutable();
		double landingY = y;
		boolean foundFloor = false;
		while (!foundFloor && floorPos.getY() > level.getMinBuildHeight()) {
			floorPos.move(Direction.DOWN);
			if (level.getBlockState(floorPos).blocksMotion())
				foundFloor = true;
			else
				landingY--;
		}
		// Vanilla fails the teleport on its own
		if (!foundFloor)
			return false;

		// Blocks with a context dependent collision can count as solid, while still letting the enderman fall through
		BlockState floorState = level.getBlockState(floorPos);
		if (Block.isShapeFullBlock(floorState.getShape(level, floorPos))
				&& floorState.getCollisionShape(level, floorPos, CollisionContext.of(enderMan)).isEmpty())
			return true;

		AABB landingBox = enderMan.getDimensions(enderMan.getPose()).makeBoundingBox(x, landingY, z);
		return BlockPos.betweenClosedStream(landingBox)
				.anyMatch(pos -> Block.isShapeFullBlock(level.getBlockState(pos).getShape(level, pos)));
	}

	public static float getFlyingSpeed(Player player, float original) {
		if (!Feature.isEnabled(Fixes.class)
				|| !fixAirSpeed$enabled)
			return original;

		double playerSpeedRatio = MCUtils.getMovementSpeedRatio(player);

		if (playerSpeedRatio > 1d && fixAirSpeed$slowdownOnly)
			return original;

		return (float) (playerSpeedRatio * original);
	}
}
