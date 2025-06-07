package insane96mcp.insanelib.network.message;

import insane96mcp.insanelib.network.NetworkHandler;
import insane96mcp.insanelib.util.ModNBTData;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class EntityModNBTDataSync<T> {
    final int entityId;
    final ResourceLocation path;
    final int type;
    final T value;

    public EntityModNBTDataSync(int entityId, ResourceLocation path, int type, T value) {
        this.entityId = entityId;
        this.path = path;
        this.type = type;
        this.value = value;
    }

    public static <T> void encode(EntityModNBTDataSync<T> pkt, FriendlyByteBuf buf) {
        buf.writeInt(pkt.entityId);
        buf.writeResourceLocation(pkt.path);
        buf.writeInt(pkt.type);
        switch (pkt.type) {
            case Tag.TAG_BYTE -> buf.writeByte((Byte) pkt.value);
            case Tag.TAG_SHORT -> buf.writeShort((Short) pkt.value);
            case Tag.TAG_INT -> buf.writeInt((Integer) pkt.value);
            case Tag.TAG_LONG -> buf.writeLong((Long) pkt.value);
            case Tag.TAG_FLOAT -> buf.writeFloat((Float) pkt.value);
            case Tag.TAG_DOUBLE -> buf.writeDouble((Double) pkt.value);
            case Tag.TAG_BYTE_ARRAY -> buf.writeByteArray((byte[]) pkt.value);
            case Tag.TAG_STRING -> buf.writeUtf((String) pkt.value);
            case Tag.TAG_COMPOUND -> buf.writeNbt((CompoundTag) pkt.value);
            default -> throw new IllegalArgumentException("Unsupported tag type for sync: " + pkt.type);
        }
    }

    public static EntityModNBTDataSync<?> decode(FriendlyByteBuf buf) {
        int entityId = buf.readInt();
        ResourceLocation path = buf.readResourceLocation();
        int type = buf.readInt();
        Object value;

        switch (type) {
            case Tag.TAG_BYTE -> value = buf.readByte();
            case Tag.TAG_SHORT -> value = buf.readShort();
            case Tag.TAG_INT -> value = buf.readInt();
            case Tag.TAG_LONG -> value = buf.readLong();
            case Tag.TAG_FLOAT -> value = buf.readFloat();
            case Tag.TAG_DOUBLE -> value = buf.readDouble();
            case Tag.TAG_BYTE_ARRAY -> value = buf.readByteArray();
            case Tag.TAG_STRING -> value = buf.readUtf();
            case Tag.TAG_COMPOUND -> value = buf.readNbt();
            default -> throw new IllegalArgumentException("Unsupported tag type for sync: " + type);
        }

        return new EntityModNBTDataSync<>(entityId, path, type, value);
    }

    public static void handle(EntityModNBTDataSync<?> message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Entity entity = Minecraft.getInstance().level.getEntity(message.entityId);
            if (!(entity instanceof LivingEntity livingEntity))
                return;

            ModNBTData.put(livingEntity, message.path, message.value);
        });
        ctx.get().setPacketHandled(true);
    }

    public static void sync(LivingEntity entity, ResourceLocation path, Class<?> type) {
        if (!(entity.level() instanceof ServerLevel serverLevel))
            return;
        serverLevel.players().forEach(player ->
                sync(player, entity, path, type));
    }

    public static <T> void sync(ServerPlayer player, LivingEntity entity, ResourceLocation path, Class<T> type) {
        T value = ModNBTData.get(entity, path, type);
        EntityModNBTDataSync<T> msg = new EntityModNBTDataSync<>(entity.getId(), path, ModNBTData.classToNBTType(type), value);
        NetworkHandler.CHANNEL.sendTo(msg, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }
}
