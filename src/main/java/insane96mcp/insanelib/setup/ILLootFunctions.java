package insane96mcp.insanelib.setup;

import insane96mcp.insanelib.InsaneLib;
import insane96mcp.insanelib.data.function.EnchantRandomlyWeightlessFunction;
import insane96mcp.insanelib.data.function.EnchantWithTreasureFunction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ILLootFunctions {
    public static final DeferredRegister<LootItemFunctionType<?>> REGISTRY =
            DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, InsaneLib.MOD_ID);

    public static final DeferredHolder<LootItemFunctionType<?>, LootItemFunctionType<EnchantRandomlyWeightlessFunction>> ENCHANT_RANDOMLY_WEIGHTLESS =
            REGISTRY.register("enchant_randomly_weightless", () -> new LootItemFunctionType<>(EnchantRandomlyWeightlessFunction.CODEC));

    public static final DeferredHolder<LootItemFunctionType<?>, LootItemFunctionType<EnchantWithTreasureFunction>> ENCHANT_WITH_TREASURE =
            REGISTRY.register("enchant_with_treasure", () -> new LootItemFunctionType<>(EnchantWithTreasureFunction.CODEC));
}
