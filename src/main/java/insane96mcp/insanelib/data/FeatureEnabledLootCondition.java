package insane96mcp.insanelib.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import insane96mcp.insanelib.core.feature.Feature;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public record FeatureEnabledLootCondition(String featureName) implements LootItemCondition {

    public static final MapCodec<FeatureEnabledLootCondition> MAP_CODEC = Codec.STRING
            .fieldOf("feature")
            .xmap(FeatureEnabledLootCondition::new, FeatureEnabledLootCondition::featureName);

    @Override
    public MapCodec<? extends LootItemCondition> codec() {
        return MAP_CODEC;
    }

    @Override
    public boolean test(LootContext lootContext) {
        return Feature.isEnabled(featureName);
    }

    public static LootItemCondition.Builder builder(String featureName) {
        return () -> new FeatureEnabledLootCondition(featureName);
    }
}
