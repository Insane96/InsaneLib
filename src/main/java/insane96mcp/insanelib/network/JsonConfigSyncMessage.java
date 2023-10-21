package insane96mcp.insanelib.network;

import insane96mcp.insanelib.base.JsonFeature;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class JsonConfigSyncMessage {
    ResourceLocation jsonConfigSync;
    String json;
    int jsonSize;

    public JsonConfigSyncMessage(ResourceLocation jsonConfigSync, String json) {
        this.jsonConfigSync = jsonConfigSync;
        this.json = json;
        this.jsonSize = json.length();
    }

    public static void encode(JsonConfigSyncMessage pkt, FriendlyByteBuf buf) {
        buf.writeResourceLocation(pkt.jsonConfigSync);
        buf.writeInt(pkt.jsonSize);
        buf.writeBytes(pkt.json.getBytes());
    }

    public static JsonConfigSyncMessage decode(FriendlyByteBuf buf) {
        ResourceLocation jsonConfigSync = buf.readResourceLocation();
        int size = buf.readInt();
        byte[] jsonByte = new byte[size];
        buf.readBytes(jsonByte);
        String json = new String(jsonByte);
        return new JsonConfigSyncMessage(jsonConfigSync, json);
    }

    public static void handle(final JsonConfigSyncMessage message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            JsonFeature.SyncType syncType1 = JsonFeature.getSyncType(message.jsonConfigSync);
            if (syncType1 == null)
                throw new NullPointerException("Failed to get JsonConfigSync with id %s".formatted(message.jsonConfigSync));
            syncType1.onSync.accept(message.json);
        });
        ctx.get().setPacketHandled(true);
    }

    public static void sync(ResourceLocation jsonConfigSync, String json, ServerPlayer player) {
        Object msg = new JsonConfigSyncMessage(jsonConfigSync, json);
        NetworkHandler.CHANNEL.sendTo(msg, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }
}
