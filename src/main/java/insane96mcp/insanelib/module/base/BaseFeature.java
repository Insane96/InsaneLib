package insane96mcp.insanelib.module.base;

import insane96mcp.insanelib.InsaneLib;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.mixin.ServerLevelAccessor;
import insane96mcp.insanelib.util.IntegratedPack;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.function.Predicate;

@LoadFeature(module = "insanelib:base", canBeDisabled = false)
public class BaseFeature extends Feature {

    @Config(description = "If true, game time, day time, and weather will not advance if no players are online. This can break anything that relies on game time. Also Serene Season and Time Control mods ticking are stopped. Game time is stopped with a simple flag in-code, whilst day time and weather are stopped with an integrated data pack.")
    public static Boolean preventTimeTickingIfNoPlayersOnline = true;

    @Override
    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        IntegratedPack.addServerPack(InsaneLib.MOD_ID, "no_player_time_stop", "InsaneLib's No Player Time Stop", () -> preventTimeTickingIfNoPlayersOnline);
    }

    @SubscribeEvent
    public void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        setTickTime(event.getEntity().getServer(), false, server -> server.getPlayerList().getPlayers().size() > 1);
    }

    @SubscribeEvent
    public void onPlayerLeave(PlayerEvent.PlayerLoggedInEvent event) {
        setTickTime(event.getEntity().getServer(), true, null);
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        setTickTime(event.getServer(), false, null);
    }

    public void setTickTime(@Nullable MinecraftServer server, boolean tickTime, @Nullable Predicate<MinecraftServer> extraConditions) {
        if (!preventTimeTickingIfNoPlayersOnline
                || server == null
                || (extraConditions != null && extraConditions.test(server)))
            return;
        ((ServerLevelAccessor) server.overworld()).setTickTime(tickTime);
    }
}
