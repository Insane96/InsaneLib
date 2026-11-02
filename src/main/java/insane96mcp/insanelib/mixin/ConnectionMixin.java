package insane96mcp.insanelib.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import insane96mcp.insanelib.module.base.Fixes;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.PacketListener;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Connection.class)
public class ConnectionMixin {
    /**
     * The status protocol has no disconnect packet, so when a status connection fails (e.g. a server list ping that resets the connection) encoding it logs an error. Disconnects without sending it, as the send would've done after failing.
     */
    @WrapOperation(method = "exceptionCaught", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;)V"))
    private void onExceptionCaughtSendDisconnect(Connection connection, Packet<?> packet, PacketSendListener listener, Operation<Void> original) {
        PacketListener packetListener = connection.getPacketListener();
        if (Fixes.shouldFixStatusDisconnectError() && packetListener != null && packetListener.protocol() == ConnectionProtocol.STATUS) {
            listener.onSuccess();
            return;
        }
        original.call(connection, packet, listener);
    }
}
