package insane96mcp.insanelib.setup;

import com.mojang.serialization.Codec;
import insane96mcp.insanelib.InsaneLib;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ILDataComponents {
    public static final DeferredRegister<DataComponentType<?>> REGISTRY = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, InsaneLib.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Float>> KNOCKBACK_MULTIPLIER =
            REGISTRY.register("knockback_multiplier", () -> DataComponentType.<Float>builder()
                    .persistent(Codec.FLOAT)
                    .networkSynchronized(ByteBufCodecs.FLOAT)
                    .build());
}
