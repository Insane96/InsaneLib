package insane96mcp.insanelib.setup;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

public class MobDetectionRangeAttribute extends RangedAttribute {
    public MobDetectionRangeAttribute(String descriptionId, double defaultValue, double min, double max) {
        super(descriptionId, defaultValue, min, max);
        this.setSentiment(Attribute.Sentiment.NEGATIVE);
    }
}
