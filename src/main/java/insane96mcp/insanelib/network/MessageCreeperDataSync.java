package insane96mcp.insanelib.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageCreeperDataSync {
    int id;
    short fuse;
    byte explosionRadius;

    public MessageCreeperDataSync(int id, short fuse, byte explosionRadius) {
        this.id = id;
        this.fuse = fuse;
        this.explosionRadius = explosionRadius;
    }

    public static void encode(MessageCreeperDataSync pkt, FriendlyByteBuf buf) {
        buf.writeInt(pkt.id);
        buf.writeShort(pkt.fuse);
        buf.writeByte(pkt.explosionRadius);
    }

    public static MessageCreeperDataSync decode(FriendlyByteBuf buf) {
        return new MessageCreeperDataSync(buf.readInt(), buf.readShort(), buf.readByte());
    }

    public static void handle(final MessageCreeperDataSync message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientNetworkHandler.handleCreeperDataSyncMessage(message.id, message.fuse, message.explosionRadius));
        ctx.get().setPacketHandled(true);
    }

    public static void syncCreeperToPlayers(Creeper creeper) {
        CompoundTag compound = new CompoundTag();
        creeper.addAdditionalSaveData(compound);

        Object msg = new MessageCreeperDataSync(creeper.getId(), compound.getShort("Fuse"), compound.getByte("ExplosionRadius"));
        for (Player player : creeper.level().players()) {
            NetworkHandler.CHANNEL.sendTo(msg, ((ServerPlayer) player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
        }
    }
}
