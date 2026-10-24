package insane96mcp.insanelib.util;

import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;

/**
 * Tracks the {@link LivingEntity} currently responsible for a knockback in progress (e.g. set by a mixin
 * wrapping {@code Player#attack} around each {@code LivingEntity#knockback} call), so that code reacting to
 * {@code LivingKnockBackEvent} can resolve the real attacker even when {@code getLastHurtByMob()} hasn't been
 * updated yet for the current hit (notably during sweep attacks, where knockback is applied before the hurt call).
 */
public class CurrentAttacker {
	private static final ThreadLocal<LivingEntity> CURRENT = new ThreadLocal<>();

	public static LivingEntity get() {
		return CURRENT.get();
	}

	public static void set(LivingEntity attacker) {
		CURRENT.set(attacker);
	}

	/**
	 * Resolves the attacker for a knockback affecting {@code victim}: prefers the in-progress attacker tracked
	 * via {@link #set(LivingEntity)}, falling back to {@link LivingEntity#getLastHurtByMob()} when none is set
	 * (e.g. mob melee attacks, not covered by an attacker-tracking mixin). The fallback only applies if the victim
	 * was hurt by that mob in the current tick, so that later knockbacks (e.g. explosions) aren't attributed to it.
	 */
	@Nullable
	public static LivingEntity resolve(LivingEntity victim) {
		LivingEntity current = get();
		if (current != null)
			return current;
		if (victim.getLastHurtByMobTimestamp() != victim.tickCount)
			return null;
		return victim.getLastHurtByMob();
	}
}
