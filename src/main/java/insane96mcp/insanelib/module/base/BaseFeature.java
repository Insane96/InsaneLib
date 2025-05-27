package insane96mcp.insanelib.module.base;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.mixin.ServerLevelAccessor;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = "insanelib:base", canBeDisabled = false)
public class BaseFeature extends Feature {

    @Config(description = "If true, the game time will not advance if no players are online.")
    public static Boolean preventTimeTickingIfNoPlayersOnline = true;

    public BaseFeature(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    @SubscribeEvent
    public void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!preventTimeTickingIfNoPlayersOnline)
            return;
        MinecraftServer server = event.getEntity().getServer();
        //Event is triggered before the player is removed from the playerList
        if (server == null || server.getPlayerList().getPlayers().size() > 1)
            return;
        ((ServerLevelAccessor) server.overworld()).setTickTime(false);
    }

    @SubscribeEvent
    public void onPlayerLeave(PlayerEvent.PlayerLoggedInEvent event) {
        if (!preventTimeTickingIfNoPlayersOnline)
            return;
        MinecraftServer server = event.getEntity().getServer();
        if (server == null)
            return;
        ((ServerLevelAccessor) server.overworld()).setTickTime(true);
    }
}
