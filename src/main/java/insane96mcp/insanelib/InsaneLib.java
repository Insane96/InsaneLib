package insane96mcp.insanelib;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;
import com.mojang.logging.LogUtils;
import insane96mcp.insanelib.command.ILCommand;
import insane96mcp.insanelib.data.AttributeModifierOperationSerializer;
import insane96mcp.insanelib.data.JsonFeatureDataReloadListener;
import insane96mcp.insanelib.datagen.ILItemTagProvider;
import insane96mcp.insanelib.module.base.PushResistance;
import insane96mcp.insanelib.module.base.items.ItemComponentsReloadListener;
import insane96mcp.insanelib.network.NetworkHandler;
import insane96mcp.insanelib.setup.*;
import insane96mcp.insanelib.util.IntegratedPack;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

@Mod(InsaneLib.MOD_ID)
public class InsaneLib {
    public static final String MOD_ID = "insanelib";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String CONFIG_FOLDER = "config/" + MOD_ID;

    public static ILModConfig CONFIG;

    /**
     * Same as {@link net.neoforged.neoforge.common.extensions.IAttributeExtension.FORMAT} but with one decimal place
     */
    public static DecimalFormat ONE_DECIMAL_FORMATTER;

    public InsaneLib(IEventBus eventBus, ModContainer modContainer) {
        CONFIG = new ILModConfig(id("base"), "Base", ModConfig.Type.COMMON,
                eventBus, InsaneLib.class.getClassLoader());
        modContainer.registerConfig(ModConfig.Type.COMMON, CONFIG.spec, MOD_ID + "/common.toml");

        ILAttributes.REGISTRY.register(eventBus);
        ILDataComponents.REGISTRY.register(eventBus);
        ILConditions.CONDITION_CODECS.register(eventBus);
        ILConditions.LOOT_CONDITIONS.register(eventBus);
        ILGlobalLootModifiers.REGISTRY.register(eventBus);
        ILLootFunctions.REGISTRY.register(eventBus);
        ILCriteriaTriggers.REGISTRY.register(eventBus);

        eventBus.addListener(InsaneLib::clientSetup);
        eventBus.addListener(IntegratedPack::onAddPackFinders);
        eventBus.addListener(NetworkHandler::register);
        eventBus.addListener(PushResistance::attribute);
        eventBus.addListener(InsaneLib::gatherData);
        NeoForge.EVENT_BUS.addListener(this::onAddReloadListeners);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
        NeoForge.EVENT_BUS.addListener(ILCriteriaTriggers::onBlockBreak);
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        ILCommand.register(event.getDispatcher(), event.getBuildContext());
    }

    private void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(JsonFeatureDataReloadListener.INSTANCE);
        event.addListener(ItemComponentsReloadListener.INSTANCE);
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        ONE_DECIMAL_FORMATTER = Util.make(new DecimalFormat("#.#"),
                fmt -> fmt.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT)));
    }

    public static void gatherData(GatherDataEvent event) {
        PackOutput output = event.getGenerator().getPackOutput();
        BlockTagsProvider blockTagsProvider = new BlockTagsProvider(output, event.getLookupProvider(), MOD_ID, event.getExistingFileHelper()) {
            @Override
            protected void addTags(HolderLookup.Provider provider) {}
        };
        event.getGenerator().addProvider(event.includeServer(), blockTagsProvider);
        event.getGenerator().addProvider(event.includeServer(),
                new ILItemTagProvider(output, event.getLookupProvider(), blockTagsProvider.contentsGetter(), event.getExistingFileHelper()));
    }

    /**
     * Creates a {@link ResourceLocation} using the provided namespace and path.
     *
     * @param path The specific path for the resource within the mod's namespace.
     * @return A {@link ResourceLocation} with the mod's namespace and the given path.
     */
    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    /**
     * Constructs a namespaced language key using the mod's ID and the provided path.
     *
     * @param path The specific path for the language key within the mod's namespace.
     * @return A fully-qualified language key in the format "MOD_ID.path".
     */
    public static String lang(String path) {
        return MOD_ID + "." + path;
    }

    public static GsonBuilder createGsonBuilder() {
        GsonBuilder gsonBuilder = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(AttributeModifier.Operation.class, new AttributeModifierOperationSerializer());
        //if (ModList.get().isLoaded("sereneseasons"))
        //    gsonBuilder.registerTypeAdapter(Season.SubSeason.class, new SubSeasonSerializer());
        return gsonBuilder;
    }

    public static Gson createGson() {
        return createGsonBuilder().create();
    }

    /**
     * Creates a Gson instance with an optional extra {@link TypeAdapterFactory} registered.
     * Used by {@link insane96mcp.insanelib.core.JsonFeature.JsonConfig} when {@code withRegistryFor()} is set.
     */
    public static Gson createGson(TypeAdapterFactory adapterFactory) {
        GsonBuilder builder = createGsonBuilder();
        if (adapterFactory != null)
            builder.registerTypeAdapterFactory(adapterFactory);
        return builder.create();
    }
}
