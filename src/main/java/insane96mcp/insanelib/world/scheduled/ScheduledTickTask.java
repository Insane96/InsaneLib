package insane96mcp.insanelib.world.scheduled;

public abstract class ScheduledTickTask implements Runnable {
	int tickDelay;
	int tick;

	boolean executed = false;

	/**
	 * The task runs on the (tickDelay + 1)th call of {@link #tick()}, so a tickDelay of 0 runs on the first one
	 */
	public ScheduledTickTask(int tickDelay) {
		this.tickDelay = tickDelay;
		this.tick = 0;
	}

	public final void tick() {
		if (this.tick++ >= this.tickDelay) {
			this.run();
			this.executed = true;
		}
	}

	@Override
	public abstract void run();

	protected boolean hasBeenExecuted() {
		return this.executed;
	}
}