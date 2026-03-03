package insane96mcp.insanelib.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.setup.ILConditions;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public record FeatureEnabledLootCondition(String featureName) implements LootItemCondition {

    public static final MapCodec<FeatureEnabledLootCondition> CODEC = Codec.STRING
            .fieldOf("feature")
            .xmap(FeatureEnabledLootCondition::new, FeatureEnabledLootCondition::featureName);

    @Override
    public LootItemConditionType getType() {
        return ILConditions.FEATURE_ENABLED_LOOT.get();
    }

    @Override
    public boolean test(LootContext lootContext) {
        return Feature.isEnabled(featureName);
    }

    public static LootItemCondition.Builder builder(String featureName) {
        return () -> new FeatureEnabledLootCondition(featureName);
    }
}
