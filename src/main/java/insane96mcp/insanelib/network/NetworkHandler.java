package insane96mcp.insanelib.network;

import insane96mcp.insanelib.InsaneLib;
import insane96mcp.insanelib.network.message.EntityModNBTDataSync;
import insane96mcp.insanelib.network.message.JsonConfigSyncMessage;
import insane96mcp.insanelib.network.message.MessageCreeperDataSync;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = Integer.toString(2);
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            InsaneLib.location("network_channel"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            NetworkRegistry.acceptMissingOr(PROTOCOL_VERSION)
    );

    static int index = 0;

    public static void init() {
        CHANNEL.registerMessage(++index, MessageCreeperDataSync.class, MessageCreeperDataSync::encode, MessageCreeperDataSync::decode, MessageCreeperDataSync::handle);
        CHANNEL.registerMessage(++index, JsonConfigSyncMessage.class, JsonConfigSyncMessage::encode, JsonConfigSyncMessage::decode, JsonConfigSyncMessage::handle);
        CHANNEL.registerMessage(++index, EntityModNBTDataSync.class, EntityModNBTDataSync::encode, EntityModNBTDataSync::decode, EntityModNBTDataSync::handle);
    }
}
