package insane96mcp.insanelib.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import net.minecraft.util.GsonHelper;

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