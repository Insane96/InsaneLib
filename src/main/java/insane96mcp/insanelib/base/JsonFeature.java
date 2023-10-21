package insane96mcp.insanelib.base;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import insane96mcp.insanelib.data.IdTagMatcher;
import insane96mcp.insanelib.data.JsonFeatureDataReloadListener;
import insane96mcp.insanelib.network.JsonConfigSyncMessage;
import insane96mcp.insanelib.util.LogHelper;
import insane96mcp.insanelib.util.TagUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.commons.io.FilenameUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * An extension of {@link Feature} that can handle Json Configs
 */
public abstract class JsonFeature extends Feature {
    public final List<JsonConfig<?>> JSON_CONFIGS = new ArrayList<>();

    public JsonFeature(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
        JsonFeatureDataReloadListener.INSTANCE.registerJsonConfigFeature(this);
    }

    public abstract String getModConfigFolder();

    public void loadJsonConfigs() {
        if (!this.isEnabled())
            return;
        File jsonConfigFolder = new File(getModConfigFolder() + "/" + this.getModule().getName() + "/" + this.getName());
        if (!jsonConfigFolder.exists()) {
            if (!jsonConfigFolder.mkdirs()) {
                LogHelper.warn("Failed to create %s json config folder", this.getName());
            }
        }
        for (JsonConfig<?> jsonConfig : JSON_CONFIGS) {
            jsonConfig.loadAndReadFile(jsonConfigFolder);
        }
    }

    protected static <T> void loadAndReadJson(String json, List<T> list, final List<T> defaultList, Type listType) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
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

    /**
     * Use this instead of Utils.isItemInTag when reloading data due to tags not existing yet at reload
     */
    public static boolean isItemInTag(Item item, ResourceLocation tag, boolean isClientSide) {
        if (isClientSide)
            return TagUtils.isItemInTag(item, tag);
        TagKey<Item> tagKey = TagKey.create(Registries.ITEM, tag);
        Collection<Holder<Item>> tags = JsonFeatureDataReloadListener.reloadContext.getTag(tagKey);
        for (Holder<Item> holder : tags) {
            if (holder.value().equals(item))
                return true;
        }
        return false;
    }

    /**
     * Use this instead of Utils.isItemInTag when reloading data due to tags not existing yet at reload
     */
    public static boolean isItemInTag(Item item, TagKey<Item> tag, boolean isClientSide) {
        if (isClientSide)
            return TagUtils.isItemInTag(item, tag);
        Collection<Holder<Item>> tags = JsonFeatureDataReloadListener.reloadContext.getTag(tag);
        for (Holder<Item> holder : tags) {
            if (holder.value().equals(item))
                return true;
        }
        return false;
    }

    /**
     * Use this instead of IdTagMatcher#getAllItems when reloading data due to tags not existing yet at reload
     */
    public static List<Item> getAllItems(IdTagMatcher idTagMatcher, boolean isClientSide) {
        if (idTagMatcher.type == IdTagMatcher.Type.ID || isClientSide)
            return idTagMatcher.getAllItems();

        TagKey<Item> tagKey = TagKey.create(Registries.ITEM, idTagMatcher.location);
        Collection<Holder<Item>> tags = JsonFeatureDataReloadListener.reloadContext.getTag(tagKey);
        ArrayList<Item> list = new ArrayList<>();
        for (Holder<Item> holder : tags) {
            list.add(holder.value());
        }
        return list;
    }

    /**
     * Use this instead of Utils.isBlockInTag when reloading data due to tags not existing yet at reload
     */
    public static boolean isBlockInTag(Block block, ResourceLocation tag, boolean isClientSide) {
        if (isClientSide)
            return TagUtils.isBlockInTag(block, tag);
        TagKey<Block> tagKey = TagKey.create(Registries.BLOCK, tag);
        Collection<Holder<Block>> tags = JsonFeatureDataReloadListener.reloadContext.getTag(tagKey);
        for (Holder<Block> holder : tags) {
            if (holder.value().equals(block))
                return true;
        }
        return false;
    }

    /**
     * Use this instead of Utils.isBlockInTag when reloading data due to tags not existing yet at reload
     */
    public static boolean isBlockInTag(Block block, TagKey<Block> tag, boolean isClientSide) {
        if (isClientSide)
            return TagUtils.isBlockInTag(block, tag);
        Collection<Holder<Block>> tags = JsonFeatureDataReloadListener.reloadContext.getTag(tag);
        for (Holder<Block> holder : tags) {
            if (holder.value().equals(block))
                return true;
        }
        return false;
    }

    /**
     * Use this instead of IdTagMatcher#getAllBlocks when reloading data due to tags not existing yet at reload
     */
    public static List<Block> getAllBlocks(IdTagMatcher idTagMatcher, boolean isClientSide) {
        if (idTagMatcher.type == IdTagMatcher.Type.ID || isClientSide)
            return idTagMatcher.getAllBlocks();

        TagKey<Block> tagKey = TagKey.create(Registries.BLOCK, idTagMatcher.location);
        Collection<Holder<Block>> tags = JsonFeatureDataReloadListener.reloadContext.getTag(tagKey);
        ArrayList<Block> list = new ArrayList<>();
        for (Holder<Block> holder : tags) {
            list.add(holder.value());
        }
        return list;
    }

    /**
     * Use this instead of Utils.isEntityInTag when reloading data due to tags not existing yet at reload
     */
    public static boolean isEntityInTag(Entity entity, ResourceLocation tag, boolean isClientSide) {
        if (isClientSide)
            return TagUtils.isEntityInTag(entity, tag);
        TagKey<EntityType<?>> tagKey = TagKey.create(Registries.ENTITY_TYPE, tag);
        Collection<Holder<EntityType<?>>> tags = JsonFeatureDataReloadListener.reloadContext.getTag(tagKey);
        for (Holder<EntityType<?>> holder : tags) {
            if (holder.value().equals(entity.getType()))
                return true;
        }
        return false;
    }

    /**
     * Use this instead of Utils.isEntityInTag when reloading data due to tags not existing yet at reload
     */
    public static boolean isEntityInTag(Entity entity, TagKey<EntityType<?>> tag, boolean isClientSide) {
        if (isClientSide)
            return TagUtils.isEntityInTag(entity, tag);
        Collection<Holder<EntityType<?>>> tags = JsonFeatureDataReloadListener.reloadContext.getTag(tag);
        for (Holder<EntityType<?>> holder : tags) {
            if (holder.value().equals(entity.getType()))
                return true;
        }
        return false;
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
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

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
                    LogHelper.error("Failed to create default Json %s: %s", FilenameUtils.removeExtension(file.getName()), e.getMessage());
                }
            }

            this.list.clear();
            try {
                FileReader fileReader = new FileReader(file);
                List<T> listRead = gson.fromJson(fileReader, listType);
                this.list.addAll(listRead);
            }
            catch (JsonSyntaxException e) {
                LogHelper.error("Parsing error loading Json %s: %s", FilenameUtils.removeExtension(file.getName()), e.getMessage());
            }
            catch (Exception e) {
                LogHelper.error("Failed loading Json %s: %s", FilenameUtils.removeExtension(file.getName()), e.getMessage());
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

            Gson gson = new GsonBuilder().create();

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
    }

    private static final HashMap<ResourceLocation, SyncType> SYNC_TYPE_REGISTRY = new HashMap<>();
    public static void addSyncType(ResourceLocation id, SyncType syncType) {
        SYNC_TYPE_REGISTRY.put(id, syncType);
    }
    @Nullable
    public static SyncType getSyncType(ResourceLocation id) {
        return SYNC_TYPE_REGISTRY.get(id);
    }

    @SubscribeEvent
    public void onDataPackSync(OnDatapackSyncEvent event) {
        for (JsonConfig<?> jsonConfig : JSON_CONFIGS) {
            jsonConfig.syncToClient(event);
        }
    }

    @SubscribeEvent
    public void onTagsUpdatedEvent(TagsUpdatedEvent event) {
        if (event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.CLIENT_PACKET_RECEIVED) {
            for (JsonConfig<?> jsonConfig : JSON_CONFIGS) {
                jsonConfig.onLoad(true);
            }
        }
    }
}
