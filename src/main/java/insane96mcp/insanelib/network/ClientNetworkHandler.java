package insane96mcp.insanelib.network;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.TickTask;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.fml.LogicalSide;

@OnlyIn(Dist.CLIENT)
public class ClientNetworkHandler {
    public static void handleCreeperDataSyncMessage(int id, short fuse, byte explosionRadius) {
        BlockableEventLoop<? super TickTask> executor = LogicalSidedProvider.WORKQUEUE.get(LogicalSide.CLIENT);
        executor.tell(new TickTask(0, () -> {
            Level level = Minecraft.getInstance().level;
            //noinspection ConstantConditions
            Entity entity = level.getEntity(id);
            if (entity instanceof Creeper creeper) {
                CompoundTag nbt = new CompoundTag();
                creeper.addAdditionalSaveData(nbt);
                nbt.putShort("Fuse", fuse);
                nbt.putShort("ExplosionRadius", explosionRadius);
                creeper.readAdditionalSaveData(nbt);
            }
        }));
    }
}
