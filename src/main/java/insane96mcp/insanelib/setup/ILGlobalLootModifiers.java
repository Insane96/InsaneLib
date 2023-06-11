package insane96mcp.insanelib.setup;

import com.mojang.serialization.Codec;
import insane96mcp.insanelib.InsaneLib;
import insane96mcp.insanelib.data.lootmodifier.InjectLootTableModifier;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ILGlobalLootModifiers {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> REGISTRY = DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, InsaneLib.MOD_ID);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> INJECT_LOOT_MODIFIER = REGISTRY.register("inject_loot_table", InjectLootTableModifier.CODEC);
}
