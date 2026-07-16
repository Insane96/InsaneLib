package insane96mcp.insanelib.module.base;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.network.message.CreeperDataSyncMessage;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@LoadFeature(description = "Various fixes and improvements")
public class Fixes extends Feature {
	private static final Identifier LEADER_ZOMBIE_BONUS_ID = Identifier.withDefaultNamespace("leader_zombie_bonus");

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

	public static boolean shouldFixMobCrossbowFireworkShooting() {
		return Feature.isEnabled(Fixes.class) && fixMobCrossbowFireworkShooting;
	}

	public static boolean shouldFixSwimmersSwimmingAttribute() {
		return Feature.isEnabled(Fixes.class) && fixSwimmersSwimmingAttribute;
	}

	@SubscribeEvent
	public void onStartTracking(PlayerEvent.StartTracking event) {
		if (event.getEntity().level().isClientSide())
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
