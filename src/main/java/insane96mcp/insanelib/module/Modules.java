package insane96mcp.insanelib.module;

import insane96mcp.insanelib.module.base.BaseModule;

public class Modules {
	public static BaseModule base;

	public static void init() {
		base = new BaseModule();
	}

	public static void loadConfig() {
		base.loadConfig();
	}
}