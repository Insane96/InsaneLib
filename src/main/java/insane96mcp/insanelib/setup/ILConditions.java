package insane96mcp.insanelib.setup;

import com.mojang.serialization.MapCodec;
import insane96mcp.insanelib.InsaneLib;
import insane96mcp.insanelib.data.FeatureEnabledCondition;
import insane96mcp.insanelib.data.FeatureEnabledLootCondition;
import insane96mcp.insanelib.data.condition.BlockTagCondition;
import insane96mcp.insanelib.data.condition.KillerHasAdvancementCondition;
import insane96mcp.insanelib.data.condition.NonPlayerArisedDropCondition;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ILConditions {
    public static final DeferredRegister<MapCodec<? extends ICondition>> CONDITION_CODECS =
            DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, InsaneLib.MOD_ID);

    public static final DeferredRegister<MapCodec<? extends LootItemCondition>> LOOT_CONDITIONS =
            DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, InsaneLib.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<FeatureEnabledCondition>> FEATURE_ENABLED =
            CONDITION_CODECS.register("feature_enabled", () -> FeatureEnabledCondition.CODEC);

    public static final DeferredHolder<MapCodec<? extends LootItemCondition>, MapCodec<FeatureEnabledLootCondition>> FEATURE_ENABLED_LOOT =
            LOOT_CONDITIONS.register("feature_enabled", () -> FeatureEnabledLootCondition.MAP_CODEC);

    public static final DeferredHolder<MapCodec<? extends LootItemCondition>, MapCodec<BlockTagCondition>> BLOCK_TAG_MATCH =
            LOOT_CONDITIONS.register("block_tag_match", () -> BlockTagCondition.MAP_CODEC);

    public static final DeferredHolder<MapCodec<? extends LootItemCondition>, MapCodec<KillerHasAdvancementCondition>> KILLER_HAS_ADVANCEMENT =
            LOOT_CONDITIONS.register("killer_has_advancement", () -> KillerHasAdvancementCondition.MAP_CODEC);

    public static final DeferredHolder<MapCodec<? extends LootItemCondition>, MapCodec<NonPlayerArisedDropCondition>> NON_PLAYER_ARISED_DROP =
            LOOT_CONDITIONS.register("non_player_arised_drop", () -> NonPlayerArisedDropCondition.MAP_CODEC);
}
