package insane96mcp.insanelib.module.base;

import insane96mcp.insanelib.InsaneLib;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.mixin.accessor.ServerLevelAccessor;
import insane96mcp.insanelib.util.IntegratedPack;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerFunctionManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Predicate;

@LoadFeature(enabledByDefault = false, description = "If true, game time and day time, will not advance if no players are online. This can break anything that relies on game time so it's disabled by default.")
public class TimeStopNoPlayerOnline extends Feature {

    @Config(description = "If true, also prevents weather from advancing if no players are online. This needs to be disabled in order to allow to set the gamerule again.")
    public static Boolean alsoWeather = true;
    @Config(description = "If true, also prevents game time from advancing if no players are online with Time Control installed. This needs to be disabled in order to allow to set the gamerule again.")
    public static Boolean timeControlIntegration = true;
    @Config(description = "If true, also prevents seasons from advancing if no players are online with Serene Seasons installed. This needs to be disabled in order to allow to set the gamerule again.")
    public static Boolean sereneSeasonsIntegration = true;

    @Override
    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        IntegratedPack.addServerPack(InsaneLib.MOD_ID, "no_player_time_stop", "InsaneLib's No Player Time Stop", this::isEnabled);
        IntegratedPack.addServerPack(InsaneLib.MOD_ID, "no_player_time_stop_tc", "InsaneLib's No Player Time Stop Time Control", () -> this.isEnabled() && ModList.get().isLoaded("timecontrol"));
        IntegratedPack.addServerPack(InsaneLib.MOD_ID, "no_player_time_stop_season", "InsaneLib's No Player Time Stop Serene Seasons", () -> this.isEnabled() && ModList.get().isLoaded("sereneseasons"));
    }

    @SubscribeEvent
    public void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        setTickTime(event.getEntity().level().getServer(), false, server -> server.getPlayerList().getPlayers().size() > 1);
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        setTickTime(event.getEntity().level().getServer(), true, null);
    }

    /**
     * If true, world clocks (day time) don't advance. Checked by {@link insane96mcp.insanelib.mixin.ServerClockManagerMixin}
     */
    public static boolean clocksStopped = false;

    static int tick = 0;
    // Looked up every time, as functions change on /reload
    public static final Identifier STOP = InsaneLib.id("stop_weather_if_no_player_online");
    public static final Identifier TC = InsaneLib.id("stop_time_if_no_player_online_tc");
    public static final Identifier SEASONS = InsaneLib.id("stop_season_if_no_player_online");

    @SubscribeEvent
    public void serverTick(ServerTickEvent.Post event) {
        if (!this.isEnabled()) {
            // The feature has been disabled while time was stopped
            ServerLevelAccessor overworld = (ServerLevelAccessor) event.getServer().overworld();
            if (!overworld.insanelib$isTickTime())
                overworld.setTickTime(true);
            clocksStopped = false;
            return;
        }
        if (++tick <= 20)
            return;
        tick = 0;
        ServerFunctionManager functions = event.getServer().getFunctions();
        if (alsoWeather)
            executeFunction(functions, STOP);
        if (ModList.get().isLoaded("timecontrol") && timeControlIntegration)
            executeFunction(functions, TC);
        if (ModList.get().isLoaded("sereneseasons") && sereneSeasonsIntegration)
            executeFunction(functions, SEASONS);
    }

    private void executeFunction(ServerFunctionManager functions, Identifier id) {
        Optional<CommandFunction<CommandSourceStack>> func = functions.get(id);
        if (func.isPresent())
            functions.execute(func.get(), functions.getGameLoopSender());
        else
            InsaneLib.LOGGER.warn("{} function not found", id);
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        setTickTime(event.getServer(), false, null);
    }

    @SubscribeEvent
    public void onServerStopped(ServerStoppedEvent event) {
        clocksStopped = false;
    }

    public void setTickTime(@Nullable MinecraftServer server, boolean tickTime, @Nullable Predicate<MinecraftServer> extraConditions) {
        if (!this.isEnabled()
                || server == null
                || (extraConditions != null && extraConditions.test(server)))
            return;
        // Game time
        ((ServerLevelAccessor) server.overworld()).setTickTime(tickTime);
        // Day time (world clocks), see ServerClockManagerMixin
        clocksStopped = !tickTime;
    }
}
