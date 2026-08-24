package insane96mcp.insanelib.event;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

/**
 * Fired when a LivingEntity is damaged by a DamageSource whose damage type is in the
 * {@code insanelib:no_invincibility_frames} tag, right after its invincibility frames
 * (hurt cooldown) for that hit have been removed.
 */
public class NoInvincibilityFramesEvent extends LivingEvent {
    private final DamageSource source;

    public NoInvincibilityFramesEvent(LivingEntity entity, DamageSource source) {
        super(entity);
        this.source = source;
    }

    public DamageSource getSource() {
        return source;
    }
}
