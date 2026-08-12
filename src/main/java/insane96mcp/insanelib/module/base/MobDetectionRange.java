package insane96mcp.insanelib.module.base;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.setup.ILAttributes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

@LoadFeature(description = "Adds a new `insanelib:mob_detection_range` attribute to players (0~1), that reduces in percentage the range at which mobs can detect/see them. Hooks into LivingVisibilityEvent with LOWEST priority so it modifies the final visibility value.")
public class MobDetectionRange extends Feature {
    public static void attribute(EntityAttributeModificationEvent event) {
        if (!event.has(EntityType.PLAYER, ILAttributes.MOB_DETECTION_RANGE))
            event.add(EntityType.PLAYER, ILAttributes.MOB_DETECTION_RANGE);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onVisibility(LivingEvent.LivingVisibilityEvent event) {
        if (!this.isEnabled())
            return;

        LivingEntity target = event.getEntity();
        AttributeInstance attributeInstance = target.getAttribute(ILAttributes.MOB_DETECTION_RANGE);
        if (attributeInstance == null)
            return;

        double reduction = target.getAttributeValue(ILAttributes.MOB_DETECTION_RANGE);
        if (reduction <= 0d)
            return;

        event.modifyVisibility(1d - reduction);
    }
}
