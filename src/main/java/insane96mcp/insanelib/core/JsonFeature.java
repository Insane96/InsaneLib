package insane96mcp.insanelib.core;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import insane96mcp.insanelib.InsaneLib;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.data.JsonFeatureDataReloadListener;
import insane96mcp.insanelib.network.message.JsonConfigSyncMessage;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import org.apache.commons.io.FilenameUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * An extension of {@link Feature} that can handle Json Configs
 */
public abstract class JsonFeature extends Feature {
    public final List<JsonConfig<?>> JSON_CONFIGS = new ArrayList<>();

    public JsonFeature() {
        super();
    }

    @Override
    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        JsonFeatureDataReloadListener.INSTANCE.registerJsonConfigFeature(this);
    }

    @Override
    public void registerEvents() {
        super.registerEvents();
        NeoForge.EVENT_BUS.addListener(this::onDataPackSync);
        NeoForge.EVENT_BUS.addListener(this::onTagsUpdatedEvent);
    }

    public abstract String getModConfigFolder();

    public void loadJsonConfigs() {
        if (!this.isEnabled())
            return;
        File jsonConfigFolder = new File(getModConfigFolder() + "/" + this.getModule().getName() + "/" + this.getName());
        if (!jsonConfigFolder.exists()) {
            if (!jsonConfigFolder.mkdirs()) {
                InsaneLib.LOGGER.warn("Failed to create {} json config folder", this.getName());
            }
        }
        for (JsonConfig<?> jsonConfig : JSON_CONFIGS) {
            jsonConfig.loadAndReadFile(jsonConfigFolder);
        }
    }

    protected static <T> void loadAndReadJson(String json, List<T> list, final List<T> defaultList, Type listType) {
        Gson gson = InsaneLib.createGson();
        list.clear();
        List<T> listRead;
        try {
            listRead = gson.fromJson(json, listType);
        }
        catch (Exception e) {
            listRead = new ArrayList<>(defaultList);
        }
        list.addAll(listRead);
    }

    public static class JsonConfig<T> {
        String fileName;
        /**
         * Items read from the json are stored in this list
         */
        List<T> list;
        /**
         * If there's no json yet, write these values and read them
         */
        List<T> defaultList;
        /**
         * The list type from {@link com.google.gson.reflect.TypeToken} <br/>
         * E.g. {@code new TypeToken<ArrayList<JsonData>>(){}.getType()}
         */
        Type listType;
        /**
         * Ran when the json has been loaded
         */
        @Nullable
        BiConsumer<List<T>, Boolean> onLoad;
        /**
         * If the {@link JsonConfig} should be synced to client
         */
        boolean syncToClient;
        /**
         * The id of the {@link SyncType} that will be called on client sync
         */
        @Nullable
        ResourceLocation syncType;

        public JsonConfig(String fileName, List<T> list, List<T> defaultList, Type listType, @Nullable BiConsumer<List<T>, Boolean> onLoad, boolean syncToClient, ResourceLocation syncType) {
            this.fileName = fileName;
            this.list = list;
            this.defaultList = defaultList;
            this.listType = listType;
            this.onLoad = onLoad;
            this.syncToClient = syncToClient;
            this.syncType = syncType;
        }

        public JsonConfig(String fileName, List<T> list, List<T> defaultList, Type listType, boolean syncToClient, ResourceLocation syncType) {
            this(fileName, list, defaultList, listType, null, syncToClient, syncType);
        }

        public JsonConfig(String fileName, List<T> list, List<T> defaultList, Type listType, BiConsumer<List<T>, Boolean> onLoad) {
            this(fileName, list, defaultList, listType, onLoad, false, null);
        }

        public JsonConfig(String fileName, List<T> list, List<T> defaultList, Type listType) {
            this(fileName, list, defaultList, listType, false, null);
        }

        protected void loadAndReadFile(File folder) {
            Gson gson = InsaneLib.createGson();

            File file = new File(folder, this.fileName);
            if (!file.exists()) {
                try {
                    if (!file.createNewFile()) {
                        throw new Exception("File#createNewFile failed");
                    }
                    String json = gson.toJson(defaultList, listType);
                    Files.write(file.toPath(), json.getBytes());
                }
                catch (Exception e) {
                    InsaneLib.LOGGER.error("Failed to create default Json {}: {}", FilenameUtils.removeExtension(file.getName()), e.getMessage());
                }
            }

            this.list.clear();
            try {
                FileReader fileReader = new FileReader(file);
                List<T> listRead = gson.fromJson(fileReader, listType);
                this.list.addAll(listRead);
            }
            catch (JsonSyntaxException e) {
                InsaneLib.LOGGER.error("Parsing error loading Json {}: {}", FilenameUtils.removeExtension(file.getName()), e.getMessage());
            }
            catch (Exception e) {
                InsaneLib.LOGGER.error("Failed loading Json {}: {}", FilenameUtils.removeExtension(file.getName()), e.getMessage());
            }

            this.onLoad(false);
        }

        public void onLoad(boolean isClientSide) {
            if (this.onLoad != null)
                this.onLoad.accept(this.list, isClientSide);
        }

        public void syncToClient(OnDatapackSyncEvent event) {
            if (!this.syncToClient)
                return;

            Gson gson = InsaneLib.createGson();

            if (event.getPlayer() == null) {
                event.getPlayerList().getPlayers().forEach(player -> JsonConfigSyncMessage.sync(this.syncType, gson.toJson(this.list, this.listType), player));
            }
            else {
                JsonConfigSyncMessage.sync(this.syncType, gson.toJson(this.list, this.listType), event.getPlayer());
            }
        }
    }

    public static class SyncType {
        public Consumer<String> onSync;

        public SyncType(Consumer<String> onSync) {
            this.onSync = onSync;
        }
    }

    private static final HashMap<ResourceLocation, SyncType> SYNC_TYPE_REGISTRY = new HashMap<>();
    public static void addSyncType(ResourceLocation id, SyncType syncType) {
        SYNC_TYPE_REGISTRY.put(id, syncType);
    }
    @Nullable
    public static SyncType getSyncType(ResourceLocation id) {
        return SYNC_TYPE_REGISTRY.get(id);
    }

    public void onDataPackSync(OnDatapackSyncEvent event) {
        for (JsonConfig<?> jsonConfig : JSON_CONFIGS) {
            jsonConfig.syncToClient(event);
        }
    }

    public void onTagsUpdatedEvent(TagsUpdatedEvent event) {
        if (event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.CLIENT_PACKET_RECEIVED) {
            for (JsonConfig<?> jsonConfig : JSON_CONFIGS) {
                jsonConfig.onLoad(true);
            }
        }
    }
}
