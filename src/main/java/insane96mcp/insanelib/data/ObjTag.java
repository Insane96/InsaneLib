package insane96mcp.insanelib.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import insane96mcp.insanelib.util.ILLogger;
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

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@JsonAdapter(ObjTag.Serializer.class)
public class ObjTag<T> {
    @Nullable
    private T obj;
    @Nullable
    private TagKey<T> tag;
    private final Registry<T> registry;
    //TODO Add dimension

    private ObjTag(T obj, Registry<T> registry) {
        this.obj = obj;
        this.registry = registry;
    }

    private ObjTag(TagKey<T> tag, Registry<T> registry) {
        this.tag = tag;
        this.registry = registry;
    }

    public static <T> ObjTag<T> objOf(T obj, Registry<T> registry) {
        return new ObjTag<>(obj, registry);
    }

    public static <T> ObjTag<T> tagOf(TagKey<T> tagKey, Registry<T> registry) {
        return new ObjTag<>(tagKey, registry);
    }

    public static <T> ObjTag<T> of(String id, ResourceKey<Registry<T>> registry) {
        if (id.startsWith("#"))
            return tagOf(ResourceLocation.parse(id.substring(1)), registry);
        else
            return objOf(ResourceLocation.parse(id), registry);
    }

    public static <T> ObjTag<T> objOf(ResourceLocation id, ResourceKey<Registry<T>> registry) {
        //noinspection unchecked
        Registry<T> reg = (Registry<T>) BuiltInRegistries.REGISTRY.get(registry.location());
        if (reg == null)
            throw new IllegalArgumentException("Unknown registry %s".formatted(registry.location()));
        return objOf(reg.get(id), reg);
    }

    public static <T> ObjTag<T> tagOf(ResourceLocation id, ResourceKey<Registry<T>> registry) {
        //noinspection unchecked
        Registry<T> reg = (Registry<T>) BuiltInRegistries.REGISTRY.get(registry.location());
        if (reg == null)
            throw new IllegalArgumentException("Unknown registry %s".formatted(registry.location()));
        return tagOf(TagKey.create(registry, id), reg);
    }

    public boolean matches(T obj) {
        if (this.obj != null && this.obj.equals(obj))
            return true;
        if (this.tag == null)
            return false;
        Optional<HolderSet.Named<T>> tag = this.registry.getTag(this.tag);
        if (tag.isEmpty()) {
            ILLogger.debug("Tag {} not found", this.tag);
            return false;
        }
        Holder<T> holder = this.registry.wrapAsHolder(obj);
        return tag.get().contains(holder);
    }

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

    /// Deserializes an object ("namespace:id") or a tag ("#namespace:id")
    public static <T> ObjTag<T> deserialize(JsonElement jElement, ResourceKey<Registry<T>> registryKey) throws JsonParseException {
        if (!jElement.isJsonPrimitive())
            throw new JsonParseException("Expected %s to be a string".formatted(jElement));
        return of(jElement.getAsString(), registryKey);
    }

    /// Deserializes a list of objects or tags
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

    public JsonElement serialize() {
        if (this.obj != null)
            return new JsonPrimitive(this.registry.getKey(this.obj).toString());
        else if (this.tag != null)
            return new JsonPrimitive("#" + this.tag.location());
        else
            throw new RuntimeException("Invalid ObjTag");
    }

    public static <T> JsonArray serializeList(List<ObjTag<T>> list) {
        JsonArray jArray = new JsonArray(list.size());
        for (ObjTag<T> tObjTag : list) {
            jArray.add(tObjTag.serialize());
        }
        return jArray;
    }

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
                public void write(JsonWriter out, ObjTag<T> value) throws IOException {
                    gson.toJson(value.serialize(), out);
                }

                @Override
                public ObjTag<T> read(JsonReader in) throws IOException {
                    JsonElement json = JsonParser.parseReader(in);
                    return ObjTag.deserialize(json, registryKey);
                }
            };
        }
    }
}
