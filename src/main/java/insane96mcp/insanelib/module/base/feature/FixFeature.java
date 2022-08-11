package insane96mcp.insanelib.module.base.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.setup.Config;
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
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Set;

@Label(name = "Fixes", description = "A few fixes")
public class FixFeature extends Feature {

	private final ForgeConfigSpec.ConfigValue<Boolean> fixFollowRangeConfig;
	private final ForgeConfigSpec.ConfigValue<Boolean> removeZombiesBonusHealthConfig;
	private final ForgeConfigSpec.ConfigValue<Boolean> fixJumpMovementFactorConfig;
	private final ForgeConfigSpec.ConfigValue<Boolean> slowdownOnlyConfig;

	public boolean fixFollowRange = true;
	public boolean removeZombiesBonusHealth = true;
	public boolean fixJumpMovementFactor = true;
	public boolean slowdownOnly = true;

	public FixFeature(Module module) {
		super(Config.builder, module);
		Config.builder.comment(this.getDescription()).push(this.getName());
		this.fixFollowRangeConfig = Config.builder
				.comment("If true, mobs will have their follow range fixed. https://bugs.mojang.com/browse/MC-145656")
				.define("Fix Follow Range", this.fixFollowRange);
		this.removeZombiesBonusHealthConfig = Config.builder
				.comment("Removes the random bonus health given to Leader Zombies. In vanilla it's useless since doesn't work. https://minecraft.fandom.com/wiki/Attribute#Vanilla_modifiers")
				.define("Remove Zombies Bonus Health", this.removeZombiesBonusHealth);
		this.fixJumpMovementFactorConfig = Config.builder
				.comment("When affected by slowness the player can still jump really far away. When true, jumps length will be calculated based off player's movement speed.")
				.define("Fix Jump Movement Factor", this.fixJumpMovementFactor);
		this.slowdownOnlyConfig = Config.builder
				.comment("The fix for Jump Movement Factor is applied only when the player is slowed down. If false, the player will jump really farther when going faster.")
				.define("Fix Jump Movement Factor Slowdown Only", this.slowdownOnly);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.fixFollowRange = this.fixFollowRangeConfig.get();
		this.removeZombiesBonusHealth = this.removeZombiesBonusHealthConfig.get();
		this.fixJumpMovementFactor = this.fixJumpMovementFactorConfig.get();
		this.slowdownOnly = this.slowdownOnlyConfig.get();
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onSpawn(EntityJoinLevelEvent event) {
		if (!this.isEnabled())
			return;

		fixFollowRange(event.getEntity());
		removeZombiesBonusHealth(event.getEntity());
	}

	private void removeZombiesBonusHealth(Entity entity) {
		if (!this.removeZombiesBonusHealth)
			return;

		if (!(entity instanceof Zombie zombie))
			return;

		if (zombie.getAttribute(Attributes.MAX_HEALTH) == null)
			return;

		Set<AttributeModifier> modifiers = zombie.getAttribute(Attributes.MAX_HEALTH).getModifiers();
		for (AttributeModifier attributeModifier : modifiers)
			if (attributeModifier.getName().equals("Leader zombie bonus"))
				zombie.getAttribute(Attributes.MAX_HEALTH).removeModifier(attributeModifier.getId());
	}

	private void fixFollowRange(Entity entity) {
		if (!this.fixFollowRange)
			 return;

		if (!(entity instanceof Mob mobEntity))
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
		if (!this.isEnabled())
			return;

		if (!this.fixJumpMovementFactor)
			return;

		if (event.phase != TickEvent.Phase.START)
			return;

		float baseJMF = 0.02f;
		if (event.player.isSprinting())
			baseJMF += 0.006f;

		double playerSpeedRatio = MCUtils.getMovementSpeedRatio(event.player);

		if (playerSpeedRatio > 1d && this.slowdownOnly)
			return;

		event.player.flyingSpeed = (float) (playerSpeedRatio * baseJMF);
	}
}
