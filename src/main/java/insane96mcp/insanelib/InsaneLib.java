package insane96mcp.insanelib;

import insane96mcp.insanelib.data.JsonFeatureDataReloadListener;
import insane96mcp.insanelib.network.NetworkHandler;
import insane96mcp.insanelib.setup.Config;
import insane96mcp.insanelib.setup.ILGlobalLootModifiers;
import net.minecraft.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

@Mod(InsaneLib.MOD_ID)
public class InsaneLib
{
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "insanelib";
    public static final String RESOURCE_PREFIX = MOD_ID + ":";

    /**
     * Same as {@link ItemStack#ATTRIBUTE_MODIFIER_FORMAT} but with one decimal place
     */
    public static DecimalFormat ONE_DECIMAL_FORMATTER;

    public InsaneLib(FMLJavaModLoadingContext context) {
        context.registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, Config.COMMON_SPEC, MOD_ID + ".toml");
        context.getModEventBus().addListener(InsaneLib::clientSetup);
        context.getModEventBus().addListener(this::preInit);
        MinecraftForge.EVENT_BUS.register(this);
        final IEventBus modEventBus = context.getModEventBus();
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

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        ONE_DECIMAL_FORMATTER = Util.make(new DecimalFormat("#.#"), (p_41704_) -> {
            p_41704_.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
        });
    }
}
