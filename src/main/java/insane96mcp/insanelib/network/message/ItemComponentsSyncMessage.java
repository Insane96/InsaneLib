package insane96mcp.insanelib.network.message;

import insane96mcp.insanelib.InsaneLib;
import insane96mcp.insanelib.module.base.items.ItemComponentsReloadListener;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

import java.util.HashMap;
import java.util.Map;

public record ItemComponentsSyncMessage(Map<ResourceLocation, DataComponentPatch> patches) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ItemComponentsSyncMessage> TYPE =
            new CustomPacketPayload.Type<>(InsaneLib.id("item_components_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemComponentsSyncMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, DataComponentPatch.STREAM_CODEC),
            ItemComponentsSyncMessage::patches,
            ItemComponentsSyncMessage::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final ItemComponentsSyncMessage payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ItemComponentsReloadListener.PATCHED_COMPONENTS.clear();
            for (Map.Entry<ResourceLocation, DataComponentPatch> entry : payload.patches().entrySet()) {
                if (!BuiltInRegistries.ITEM.containsKey(entry.getKey())) {
                    InsaneLib.LOGGER.warn("ItemComponents: received sync for unknown item '{}', skipping", entry.getKey());
                    continue;
                }
                Item item = BuiltInRegistries.ITEM.get(entry.getKey());
                DataComponentMap patched = PatchedDataComponentMap.fromPatch(item.components(), entry.getValue());
                ItemComponentsReloadListener.PATCHED_COMPONENTS.put(item, patched);
            }
        });
    }

    public static void sync(Map<ResourceLocation, DataComponentPatch> patches, ServerPlayer player) {
        if (!NetworkRegistry.hasChannel(player.connection, TYPE.id()))
            return;
        PacketDistributor.sendToPlayer(player, new ItemComponentsSyncMessage(patches));
    }
}
