package insane96mcp.insanelib.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import insane96mcp.insanelib.InsaneLib;
import insane96mcp.insanelib.core.feature.config.ConfigOption;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.ModConfigSpec;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A generic, type-safe reference to either a registry object or a tag.
 * Replaces the old {@code IdTagMatcher} system with a unified API that works with any registry type.
 * <p>
 * An ObjTag can represent:
 * <ul>
 *   <li>A direct object reference (e.g. {@code "minecraft:zombie"})</li>
 *   <li>A tag reference (e.g. {@code "#minecraft:undead"})</li>
 * </ul>
 *
 * @param <T> the registry object type (e.g. {@code EntityType<?>}, {@code Item}, {@code Block})
 */
@JsonAdapter(ObjTag.Serializer.class)
public class ObjTag<T> {
    @Nullable
    private T obj;
    @Nullable
    private TagKey<T> tag;
    private final Registry<T> registry;
    private final ResourceKey<Registry<T>> registryKey;
    //TODO Add dimension

    @SuppressWarnings("unchecked")
    private ObjTag(T obj, Registry<T> registry) {
        this.obj = obj;
        this.registry = registry;
        this.registryKey = (ResourceKey<Registry<T>>) registry.key();
    }

    @SuppressWarnings("unchecked")
    private ObjTag(TagKey<T> tag, Registry<T> registry) {
        this.tag = tag;
        this.registry = registry;
        this.registryKey = (ResourceKey<Registry<T>>) registry.key();
    }

    /**
     * Creates an ObjTag wrapping a direct registry object.
     */
    public static <T> ObjTag<T> objOf(T obj, Registry<T> registry) {
        return new ObjTag<>(obj, registry);
    }

    /**
     * Creates an ObjTag wrapping a tag key.
     */
    public static <T> ObjTag<T> tagOf(TagKey<T> tagKey, Registry<T> registry) {
        return new ObjTag<>(tagKey, registry);
    }

    /**
     * Creates an ObjTag from a string identifier. Strings starting with {@code #} are treated as tags.
     * @param id the identifier, e.g. {@code "minecraft:zombie"} or {@code "#minecraft:undead"}
     * @param registry the registry key to resolve against
     */
    public static <T> ObjTag<T> of(String id, ResourceKey<Registry<T>> registry) {
        if (id.startsWith("#"))
            return tagOf(ResourceLocation.parse(id.substring(1)), registry);
        else
            return objOf(ResourceLocation.parse(id), registry);
    }

    /**
     * Creates an ObjTag for a direct object, resolved by {@link ResourceLocation} from a registry key.
     */
    public static <T> ObjTag<T> objOf(ResourceLocation id, ResourceKey<Registry<T>> registry) {
        //noinspection unchecked
        Registry<T> reg = (Registry<T>) BuiltInRegistries.REGISTRY.get(registry.location());
        if (reg == null)
            throw new IllegalArgumentException("Unknown registry %s".formatted(registry.location()));
        return objOf(reg.get(id), reg);
    }

    /**
     * Creates an ObjTag for a tag, resolved by {@link ResourceLocation} from a registry key.
     */
    public static <T> ObjTag<T> tagOf(ResourceLocation id, ResourceKey<Registry<T>> registry) {
        //noinspection unchecked
        Registry<T> reg = (Registry<T>) BuiltInRegistries.REGISTRY.get(registry.location());
        if (reg == null)
            throw new IllegalArgumentException("Unknown registry %s".formatted(registry.location()));
        return tagOf(TagKey.create(registry, id), reg);
    }

    /**
     * Returns the registry key this ObjTag resolves against.
     */
    public ResourceKey<Registry<T>> getRegistryKey() {
        return this.registryKey;
    }

    /**
     * Returns the string representation of this ObjTag ({@code "namespace:id"} or {@code "#namespace:id"}).
     */
    public String toSerializedString() {
        return serialize().getAsString();
    }

    /**
     * Returns true if the given object matches this ObjTag.
     * For direct references, uses {@code equals()}. For tags, checks if the object is contained in the tag.
     */
    public boolean matches(T obj) {
        if (this.obj != null && this.obj.equals(obj))
            return true;
        if (this.tag == null)
            return false;
        Optional<HolderSet.Named<T>> tag = this.registry.getTag(this.tag);
        if (tag.isEmpty()) {
            InsaneLib.LOGGER.debug("Tag {} not found", this.tag);
            return false;
        }
        Holder<T> holder = this.registry.wrapAsHolder(obj);
        return tag.get().contains(holder);
    }

    /**
     * Deserializes a plain registry object from a JSON string element (not a tag).
     * @return the resolved object, or null if not found in the registry
     */
    @Nullable
    public static <T> T deserializeRegistryObject(JsonElement jElement, ResourceKey<Registry<T>> registry) throws JsonParseException {
        if (!jElement.isJsonPrimitive())
            throw new JsonParseException("Expected %s to be a string".formatted(jElement));
        //noinspection unchecked
        Registry<T> reg = (Registry<T>) BuiltInRegistries.REGISTRY.get(registry.location());
        if (reg == null)
            throw new JsonParseException("Unknown registry %s".formatted(registry.location()));
        ResourceLocation objectId = ResourceLocation.parse(jElement.getAsString());
        return reg.get(objectId);
    }

    /**
     * Deserializes an object ({@code "namespace:id"}) or a tag ({@code "#namespace:id"}) from a JSON string element.
     */
    public static <T> ObjTag<T> deserialize(JsonElement jElement, ResourceKey<Registry<T>> registryKey) throws JsonParseException {
        if (!jElement.isJsonPrimitive())
            throw new JsonParseException("Expected %s to be a string".formatted(jElement));
        return of(jElement.getAsString(), registryKey);
    }

    /**
     * Deserializes a JSON array of objects and/or tags into a list of ObjTags.
     */
    public static <T> List<ObjTag<T>> deserializeList(JsonElement jElement, ResourceKey<Registry<T>> registryKey) throws JsonParseException {
        if (!jElement.isJsonArray())
            throw new JsonParseException("Expected %s to be an array".formatted(jElement));
        JsonArray jArray = jElement.getAsJsonArray();
        List<ObjTag<T>> list = new ArrayList<>(jArray.size());
        for (int i = 0; i < jArray.size(); i++) {
            list.add(deserialize(jArray.get(i), registryKey));
        }
        return list;
    }

    /**
     * Serializes this ObjTag to a JSON string. Objects are serialized as {@code "namespace:id"},
     * tags as {@code "#namespace:id"}.
     */
    public JsonElement serialize() {
        if (this.obj != null)
            return new JsonPrimitive(this.registry.getKey(this.obj).toString());
        else if (this.tag != null)
            return new JsonPrimitive("#" + this.tag.location());
        else
            throw new RuntimeException("Invalid ObjTag");
    }

    /**
     * Serializes a list of ObjTags to a JSON array.
     */
    public static <T> JsonArray serializeList(List<ObjTag<T>> list) {
        JsonArray jArray = new JsonArray(list.size());
        for (ObjTag<T> tObjTag : list) {
            jArray.add(tObjTag.serialize());
        }
        return jArray;
    }

    /**
     * Config option that stores an ObjTag as a string value in the mod config.
     */
    public static class COption<T> extends ConfigOption<ObjTag<T>> {
        private final ModConfigSpec.ConfigValue<String> valueConfig;
        private final ResourceKey<Registry<T>> registryKey;

        public COption(ModConfigSpec.Builder builder, String name, String description, ObjTag<T> defaultValue) {
            super(builder, name, description);
            this.registryKey = defaultValue.getRegistryKey();
            this.valueConfig = builder.define(name, defaultValue.toSerializedString());
        }

        @Override
        public ObjTag<T> get() {
            return ObjTag.of(this.valueConfig.get(), this.registryKey);
        }

        @Override
        public void set(Object value) {
            @SuppressWarnings("unchecked")
            ObjTag<T> objTag = (ObjTag<T>) value;
            this.valueConfig.set(objTag.toSerializedString());
        }

        @javax.annotation.Nullable
        @Override
        public java.util.List<String> getConfigPath() {
            return valueConfig.getPath();
        }
    }

    /**
     * Gson serializer/deserializer for ObjTag. Deserialization via Gson directly is not supported;
     * use {@link #deserialize(JsonElement, ResourceKey)} or {@link #deserializeList(JsonElement, ResourceKey)} instead.
     */
    public static class Serializer implements JsonSerializer<ObjTag<?>>, JsonDeserializer<ObjTag<?>> {

        @Override
        public ObjTag<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            throw new JsonParseException("Use ObjTag.deserialize to deserialize an ObjTag or deserializeList for a list");
        }

        @Override
        public JsonElement serialize(ObjTag<?> src, Type typeOfSrc, JsonSerializationContext context) {
            return src.serialize();
        }
    }

    /**
     * Maps Java classes to their corresponding Minecraft registry keys.
     * Supports Block, Item, EntityType, Enchantment, and Fluid.
     */
    public static class RegistryMappings {
        private static final Map<Class<?>, ResourceKey<? extends Registry<?>>> REGISTRY_BY_CLASS = Map.of(
                Block.class, Registries.BLOCK,
                Item.class, Registries.ITEM,
                EntityType.class, Registries.ENTITY_TYPE,
                Enchantment.class, Registries.ENCHANTMENT,
                Fluid.class, Registries.FLUID
        );

        @SuppressWarnings("unchecked")
        public static <T> ResourceKey<Registry<T>> getRegistryKey(Class<T> clazz) {
            return (ResourceKey<Registry<T>>) REGISTRY_BY_CLASS.get(clazz);
        }
    }

    /**
     * Gson {@link TypeAdapterFactory} for ObjTag fields. Requires a registry key to know which registry
     * to resolve against. Register with {@code gsonBuilder.registerTypeAdapterFactory(new ObjTag.AdapterFactory<>(registryKey))}.
     */
    public static class AdapterFactory<T> implements TypeAdapterFactory {
        private final ResourceKey<Registry<T>> registryKey;

        public AdapterFactory(ResourceKey<Registry<T>> registryKey) {
            this.registryKey = registryKey;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <R> TypeAdapter<R> create(Gson gson, TypeToken<R> typeToken) {
            if (!ObjTag.class.isAssignableFrom(typeToken.getRawType())) return null;

            return (TypeAdapter<R>) new TypeAdapter<ObjTag<T>>() {
                @Override
                public void write(JsonWriter out, ObjTag<T> value) {
                    gson.toJson(value.serialize(), out);
                }

                @Override
                public ObjTag<T> read(JsonReader in) {
                    JsonElement json = JsonParser.parseReader(in);
                    return ObjTag.deserialize(json, registryKey);
                }
            };
        }
    }
}
