package insane96mcp.insanelib;

import insane96mcp.insanelib.data.JsonFeatureDataReloadListener;
import insane96mcp.insanelib.network.NetworkHandler;
import insane96mcp.insanelib.setup.ClientSetup;
import insane96mcp.insanelib.setup.Config;
import insane96mcp.insanelib.setup.ILEntities;
import insane96mcp.insanelib.setup.ILGlobalLootModifiers;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(InsaneLib.MOD_ID)
public class InsaneLib
{
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "insanelib";
    public static final String RESOURCE_PREFIX = MOD_ID + ":";

    public InsaneLib() {
        ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, Config.COMMON_SPEC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::init);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::preInit);
        MinecraftForge.EVENT_BUS.register(this);
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ILEntities.ENTITIES.register(modEventBus);
        ILGlobalLootModifiers.REGISTRY.register(modEventBus);
    }

    public void preInit(FMLCommonSetupEvent event) {
        NetworkHandler.init();
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onAddReloadListener(AddReloadListenerEvent event) {
        JsonFeatureDataReloadListener.reloadContext = event.getConditionContext();
        event.addListener(JsonFeatureDataReloadListener.INSTANCE);
    }
}
