package insane96mcp.insanelib.module.base;

import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.setup.ILDataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;

@LoadFeature(description = "A new item component insanelib:knockback_multiplier (0~1) that can reduce the knockback from an item", canBeDisabled = false)
public class KnockbackMultiplier {
    @SubscribeEvent
    public void onLivingKnockback(LivingKnockBackEvent event) {
        LivingEntity attacker = event.getEntity().getLastHurtByMob();
        if (attacker == null)
            return;
        Float multiplier = attacker.getMainHandItem().get(ILDataComponents.KNOCKBACK_MULTIPLIER.get());
        if (multiplier == null)
            return;
        event.setStrength(event.getStrength() * multiplier);
    }
}
