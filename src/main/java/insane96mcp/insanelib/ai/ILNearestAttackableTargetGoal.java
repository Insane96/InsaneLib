package insane96mcp.insanelib.ai;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.function.Predicate;

/**
 * More configurable version of NearestAttackableTargetGoal with xRay and instant targeting
 */
public class ILNearestAttackableTargetGoal<T extends LivingEntity> extends TargetGoal {
	protected final Class<T> targetClass;
	protected int targetChance;
	protected LivingEntity nearestTarget;

	public TargetingConditions targetEntitySelector;

	public ILNearestAttackableTargetGoal(Mob goalOwnerIn, Class<T> targetClassIn, boolean mustSee) {
		this(goalOwnerIn, targetClassIn, mustSee, false);
	}

	public ILNearestAttackableTargetGoal(Mob goalOwnerIn, Class<T> targetClassIn, boolean mustSee, boolean mustReach) {
		this(goalOwnerIn, targetClassIn, mustSee, mustReach, null);
	}

	public ILNearestAttackableTargetGoal(Mob goalOwnerIn, Class<T> targetClassIn, boolean mustSee, boolean mustReach, @Nullable Predicate<LivingEntity> targetPredicate) {
		super(goalOwnerIn, mustSee, mustReach);
		this.targetClass = targetClassIn;
		this.targetChance = 10;
		this.setFlags(EnumSet.of(Flag.TARGET));
		this.targetEntitySelector = TargetingConditions.forCombat().range(this.getFollowDistance()).selector(targetPredicate);
	}

	/**
	 * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
	 * method as well.
	 */
	public boolean canUse() {
		if (this.targetChance > 0 && this.mob.getRandom().nextInt(this.targetChance) != 0) {
			return false;
		}
		else {
			this.findTarget();
			return this.nearestTarget != null;
		}
	}

	protected AABB getTargetSearchArea(double targetDistance) {
		return this.mob.getBoundingBox().inflate(targetDistance, targetDistance, targetDistance);
	}

	protected void findTarget() {
		if (this.targetClass != Player.class && this.targetClass != ServerPlayer.class) {
			this.nearestTarget = this.mob.level.getNearestEntity(this.targetClass, this.targetEntitySelector, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ(), this.getTargetSearchArea(this.getFollowDistance()));
		}
		else {
			this.nearestTarget = this.mob.level.getNearestPlayer(this.targetEntitySelector, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
		}

	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void start() {
		this.mob.setTarget(this.nearestTarget);
		super.start();
	}

	/**
	 * Entities will no longer have 1 in 10 chance to target an entity. Same as calling setTargetChance(0)
	 */
	public ILNearestAttackableTargetGoal<T> setInstaTarget() {
		return this.setTargetChance(0);
	}

	public ILNearestAttackableTargetGoal<T> setTargetChance(int targetChance) {
		this.targetChance = reducedTickDelay(targetChance);
		return this;
	}

	/**
	 * Let the entity see through walls (X-Ray)
	 */
	public ILNearestAttackableTargetGoal<T> setIgnoreLineOfSight() {
		this.targetEntitySelector.ignoreLineOfSight();
		return this;
	}
}
