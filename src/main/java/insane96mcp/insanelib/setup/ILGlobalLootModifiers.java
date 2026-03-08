package insane96mcp.insanelib.setup;

import com.mojang.serialization.MapCodec;
import insane96mcp.insanelib.InsaneLib;
import insane96mcp.insanelib.data.lootmodifier.*;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ILGlobalLootModifiers {
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> REGISTRY =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, InsaneLib.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<ReplaceLootModifier>> REPLACE_LOOT =
            REGISTRY.register("replace_loot", () -> ReplaceLootModifier.CODEC);

    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<InjectLootTableModifier>> INJECT_LOOT_TABLE =
            REGISTRY.register("inject_loot_table", () -> InjectLootTableModifier.CODEC);

    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<DropMultiplierModifier>> DROP_MULTIPLIER =
            REGISTRY.register("drop_multiplier", () -> DropMultiplierModifier.CODEC);

    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<LootPurgerModifier>> LOOT_PURGER =
            REGISTRY.register("loot_purger", () -> LootPurgerModifier.CODEC);

    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<DisenchantModifier>> DISENCHANT =
            REGISTRY.register("disenchant", () -> DisenchantModifier.CODEC);
}
