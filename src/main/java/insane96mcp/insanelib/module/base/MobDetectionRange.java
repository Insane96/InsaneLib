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

@LoadFeature(description = "Adds a new `insanelib:mob_detection_range` attribute to living entities (-Double.MAX_VALUE~1, default 0), that reduces (negative values) or increases (positive values) in percentage the range at which other entities can detect/see them. Hooks into LivingVisibilityEvent with LOWEST priority so it modifies the final visibility value.")
public class MobDetectionRange extends Feature {
    public static void attribute(EntityAttributeModificationEvent event) {
        for (EntityType<? extends LivingEntity> entityType : event.getTypes()) {
            if (!event.has(entityType, ILAttributes.MOB_DETECTION_RANGE))
                event.add(entityType, ILAttributes.MOB_DETECTION_RANGE);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onVisibility(LivingEvent.LivingVisibilityEvent event) {
        if (!this.isEnabled())
            return;

        LivingEntity target = event.getEntity();
        AttributeInstance attributeInstance = target.getAttribute(ILAttributes.MOB_DETECTION_RANGE);
        if (attributeInstance == null)
            return;

        double value = target.getAttributeValue(ILAttributes.MOB_DETECTION_RANGE);
        if (value == 0d)
            return;

        event.modifyVisibility(1d + value);
    }
}
