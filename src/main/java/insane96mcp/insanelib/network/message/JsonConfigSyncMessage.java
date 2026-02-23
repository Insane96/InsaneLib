package insane96mcp.insanelib.network.message;

import insane96mcp.insanelib.InsaneLib;
import insane96mcp.insanelib.core.JsonFeature;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

public record JsonConfigSyncMessage(ResourceLocation syncType, String json) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<JsonConfigSyncMessage> TYPE =
            new CustomPacketPayload.Type<>(InsaneLib.location("json_config_sync"));

    public static final StreamCodec<FriendlyByteBuf, JsonConfigSyncMessage> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public JsonConfigSyncMessage decode(FriendlyByteBuf buf) {
            return new JsonConfigSyncMessage(buf.readResourceLocation(), buf.readUtf(Integer.MAX_VALUE));
        }

        @Override
        public void encode(FriendlyByteBuf buf, JsonConfigSyncMessage msg) {
            buf.writeResourceLocation(msg.syncType());
            buf.writeUtf(msg.json());
        }
    };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final JsonConfigSyncMessage payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            JsonFeature.SyncType syncType = JsonFeature.getSyncType(payload.syncType());
            if (syncType == null)
                throw new NullPointerException("Failed to get JsonConfigSync with id %s".formatted(payload.syncType()));
            syncType.onSync.accept(payload.json());
        });
    }

    public static void sync(ResourceLocation syncType, String json, ServerPlayer player) {
        if (!NetworkRegistry.hasChannel(player.connection, TYPE.id()))
            return;
        PacketDistributor.sendToPlayer(player, new JsonConfigSyncMessage(syncType, json));
    }
}
