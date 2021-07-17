package insane96mcp.insanelib;

import insane96mcp.insanelib.setup.ClientSetup;
import insane96mcp.insanelib.setup.Config;
import insane96mcp.insanelib.setup.ModEntities;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(InsaneLib.MOD_ID)
public class InsaneLib
{
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "insanelib";

    public InsaneLib() {
        ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, Config.COMMON_SPEC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::init);
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEntities.ENTITIES.register(modEventBus);
    }
}