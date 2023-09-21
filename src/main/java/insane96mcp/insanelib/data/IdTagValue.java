package insane96mcp.insanelib.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;

@JsonAdapter(IdTagValue.Serializer.class)
public class IdTagValue {
    public IdTagMatcher id;
    public double value;

    public IdTagValue(IdTagMatcher.Type type, String id, double value) {
        this(new IdTagMatcher(type, id), value);
    }

    public IdTagValue(IdTagMatcher idTagMatcher, double value) {
        this.id = idTagMatcher;
        this.value = value;
    }

    public static final java.lang.reflect.Type LIST_TYPE = new TypeToken<ArrayList<IdTagValue>>(){}.getType();

    public static class Serializer implements JsonDeserializer<IdTagValue>, JsonSerializer<IdTagValue> {
        @Override
        public IdTagValue deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jObject = json.getAsJsonObject();
            IdTagMatcher idTagMatcher = context.deserialize(jObject.get("id"), IdTagMatcher.class);

            return new IdTagValue(idTagMatcher, GsonHelper.getAsDouble(jObject, "value"));
        }

        @Override
        public JsonElement serialize(IdTagValue src, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jObject = new JsonObject();
            jObject.add("id", context.serialize(src.id));
            jObject.addProperty("value", src.value);

            return jObject;
        }
    }
}