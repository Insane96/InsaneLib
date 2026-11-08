package insane96mcp.insanelib.module.base;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.setup.ILAttributes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;

@LoadFeature(description = "Adds a new `insanelib:off_ground_mining_speed` attribute to players, the multiplier applied to the mining speed when off ground (vanilla is 0.2, 1 means no penalty). Disabling this will prevent the attribute from working.")
public class OffGroundMiningSpeed extends Feature {
    public static void attribute(EntityAttributeModificationEvent event) {
        if (!event.has(EntityType.PLAYER, ILAttributes.OFF_GROUND_MINING_SPEED))
            event.add(EntityType.PLAYER, ILAttributes.OFF_GROUND_MINING_SPEED);
    }

    /**
     * Returns the value the mining speed is divided by when the player is off ground, or {@code original} if the attribute can't be used
     */
    public static float getDivider(Player player, float original) {
        if (!Feature.isEnabled(OffGroundMiningSpeed.class))
            return original;

        AttributeInstance attribute = player.getAttribute(ILAttributes.OFF_GROUND_MINING_SPEED);
        if (attribute == null || attribute.getValue() <= 0d)
            return original;

        return (float) (1d / attribute.getValue());
    }
}
