package insane96mcp.insanelib.module.base;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.event.NoInvincibilityFramesEvent;
import insane96mcp.insanelib.setup.ILTags;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@LoadFeature(description = "Damage sources whose damage type is in the insanelib:no_invincibility_frames tag will not grant invincibility frames (hurt cooldown) to the entity they damage", canBeDisabled = false)
public class NoInvincibilityFrames extends Feature {
    @SubscribeEvent
    public void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        if (!event.getSource().is(ILTags.DamageTypes.NO_INVINCIBILITY_FRAMES))
            return;

        // NeoForge requires at least 1 tick. Vanilla only blocks damage while invulnerableTime > 10, so 1 means no invincibility frames
        event.setInvulnerabilityTicks(1);
        NeoForge.EVENT_BUS.post(new NoInvincibilityFramesEvent(event.getEntity(), event.getSource()));
    }
}
