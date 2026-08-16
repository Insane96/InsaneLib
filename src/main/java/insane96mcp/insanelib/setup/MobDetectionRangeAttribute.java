package insane96mcp.insanelib.setup;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

public class MobDetectionRangeAttribute extends RangedAttribute {
    public MobDetectionRangeAttribute(String descriptionId, double defaultValue, double min, double max) {
        super(descriptionId, defaultValue, min, max);
        this.setSentiment(Attribute.Sentiment.NEGATIVE);
    }

    @Override
    public MutableComponent toValueComponent(@Nullable AttributeModifier.Operation op, double value, TooltipFlag flag) {
        return Component.translatable("neoforge.value.percent", FORMAT.format(value * 100));
    }
}
