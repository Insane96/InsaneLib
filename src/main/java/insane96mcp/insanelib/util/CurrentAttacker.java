package insane96mcp.insanelib.util;

import net.minecraft.world.entity.LivingEntity;

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
	 * (e.g. knockback from explosions or other sources not covered by an attacker-tracking mixin).
	 */
	public static LivingEntity resolve(LivingEntity victim) {
		LivingEntity current = get();
		return current != null ? current : victim.getLastHurtByMob();
	}
}
