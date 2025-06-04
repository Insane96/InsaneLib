package insane96mcp.insanelib.module.base;

import com.google.gson.reflect.TypeToken;
import insane96mcp.insanelib.InsaneLib;
import insane96mcp.insanelib.base.JsonFeature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.data.ObjTag;
import insane96mcp.insanelib.mixin.ServerLevelAccessor;
import insane96mcp.insanelib.util.ILLogger;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@LoadFeature(module = "insanelib:base", canBeDisabled = false)
public class BaseFeature extends JsonFeature {
    public static final List<ObjTag<Block>> TEST_BLOCKS_DEFAULT = List.of(ObjTag.of("minecraft:stone", Registries.BLOCK), ObjTag.of("minecraft:dirt", Registries.BLOCK), ObjTag.of("#minecraft:cherry_logs", Registries.BLOCK));
    public static final List<ObjTag<Block>> testBlocks = new ArrayList<>();

    private static final Type BLOCK_LIST_TYPE = (new TypeToken<ArrayList<ObjTag<Block>>>() {}).getType();


    @Config(description = "If true, game time, day time, and weather will not advance if no players are online. This can break anything that relies on game time. Also Serene Season and Time Control mods ticking are stopped. Game time is stopped with a simple flag in-code, whilst day time and weather are stopped with an integrated data pack.")
    public static Boolean preventTimeTickingIfNoPlayersOnline = true;

    public BaseFeature(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
        IntegratedPack.addServerPack(InsaneLib.MOD_ID, "no_player_time_stop", "InsaneLib's No Player Time Stop", () -> preventTimeTickingIfNoPlayersOnline);
        addJsonConfig(
                new JsonConfig<>("test_blocks.json", testBlocks, TEST_BLOCKS_DEFAULT, BLOCK_LIST_TYPE)
                .withRegistryFor(Block.class)
        );
    }

    @Override
    public String getModConfigFolder() {
        return InsaneLib.CONFIG_FOLDER;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSpawn(LivingHurtEvent event) {
        if (!(event.getSource().getDirectEntity() instanceof Player player))
            return;
        if (player.level().dimension() == Level.OVERWORLD)
        for (ObjTag<Block> testBlock : testBlocks) {
            if (testBlock.matches(player.level().getBlockState(event.getEntity().blockPosition().below()).getBlock())) {
                ILLogger.info("Entity %s hurt when standing on test block", event.getEntity().getName().getString());
            }
        }
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
