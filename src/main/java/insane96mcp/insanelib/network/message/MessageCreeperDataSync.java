package insane96mcp.insanelib.network.message;

import insane96mcp.insanelib.InsaneLib;
import insane96mcp.insanelib.mixin.accessor.CreeperAccessor;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Creeper;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MessageCreeperDataSync(int id, int maxSwell, int explosionRadius) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MessageCreeperDataSync> TYPE =
            new CustomPacketPayload.Type<>(InsaneLib.location("creeper_data_sync"));

    public static final StreamCodec<ByteBuf, MessageCreeperDataSync> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, MessageCreeperDataSync::id,
            ByteBufCodecs.VAR_INT, MessageCreeperDataSync::maxSwell,
            ByteBufCodecs.VAR_INT, MessageCreeperDataSync::explosionRadius,
            MessageCreeperDataSync::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final MessageCreeperDataSync payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            var level = Minecraft.getInstance().level;
            if (level == null) return;
            Entity entity = level.getEntity(payload.id());
            if (entity instanceof Creeper creeper) {
                ((CreeperAccessor) creeper).setMaxSwell(payload.maxSwell());
                ((CreeperAccessor) creeper).setExplosionRadius(payload.explosionRadius());
            }
        });
    }

    public static void syncCreeperToPlayers(Creeper creeper) {
        MessageCreeperDataSync msg = new MessageCreeperDataSync(
                creeper.getId(),
                ((CreeperAccessor) creeper).getMaxSwell(),
                ((CreeperAccessor) creeper).getExplosionRadius()
        );
        PacketDistributor.sendToPlayersTrackingEntity(creeper, msg);
    }
}
