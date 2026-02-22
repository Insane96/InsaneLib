package insane96mcp.insanelib;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;
import com.mojang.logging.LogUtils;
import insane96mcp.insanelib.data.AttributeModifierOperationSerializer;
import insane96mcp.insanelib.data.JsonFeatureDataReloadListener;
import insane96mcp.insanelib.network.NetworkHandler;
import insane96mcp.insanelib.setup.ILModConfig;
import insane96mcp.insanelib.util.IntegratedPack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.slf4j.Logger;

@Mod(InsaneLib.MOD_ID)
public class InsaneLib {
    public static final String MOD_ID = "insanelib";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String CONFIG_FOLDER = "config/" + MOD_ID;

    public static ILModConfig CONFIG;

    public InsaneLib(IEventBus modEventBus, ModContainer modContainer) {
        CONFIG = new ILModConfig(location("base"), "Base", ModConfig.Type.COMMON,
                modEventBus, InsaneLib.class.getClassLoader());
        modContainer.registerConfig(ModConfig.Type.COMMON, CONFIG.spec, MOD_ID + "/common.toml");
        modEventBus.addListener(IntegratedPack::onAddPackFinders);
        modEventBus.addListener(NetworkHandler::register);
        NeoForge.EVENT_BUS.addListener(this::onAddReloadListeners);
    }

    private void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(JsonFeatureDataReloadListener.INSTANCE);
    }

    /**
     * Creates a {@link ResourceLocation} using the provided namespace and path.
     *
     * @param path The specific path for the resource within the mod's namespace.
     * @return A {@link ResourceLocation} with the mod's namespace and the given path.
     */
    public static ResourceLocation location(String path) {
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
