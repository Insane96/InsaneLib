package insane96mcp.insanelib.network.message;

import insane96mcp.insanelib.InsaneLib;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.config.ConfigTracker;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.config.ModConfigs;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

import java.io.IOException;
import java.nio.file.Files;

/**
 * NeoForge syncs SERVER configs only on login. This re-sends a SERVER config to the connected clients when the server
 * reloads it because the file has been changed.
 */
public record ServerConfigSyncMessage(String fileName, byte[] contents) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ServerConfigSyncMessage> TYPE =
            new CustomPacketPayload.Type<>(InsaneLib.id("server_config_sync"));

    public static final StreamCodec<FriendlyByteBuf, ServerConfigSyncMessage> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ServerConfigSyncMessage decode(FriendlyByteBuf buf) {
            return new ServerConfigSyncMessage(buf.readUtf(), buf.readByteArray());
        }

        @Override
        public void encode(FriendlyByteBuf buf, ServerConfigSyncMessage msg) {
            buf.writeUtf(msg.fileName());
            buf.writeByteArray(msg.contents());
        }
    };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final ServerConfigSyncMessage payload, final IPayloadContext context) {
        // Singleplayer / LAN host share the config with the server
        if (context.connection().isMemoryConnection())
            return;
        context.enqueueWork(() -> {
            ModConfig modConfig = ModConfigs.getFileMap().get(payload.fileName());
            if (modConfig == null || modConfig.getType() != ModConfig.Type.SERVER) {
                InsaneLib.LOGGER.warn("Received server config sync for unknown server config {}", payload.fileName());
                return;
            }
            ConfigTracker.acceptSyncedConfig(modConfig, payload.contents());
        });
    }

    /**
     * Sends the config to all the connected players and notifies operators. Must be called on the server thread.
     */
    public static void syncToAll(ModConfig modConfig, MinecraftServer server) {
        byte[] contents;
        try {
            contents = Files.readAllBytes(modConfig.getFullPath());
        }
        catch (IOException | IllegalStateException e) {
            InsaneLib.LOGGER.error("Failed to read server config {} to sync it to clients", modConfig.getFileName(), e);
            return;
        }
        ServerConfigSyncMessage message = new ServerConfigSyncMessage(modConfig.getFileName(), contents);
        int synced = 0;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!NetworkRegistry.hasChannel(player.connection, TYPE.id()))
                continue;
            PacketDistributor.sendToPlayer(player, message);
            if (!player.connection.getConnection().isMemoryConnection())
                synced++;
        }
        InsaneLib.LOGGER.info("Server config {} reloaded and synced to {} players", modConfig.getFileName(), synced);
        Component notification = Component.literal("Server config %s reloaded and synced to %d players".formatted(modConfig.getFileName(), synced)).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (server.getPlayerList().isOp(player.nameAndId()))
                player.sendSystemMessage(notification);
        }
    }
}
