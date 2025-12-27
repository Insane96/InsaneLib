package insane96mcp.insanelib.module.base;

import insane96mcp.insanelib.InsaneLib;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Optional;
import java.util.Set;

@LoadFeature(module = "insanelib:base", description = "Various fixes and improvements")
public class FixesFeature extends Feature {
	public static TagKey<EntityType<?>> FIX_FOLLOW_RANGE = TagKey.create(Registries.ENTITY_TYPE, InsaneLib.location("fix_follow_range"));

	@Config(description = "If true, mobs will have their follow range fixed. https://bugs.mojang.com/browse/MC-145656. Only affects entities in `insanelib:fix_follow_range` entity type tag (all vanilla mobs by default) and entities that use the NearestAttackableTargetGoal goal.")
	public static Boolean fixFollowRange = true;

	@Config(description = "Removes the random bonus health given to Leader Zombies. In vanilla it's useless since doesn't work. https://minecraft.wiki/Attribute#Vanilla_modifiers")
	public static Boolean removeZombiesBonusHealth = true;

	@Config(description = "When affected by slowness the player can still jump really far away. When true, jumps length will be calculated based off player's movement speed.")
	public static Boolean fixJumpMovementFactor = true;

	@Config(description = "The fix for Jump Movement Factor is applied only when the player is slowed down. If false, the player will jump really farther when going faster.")
	public static Boolean fixJumpMovementFactorSlowdownOnly = true;

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

		@SuppressWarnings("ConstantConditions") Set<AttributeModifier> modifiers = zombie.getAttribute(Attributes.MAX_HEALTH).getModifiers();
		for (AttributeModifier attributeModifier : modifiers)
			if (attributeModifier.getName().equals("Leader zombie bonus"))
                //noinspection DataFlowIssue
                zombie.getAttribute(Attributes.MAX_HEALTH).removeModifier(attributeModifier.getId());
	}

	private void fixFollowRange(Entity entity) {
		if (!fixFollowRange
				|| !(entity instanceof Mob mobEntity)
				|| !mobEntity.getType().is(FIX_FOLLOW_RANGE))
			 return;

		AttributeInstance followRangeAttribute = mobEntity.getAttribute(Attributes.FOLLOW_RANGE);
        if (followRangeAttribute == null)
            return;

        for (WrappedGoal pGoal : mobEntity.targetSelector.availableGoals) {
            if (pGoal.getGoal() instanceof NearestAttackableTargetGoal<? extends LivingEntity> nearestAttackableTargetGoal) {
                nearestAttackableTargetGoal.targetConditions.range(mobEntity.getAttributeValue(Attributes.FOLLOW_RANGE));
            }
        }
    }

	public static Optional<Float> getFlyingSpeed(Player player) {
		if (!Feature.isEnabled(FixesFeature.class)
				|| !fixJumpMovementFactor)
			return Optional.empty();

		float baseFlyingSpeed = 0.02f;
		if (player.isSprinting())
			baseFlyingSpeed += 0.006f;

		double playerSpeedRatio = MCUtils.getMovementSpeedRatio(player);

		if (playerSpeedRatio > 1d && fixJumpMovementFactorSlowdownOnly)
			return Optional.empty();

		return Optional.of((float) (playerSpeedRatio * baseFlyingSpeed));
	}
}
