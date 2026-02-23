package insane96mcp.insanelib.data;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import insane96mcp.insanelib.InsaneLib;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.GsonHelper;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * A pairing of an {@link ObjTag} and a {@code double} value.
 * Replaces the old {@code IdTagValue} system with a generic, type-safe equivalent.
 *
 * @param <T> the registry object type (e.g. {@code EntityType<?>}, {@code Item}, {@code Block})
 */
public class ObjTagValue<T> {
    public ObjTag<T> id;
    public double value;

    public ObjTagValue(ObjTag<T> id, double value) {
        this.id = id;
        this.value = value;
    }

    /**
     * Creates an {@code ObjTagValue} from a string identifier and value.
     * Strings starting with {@code #} are treated as tags.
     *
     * @param id          the identifier, e.g. {@code "minecraft:zombie"} or {@code "#minecraft:undead"}
     * @param value       the associated double value
     * @param registryKey the registry to resolve against
     */
    public static <T> ObjTagValue<T> of(String id, double value, ResourceKey<Registry<T>> registryKey) {
        return new ObjTagValue<>(ObjTag.of(id, registryKey), value);
    }

    public static final Type LIST_TYPE = new TypeToken<ArrayList<ObjTagValue<?>>>(){}.getType();

    @Override
    public String toString() {
        return this.id.toSerializedString() + ", " + this.value;
    }

    /**
     * Serializes this {@code ObjTagValue} to a {@link JsonObject} with {@code "id"} and {@code "value"} fields.
     */
    public JsonElement serialize() {
        JsonObject jObject = new JsonObject();
        jObject.add("id", this.id.serialize());
        jObject.addProperty("value", this.value);
        return jObject;
    }

    /**
     * Deserializes an {@code ObjTagValue} from a {@link JsonElement}.
     *
     * @param json        a JSON object with {@code "id"} and {@code "value"} fields
     * @param registryKey the registry to resolve the id against
     */
    public static <T> ObjTagValue<T> deserialize(JsonElement json, ResourceKey<Registry<T>> registryKey) throws JsonParseException {
        JsonObject jObject = json.getAsJsonObject();
        ObjTag<T> objTag = ObjTag.deserialize(jObject.get("id"), registryKey);
        return new ObjTagValue<>(objTag, GsonHelper.getAsDouble(jObject, "value"));
    }

    /**
     * Deserializes a JSON array of {@code ObjTagValue} entries.
     *
     * @param json        a JSON array
     * @param registryKey the registry to resolve ids against
     */
    public static <T> List<ObjTagValue<T>> deserializeList(JsonElement json, ResourceKey<Registry<T>> registryKey) throws JsonParseException {
        if (!json.isJsonArray())
            throw new JsonParseException("Expected %s to be an array".formatted(json));
        JsonArray jArray = json.getAsJsonArray();
        List<ObjTagValue<T>> list = new ArrayList<>(jArray.size());
        for (JsonElement element : jArray) {
            ObjTagValue<T> entry = deserialize(element, registryKey);
            if (!entry.id.isValid()) {
                InsaneLib.LOGGER.warn("ObjTagValue: '{}' was not found in the registry and will be ignored", entry.id.toSerializedString());
                continue;
            }
            list.add(entry);
        }
        return list;
    }

    /**
     * Serializes a list of {@code ObjTagValue} entries to a JSON array.
     */
    public static <T> JsonArray serializeList(List<ObjTagValue<T>> list) {
        JsonArray jArray = new JsonArray(list.size());
        for (ObjTagValue<T> entry : list) {
            jArray.add(entry.serialize());
        }
        return jArray;
    }

    /**
     * Gson serializer for {@code ObjTagValue}. Only handles serialization;
     * use {@link #deserialize(JsonElement, ResourceKey)} or {@link AdapterFactory} for deserialization.
     */
    public static class Serializer implements JsonSerializer<ObjTagValue<?>> {
        @Override
        public JsonElement serialize(ObjTagValue<?> src, Type typeOfSrc, JsonSerializationContext context) {
            return src.serialize();
        }
    }

}
