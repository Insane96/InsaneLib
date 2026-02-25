package insane96mcp.insanelib.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @param attribute uses supplier due to modded attributes not being at startup
 */
@JsonAdapter(SerializableAttributeModifier.Serializer.class)
public record SerializableAttributeModifier(ResourceLocation id, List<EquipmentSlot> slots,
                                            Holder<Attribute> attribute, double amount,
                                            AttributeModifier.Operation operation) {
    public AttributeModifier getModifier() {
        return new AttributeModifier(id, amount, operation);
    }

    public static final Type LIST_TYPE = new TypeToken<ArrayList<SerializableAttributeModifier>>() {}.getType();
    public static final Type LIST_TYPE_SLOT = new TypeToken<ArrayList<EquipmentSlot>>() {}.getType();
    public static class Serializer implements JsonDeserializer<SerializableAttributeModifier>, JsonSerializer<SerializableAttributeModifier> {
        @Override
        public SerializableAttributeModifier deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jObject = json.getAsJsonObject();
            String sAttribute = GsonHelper.getAsString(jObject, "attribute");
            Optional<Holder.Reference<Attribute>> attribute = BuiltInRegistries.ATTRIBUTE.getHolder(ResourceLocation.parse(sAttribute));
            if (attribute.isEmpty())
                throw new JsonParseException("Invalid attribute: %s".formatted(sAttribute));
            ResourceLocation id = ResourceLocation.parse(GsonHelper.getAsString(jObject, "id"));
            List<EquipmentSlot> slots = new ArrayList<>();
            if (jObject.has("slot")) {
                slots.add(EquipmentSlot.byName(jObject.get("slot").getAsString()));
            }
            else if (jObject.has("slots")) {
                JsonArray jArraySlot = jObject.getAsJsonArray("slots");
                for (int i = 0; i < jArraySlot.size(); i++) {
                    EquipmentSlot slot = EquipmentSlot.byName(jArraySlot.get(i).getAsString());
                    slots.add(slot);
                }
            }
            double amount = GsonHelper.getAsDouble(jObject, "amount");
            AttributeModifier.Operation operation = context.deserialize(jObject.get("operation"), AttributeModifier.Operation.class);
            return new SerializableAttributeModifier(id, slots, attribute.get(), amount, operation);
        }

        @Override
        public JsonElement serialize(SerializableAttributeModifier src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jObject = new JsonObject();
            jObject.addProperty("attribute", BuiltInRegistries.ATTRIBUTE.getKey(src.attribute.value()).toString());
            jObject.addProperty("id", src.id.toString());
            if (src.slots.size() == 1) {
                jObject.addProperty("slot", src.slots.get(0).getName());
            }
            else if (src.slots.size() > 1) {
                JsonArray jArraySlots = new JsonArray();
                for (EquipmentSlot equipmentSlot : src.slots) {
                    jArraySlots.add(equipmentSlot.getName());
                }
                jObject.add("slots", jArraySlots);
            }
            jObject.addProperty("amount", src.amount);
            jObject.addProperty("operation", src.operation.getSerializedName());
            return jObject;
        }
    }

    public static SerializableAttributeModifier fromNetwork(FriendlyByteBuf byteBuf) {
        Optional<Holder.Reference<Attribute>> attribute = BuiltInRegistries.ATTRIBUTE.getHolder(ResourceLocation.parse(byteBuf.readUtf()));
        if (attribute.isEmpty())
            throw new IllegalStateException("Invalid attribute from network: %s".formatted(byteBuf.readUtf()));
        ResourceLocation id = byteBuf.readResourceLocation();
        List<EquipmentSlot> slots = byteBuf.readList(byteBuf1 -> byteBuf1.readEnum(EquipmentSlot.class));
        double amount = byteBuf.readDouble();
        AttributeModifier.Operation operation = byteBuf.readEnum(AttributeModifier.Operation.class);
        return new SerializableAttributeModifier(id, slots, attribute.get(), amount, operation);
    }

    public void toNetwork(FriendlyByteBuf byteBuf) {
        byteBuf.writeUtf(BuiltInRegistries.ATTRIBUTE.getKey(this.attribute.value()).toString());
        byteBuf.writeResourceLocation(this.id);
        byteBuf.writeCollection(this.slots, FriendlyByteBuf::writeEnum);
        byteBuf.writeDouble(this.amount);
        byteBuf.writeEnum(this.operation);
    }
}