package insane96mcp.insanelib.network;

import insane96mcp.insanelib.network.message.CreeperDataSyncMessage;
import insane96mcp.insanelib.network.message.JsonConfigSyncMessage;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetworkHandler {
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("2").optional();
        registrar.playToClient(CreeperDataSyncMessage.TYPE, CreeperDataSyncMessage.STREAM_CODEC, CreeperDataSyncMessage::handle);
        registrar.playToClient(JsonConfigSyncMessage.TYPE, JsonConfigSyncMessage.STREAM_CODEC, JsonConfigSyncMessage::handle);
    }
}
