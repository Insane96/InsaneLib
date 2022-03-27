package insane96mcp.insanelib.util.scheduled;

import insane96mcp.insanelib.InsaneLib;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = InsaneLib.MOD_ID)
public class ScheduledTasks {
	public static List<ScheduledTickTask> scheduledTickTasks = new ArrayList<>();

	@SubscribeEvent
	static void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase.equals(TickEvent.Phase.END)) {
			for (ScheduledTickTask task : scheduledTickTasks) {
				task.tick();
			}
			scheduledTickTasks.removeIf(ScheduledTickTask::hasBeenExecuted);
		}
	}

	public static void schedule(ScheduledTickTask task) {
		scheduledTickTasks.add(task);
	}
}
