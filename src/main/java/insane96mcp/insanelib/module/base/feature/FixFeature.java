package insane96mcp.insanelib.module.base.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Objects;
import java.util.Set;

@Label(name = "Fixes", description = "A few fixes")
public class FixFeature extends Feature {

	@Config
	@Label(name = "Fix Follow Range", description = "If true, mobs will have their follow range fixed. https://bugs.mojang.com/browse/MC-145656")
	public static boolean fixFollowRange = true;
	@Config
	@Label(name = "Remove Zombies Bonus Health", description = "Removes the random bonus health given to Leader Zombies. In vanilla it's useless since doesn't work. https://minecraft.fandom.com/wiki/Attribute#Vanilla_modifiers")
	public static boolean removeZombiesBonusHealth = true;
	@Config
	@Label(name = "Fix Flying Speed", description = "When affected by slowness the player can still jump really far away. When true, jumps length will be calculated based off player's movement speed.")
	public static boolean fixFlyingSpeed = true;
	@Config
	@Label(name = "Fix Jump Movement Factor Slowdown Only", description = "The fix for Jump Movement Factor is applied only when the player is slowed down. If false, the player will jump really farther when going faster.")
	public static boolean slowdownOnly = true;

	public FixFeature(Module module) {
		super(module);
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onSpawn(EntityJoinLevelEvent event) {
		if (!this.isEnabled())
			return;

		fixFollowRange(event.getEntity());
		removeZombiesBonusHealth(event.getEntity());
	}

	private void removeZombiesBonusHealth(Entity entity) {
		if (!removeZombiesBonusHealth
				|| !(entity instanceof Zombie zombie)
				|| zombie.getAttribute(Attributes.MAX_HEALTH) == null)
			return;

		Set<AttributeModifier> modifiers = zombie.getAttribute(Attributes.MAX_HEALTH).getModifiers();
		for (AttributeModifier attributeModifier : modifiers)
			if (attributeModifier.getName().equals("Leader zombie bonus"))
				Objects.requireNonNull(zombie.getAttribute(Attributes.MAX_HEALTH)).removeModifier(attributeModifier.getId());
	}

	private void fixFollowRange(Entity entity) {
		if (!fixFollowRange
				|| !(entity instanceof Mob mobEntity))
			 return;

		AttributeInstance followRangeAttribute = mobEntity.getAttribute(Attributes.FOLLOW_RANGE);
		if (followRangeAttribute != null) {
			for (WrappedGoal pGoal : mobEntity.targetSelector.availableGoals) {
				if (pGoal.getGoal() instanceof NearestAttackableTargetGoal<? extends LivingEntity> nearestAttackableTargetGoal) {
					nearestAttackableTargetGoal.targetConditions.range(mobEntity.getAttributeValue(Attributes.FOLLOW_RANGE));
				}
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onUpdate(TickEvent.PlayerTickEvent event) {
		if (!this.isEnabled()
				|| !fixFlyingSpeed
				|| event.phase != TickEvent.Phase.START)
			return;

		float baseFlyingSpeed = 0.02f;
		if (event.player.isSprinting())
			baseFlyingSpeed += 0.006f;

		double playerSpeedRatio = MCUtils.getMovementSpeedRatio(event.player);

		if (playerSpeedRatio > 1d && this.slowdownOnly)
			return;

		event.player.flyingSpeed = (float) (playerSpeedRatio * baseFlyingSpeed);
	}
}
