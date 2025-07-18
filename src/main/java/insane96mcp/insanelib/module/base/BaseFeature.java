package insane96mcp.insanelib.module.base;

import insane96mcp.insanelib.InsaneLib;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.mixin.ServerLevelAccessor;
import insane96mcp.insanelib.util.IntegratedPack;
import net.minecraft.commands.CommandFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerFunctionManager;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Predicate;

@LoadFeature(module = "insanelib:base", canBeDisabled = false)
public class BaseFeature extends Feature {

    @Config(description = "If true, game time and day time, will not advance if no players are online. This can break anything that relies on game time.")
    public static Boolean preventTimeTickingIfNoPlayersOnline = false;
    @Config(description = "If true, also prevents weather from advancing if no players are online. This needs to be disabled in order to allow to set the gamerule again.")
    public static Boolean alsoWeather = true;
    @Config(description = "If true, also prevents game time from advancing if no players are online with Time Control installed. This needs to be disabled in order to allow to set the gamerule again.")
    public static Boolean timeControlIntegration = true;
    @Config(description = "If true, also prevents seasons from advancing if no players are online with Serene Seasons installed. This needs to be disabled in order to allow to set the gamerule again.")
    public static Boolean sereneSeasonsIntegration = true;

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

    static int tick = 0;
    public static CommandFunction.CacheableFunction STOP = null;
    public static CommandFunction.CacheableFunction TC = null;
    public static CommandFunction.CacheableFunction SEASONS = null;

    @SubscribeEvent
    public void serverTick(TickEvent.ServerTickEvent event) {
        if (!preventTimeTickingIfNoPlayersOnline)
            return;
        if (++tick <= 20)
            return;
        tick = 0;
        ServerFunctionManager functions = event.getServer().getFunctions();
        if (alsoWeather)
            executeFunction(functions, STOP, "insanelib:stop_weather_if_no_player_online");
        if (ModList.get().isLoaded("timecontrol") && timeControlIntegration)
            executeFunction(functions, TC, "insanelib:stop_time_if_no_player_online_tc");
        if (ModList.get().isLoaded("sereneseasons") && sereneSeasonsIntegration)
            executeFunction(functions, SEASONS, "insanelib:stop_season_if_no_player_online");
    }

    private void executeFunction(ServerFunctionManager functions, CommandFunction.CacheableFunction function, String id) {
        if (function == null)
            function = new CommandFunction.CacheableFunction(ResourceLocation.parse(id));
        Optional<CommandFunction> func = function.get(functions);
        if (func.isPresent())
            functions.execute(func.get(), functions.getGameLoopSender());
        else
            InsaneLib.LOGGER.warn("{} function not found", id);
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
