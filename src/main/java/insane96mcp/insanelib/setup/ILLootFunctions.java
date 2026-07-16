package insane96mcp.insanelib.setup;

import com.mojang.serialization.MapCodec;
import insane96mcp.insanelib.InsaneLib;
import insane96mcp.insanelib.data.function.EnchantRandomlyWeightlessFunction;
import insane96mcp.insanelib.data.function.EnchantWithTreasureFunction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ILLootFunctions {
    public static final DeferredRegister<MapCodec<? extends LootItemFunction>> REGISTRY =
            DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, InsaneLib.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends LootItemFunction>, MapCodec<EnchantRandomlyWeightlessFunction>> ENCHANT_RANDOMLY_WEIGHTLESS =
            REGISTRY.register("enchant_randomly_weightless", () -> EnchantRandomlyWeightlessFunction.MAP_CODEC);

    public static final DeferredHolder<MapCodec<? extends LootItemFunction>, MapCodec<EnchantWithTreasureFunction>> ENCHANT_WITH_TREASURE =
            REGISTRY.register("enchant_with_treasure", () -> EnchantWithTreasureFunction.MAP_CODEC);
}
