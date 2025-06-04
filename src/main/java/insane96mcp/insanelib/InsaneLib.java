package insane96mcp.insanelib;

import insane96mcp.insanelib.data.JsonFeatureDataReloadListener;
import insane96mcp.insanelib.network.NetworkHandler;
import insane96mcp.insanelib.setup.Config;
import insane96mcp.insanelib.setup.ILGlobalLootModifiers;
import insane96mcp.insanelib.util.IntegratedPack;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.MissingMappingsEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.function.Function;

@Mod(InsaneLib.MOD_ID)
public class InsaneLib
{
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "insanelib";
    public static final String CONFIG_FOLDER = "config/" + MOD_ID;
    public static final String RESOURCE_PREFIX = MOD_ID + ":";

    /**
     * Same as {@link ItemStack#ATTRIBUTE_MODIFIER_FORMAT} but with one decimal place
     */
    public static DecimalFormat ONE_DECIMAL_FORMATTER;

    public InsaneLib(FMLJavaModLoadingContext context) {
        context.registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, Config.COMMON_SPEC, MOD_ID + ".toml");
        context.getModEventBus().addListener(InsaneLib::clientSetup);
        context.getModEventBus().addListener(this::preInit);
        context.getModEventBus().addListener(this::addPackFinders);
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

    @SubscribeEvent
    public void addPackFinders(AddPackFindersEvent event)
    {
        IntegratedPack.onAddPackFinders(event);
    }

    /**
     * Handles missing mappings for the given registry
     * @param event    Mappings event
     * @param handler  Mapping handler
     * @param <T>      Event type
     */
    public static <T> void handleMissingMappings(MissingMappingsEvent event, String modId, ResourceKey<? extends Registry<T>> registry, Function<String, T> handler) {
        for (MissingMappingsEvent.Mapping<T> mapping : event.getAllMappings(registry)) {
            ResourceLocation id = mapping.getKey();
            if (modId.equals(id.getNamespace())) {
                @Nullable T value = handler.apply(id.getPath());
                if (value != null) {
                    mapping.remap(value);
                }
            }
        }
    }
}
