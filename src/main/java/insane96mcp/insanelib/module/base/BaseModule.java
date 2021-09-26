package insane96mcp.insanelib.module.base;

import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.module.base.feature.AEC3DFeature;
import insane96mcp.insanelib.module.base.feature.FixFeature;
import insane96mcp.insanelib.setup.Config;

@Label(name = "Base")
public class BaseModule extends Module {
	public AEC3DFeature aec3D;
	public FixFeature fix;

	public BaseModule() {
		super(Config.builder, true, false);
		pushConfig(Config.builder);
		aec3D = new AEC3DFeature(this);
		fix = new FixFeature(this);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		aec3D.loadConfig();
		fix.loadConfig();
	}
}
