package insane96mcp.insanelib.module.base;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.mixin.ServerLevelAccessor;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.function.Predicate;

@LoadFeature(module = "insanelib:base", canBeDisabled = false)
public class BaseFeature extends Feature {

    @Config(description = "If true, the game time will not advance if no players are online. This can break anything that relies on game time.")
    public static Boolean preventTimeTickingIfNoPlayersOnline = true;

    public BaseFeature(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
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
    public void onPlayerLeave(ServerStartedEvent event) {
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
