package insane96mcp.insanelib.setup;

import insane96mcp.insanelib.InsaneLib;
import insane96mcp.insanelib.data.criterion.BlockBrokenTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ILCriteriaTriggers {
    @SuppressWarnings("rawtypes")
    public static final DeferredRegister REGISTRY =
            DeferredRegister.create(BuiltInRegistries.TRIGGER_TYPES, InsaneLib.MOD_ID);

    @SuppressWarnings("unchecked")
    public static final Supplier<BlockBrokenTrigger> BLOCK_BROKEN =
            REGISTRY.register("block_broken", BlockBrokenTrigger::new);

    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        BLOCK_BROKEN.get().trigger(player, event.getState());
    }
}
