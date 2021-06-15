package insane96mcp.insanelib.module.base;

import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.module.base.feature.AEC3DFeature;
import insane96mcp.insanelib.setup.Config;

@Label(name = "Base")
public class BaseModule extends Module {
	public AEC3DFeature aec3D;

	public BaseModule() {
		super(Config.builder);
		pushConfig(Config.builder);
		//Must be the first one to be initialized, otherwise the other modules will not get the correct difficulty settings
		aec3D = new AEC3DFeature(this);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		aec3D.loadConfig();
	}
}
