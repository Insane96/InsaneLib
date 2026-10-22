package insane96mcp.insanelib.client;

import insane96mcp.insanelib.module.base.items.ItemComponentsReloadListener;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

import java.util.Map;

public class ItemComponentsClientHandler {
    /**
     * Drops the patches of the previous server, as servers without InsaneLib never send them. The login packet is
     * always handled before the ItemComponentsSyncMessage of the new server.
     * Singleplayer / LAN host are skipped as the patches are shared with the integrated server, that already computed them.
     */
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        if (event.getConnection().isMemoryConnection())
            return;
        ItemComponentsReloadListener.PATCHED_COMPONENTS = Map.of();
    }
}
