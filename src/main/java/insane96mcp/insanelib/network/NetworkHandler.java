package insane96mcp.insanelib.network;

import insane96mcp.insanelib.network.message.MessageCreeperDataSync;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetworkHandler {
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("2").optional();
        registrar.playToClient(MessageCreeperDataSync.TYPE, MessageCreeperDataSync.STREAM_CODEC, MessageCreeperDataSync::handle);
    }
}
