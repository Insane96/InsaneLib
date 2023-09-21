package insane96mcp.insanelib.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import net.minecraft.util.GsonHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;

@JsonAdapter(IdTagRange.Serializer.class)
public class IdTagRange {
    public IdTagMatcher id;
    public double min;
    public double max;

    public IdTagRange(IdTagMatcher.Type type, String id, double value) {
        this(new IdTagMatcher(type, id), value, value);
    }

    public IdTagRange(IdTagMatcher idTagMatcher, double min, double max) {
        this.id = idTagMatcher;
        this.min = min;
        this.max = max;
    }

    public static IdTagRange newId(String location, double min, double max) {
        return newId(location, null, min, max);
    }
    public static IdTagRange newId(String location, @Nullable String dimension, double min, double max) {
        return new IdTagRange(new IdTagMatcher(IdTagMatcher.Type.ID, location, dimension), min, max);
    }

    public static IdTagRange newTag(String location, double min, double max) {
        return newTag(location, null, min, max);
    }
    public static IdTagRange newTag(String location, @Nullable String dimension, double min, double max) {
        return new IdTagRange(new IdTagMatcher(IdTagMatcher.Type.TAG, location, dimension), min, max);
    }

    public static final java.lang.reflect.Type LIST_TYPE = new TypeToken<ArrayList<IdTagRange>>(){}.getType();

    public static class Serializer implements JsonDeserializer<IdTagRange>, JsonSerializer<IdTagRange> {
        @Override
        public IdTagRange deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jObject = json.getAsJsonObject();
            IdTagMatcher idTagMatcher = context.deserialize(jObject.get("id"), IdTagMatcher.class);

            return new IdTagRange(idTagMatcher, GsonHelper.getAsDouble(jObject, "min"), GsonHelper.getAsDouble(jObject, "max"));
        }

        @Override
        public JsonElement serialize(IdTagRange src, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jObject = new JsonObject();
            jObject.add("id", context.serialize(src.id));
            jObject.addProperty("min", src.min);
            jObject.addProperty("max", src.max);

            return jObject;
        }
    }
}