package insane96mcp.insanelib.module.base.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.setup.Config;
import insane96mcp.insanelib.utils.MCUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Set;

@Label(name = "Fixes", description = "A few fixes")
public class FixFeature extends Feature {

	private final ForgeConfigSpec.ConfigValue<Boolean> fixFollowRangeConfig;
	private final ForgeConfigSpec.ConfigValue<Boolean> removeZombiesBonusHealthConfig;
	private final ForgeConfigSpec.ConfigValue<Boolean> fixJumpMovementFactorConfig;

	public boolean fixFollowRange = true;
	public boolean removeZombiesBonusHealth = true;
	public boolean fixJumpMovementFactor = true;

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
				.comment("When affected by slowness the player can still jump really far away. When true, jumps length will be calculated base off player's movement speed.")
				.define("Fix Jump Movement Factor", this.fixJumpMovementFactor);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.fixFollowRange = this.fixFollowRangeConfig.get();
		this.removeZombiesBonusHealth = this.removeZombiesBonusHealthConfig.get();
		this.fixJumpMovementFactor = this.fixJumpMovementFactorConfig.get();
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onSpawn(EntityJoinWorldEvent event) {
		if (!this.isEnabled())
			return;

		fixFollowRange(event.getEntity());
		removeZombiesBonusHealth(event.getEntity());
	}

	private void removeZombiesBonusHealth(Entity entity) {
		if (!this.removeZombiesBonusHealth)
			return;

		if (!(entity instanceof ZombieEntity))
			return;

		ZombieEntity zombie = (ZombieEntity) entity;
		if (zombie.getAttribute(Attributes.MAX_HEALTH) == null)
			return;

		Set<AttributeModifier> modifiers = zombie.getAttribute(Attributes.MAX_HEALTH).getModifierListCopy();
		for (AttributeModifier attributeModifier : modifiers)
			if (attributeModifier.getName().equals("Leader zombie bonus"))
				zombie.getAttribute(Attributes.MAX_HEALTH).removeModifier(attributeModifier.getID());
	}

	private void fixFollowRange(Entity entity) {
		if (!this.fixFollowRange)
			 return;

		if (!(entity instanceof MobEntity))
			return;

		MobEntity mobEntity = (MobEntity) entity;

		ModifiableAttributeInstance followRangeAttribute = mobEntity.getAttribute(Attributes.FOLLOW_RANGE);
		if (followRangeAttribute != null) {
			for (PrioritizedGoal pGoal : mobEntity.targetSelector.goals) {
				if (pGoal.getGoal() instanceof NearestAttackableTargetGoal) {
					NearestAttackableTargetGoal<? extends LivingEntity> nearestAttackableTargetGoal = (NearestAttackableTargetGoal<? extends LivingEntity>) pGoal.getGoal();
					nearestAttackableTargetGoal.targetEntitySelector.setDistance(mobEntity.getAttributeValue(Attributes.FOLLOW_RANGE));
				}
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onUpdate(TickEvent.PlayerTickEvent event) {
		if (!this.isEnabled())
			return;

		if (event.phase != TickEvent.Phase.START)
			return;

		float baseJMF = 0.02f;
		if (event.player.isSprinting())
			baseJMF += 0.006f;

		double playerSpeedRatio = MCUtils.getMovementSpeedRatio(event.player);

		event.player.jumpMovementFactor = (float) (playerSpeedRatio * baseJMF);
	}
}
