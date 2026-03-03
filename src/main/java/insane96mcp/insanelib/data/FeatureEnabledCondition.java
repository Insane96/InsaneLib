package insane96mcp.insanelib.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import insane96mcp.insanelib.core.feature.Feature;
import net.neoforged.neoforge.common.conditions.ICondition;

public record FeatureEnabledCondition(String featureName) implements ICondition {

    public static final MapCodec<FeatureEnabledCondition> CODEC = Codec.STRING
            .fieldOf("feature")
            .xmap(FeatureEnabledCondition::new, FeatureEnabledCondition::featureName);

    @Override
    public boolean test(IContext context) {
        return Feature.isEnabled(featureName);
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
