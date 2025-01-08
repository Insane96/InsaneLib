package insane96mcp.insanelib.data;


import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * @param attribute uses supplier due to modded attributes not being at startup
 */
@JsonAdapter(SerializableAttributeModifier.Serializer.class)
public record SerializableAttributeModifier(UUID uuid, String name, List<EquipmentSlot> slots,
                                            Supplier<Attribute> attribute, double amount,
                                            AttributeModifier.Operation operation) {

    public SerializableAttributeModifier(UUID uuid, String name,
                                         Attribute attribute, double amount,
                                         AttributeModifier.Operation operation) {
        this(uuid, name, List.of(), () -> attribute, amount, operation);
    }

    public AttributeModifier getModifier() {
        return new AttributeModifier(uuid, name, amount, operation);
    }

    public static final Type LIST_TYPE = new TypeToken<ArrayList<SerializableAttributeModifier>>() {}.getType();
    public static final Type LIST_TYPE_SLOT = new TypeToken<ArrayList<EquipmentSlot>>() {}.getType();
    public static class Serializer implements JsonDeserializer<SerializableAttributeModifier>, JsonSerializer<SerializableAttributeModifier> {
        @Override
        public SerializableAttributeModifier deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jObject = json.getAsJsonObject();
            String sUUID = GsonHelper.getAsString(jObject, "uuid");
            UUID uuid;
            try {
                uuid = UUID.fromString(sUUID);
            } catch (Exception ex) {
                throw new JsonParseException("uuid %s is not valid".formatted(sUUID));
            }
            String name = GsonHelper.getAsString(jObject, "name");
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
            String sAttribute = GsonHelper.getAsString(jObject, "attribute");
            Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(ResourceLocation.tryParse(sAttribute));
            if (attribute == null) {
                throw new JsonParseException("Invalid attribute: %s".formatted(sAttribute));
            }
            double amount = GsonHelper.getAsDouble(jObject, "amount");
            Operation operation = context.deserialize(jObject.get("operation"), Operation.class);
            return new SerializableAttributeModifier(uuid, name, slots, () -> attribute, amount, operation.get());
        }

        @Override
        public JsonElement serialize(SerializableAttributeModifier src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jObject = new JsonObject();
            jObject.addProperty("uuid", src.uuid.toString());
            jObject.addProperty("name", src.name);
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
            jObject.addProperty("attribute", ForgeRegistries.ATTRIBUTES.getKey(src.attribute.get()).toString());
            jObject.addProperty("amount", src.amount);
            jObject.addProperty("operation", Operation.getNameFromOperation(src.operation));
            return jObject;
        }
    }

    public static SerializableAttributeModifier fromNetwork(FriendlyByteBuf byteBuf) {
        UUID uuid = byteBuf.readUUID();
        String name = byteBuf.readUtf();
        List<EquipmentSlot> slots = byteBuf.readList(byteBuf1 -> byteBuf1.readEnum(EquipmentSlot.class));
        Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(ResourceLocation.tryParse(byteBuf.readUtf()));
        double amount = byteBuf.readDouble();
        Operation operation = byteBuf.readEnum(Operation.class);
        return new SerializableAttributeModifier(uuid, name, slots, () -> attribute, amount, operation.get());
    }

    public void toNetwork(FriendlyByteBuf byteBuf) {
        byteBuf.writeUUID(this.uuid);
        byteBuf.writeUtf(this.name);
        byteBuf.writeCollection(this.slots, FriendlyByteBuf::writeEnum);
        byteBuf.writeUtf(ForgeRegistries.ATTRIBUTES.getKey(this.attribute.get()).toString());
        byteBuf.writeDouble(this.amount);
        byteBuf.writeEnum(this.operation);
    }

    //Serializable wrapper for AttributeModifier.Operation
    public enum Operation {
        @SerializedName("addition")
        ADDITION(AttributeModifier.Operation.ADDITION, "addition"),
        @SerializedName("multiply_base")
        MULTIPLY_BASE(AttributeModifier.Operation.MULTIPLY_BASE, "multiply_base"),
        @SerializedName("multiply_total")
        MULTIPLY_TOTAL(AttributeModifier.Operation.MULTIPLY_TOTAL, "multiply_total");

        final AttributeModifier.Operation operation;
        public AttributeModifier.Operation get() {
            return this.operation;
        }

        final String serializedName;

        Operation(AttributeModifier.Operation operation, String serializedName) {
            this.operation = operation;
            this.serializedName = serializedName;
        }

        @Nullable
        public static String getNameFromOperation(AttributeModifier.Operation operation) {
            for (Operation amo : Operation.values()) {
                if (amo.operation == operation)
                    return amo.serializedName;
            }
            return null;
        }
    }
}