package insane96mcp.insanelib.module.base;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.mixin.accessor.CreeperAccessor;
import insane96mcp.insanelib.network.message.MessageCreeperDataSync;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@LoadFeature(module = "insanelib:base", description = "Various fixes and improvements")
public class FixesFeature extends Feature {
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

	public static boolean shouldFixFollowRange() {
		return Feature.isEnabled(FixesFeature.class) && fixFollowRange;
	}

	public static boolean shouldFixSwimmersSwimmingAttribute() {
		return Feature.isEnabled(FixesFeature.class) && fixSwimmersSwimmingAttribute;
	}

	@SubscribeEvent
	public void onStartTracking(PlayerEvent.StartTracking event) {
		if (event.getEntity().level().isClientSide)
			return;
		if (!(event.getTarget() instanceof Creeper creeper))
			return;
		MessageCreeperDataSync msg = new MessageCreeperDataSync(
				creeper.getId(),
				((CreeperAccessor) creeper).getMaxSwell(),
				((CreeperAccessor) creeper).getExplosionRadius()
		);
		PacketDistributor.sendToPlayer((ServerPlayer) event.getEntity(), msg);
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

	public static float getFlyingSpeed(Player player, float original) {
		if (!Feature.isEnabled(FixesFeature.class)
				|| !fixAirSpeed$enabled)
			return original;

		double playerSpeedRatio = MCUtils.getMovementSpeedRatio(player);

		if (playerSpeedRatio > 1d && fixAirSpeed$slowdownOnly)
			return original;

		return (float) (playerSpeedRatio * original);
	}
}
