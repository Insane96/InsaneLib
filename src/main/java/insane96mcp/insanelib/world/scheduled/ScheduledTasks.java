package insane96mcp.insanelib.world.scheduled;

import insane96mcp.insanelib.InsaneLib;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = InsaneLib.MOD_ID)
public class ScheduledTasks {
	public static List<ScheduledTickTask> scheduledTickTasks = new ArrayList<>();

	@SubscribeEvent
	static void onServerTick(ServerTickEvent.Post event) {
		List<ScheduledTickTask> listCopy = new ArrayList<>(scheduledTickTasks);
		for (ScheduledTickTask task : listCopy)
			task.tick();
		scheduledTickTasks.removeIf(ScheduledTickTask::hasBeenExecuted);
	}

    @SubscribeEvent
    static void onServerStop(ServerStoppedEvent event) {
        scheduledTickTasks.clear();
    }

	public static void schedule(ScheduledTickTask task) {
		scheduledTickTasks.add(task);
	}
}
