package insane96mcp.insanelib.module.base;

import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.module.base.feature.AEC3DFeature;
import insane96mcp.insanelib.module.base.feature.FixFeature;
import insane96mcp.insanelib.module.base.feature.TagsFeature;
import insane96mcp.insanelib.setup.Config;

@Label(name = "Base")
public class BaseModule extends Module {
	public AEC3DFeature aec3D;
	public FixFeature fix;
	public TagsFeature tags;

	public BaseModule() {
		super(Config.builder, true, false);
		this.pushConfig();
		aec3D = new AEC3DFeature(this);
		fix = new FixFeature(this);
		tags = new TagsFeature(this);
		this.popConfig();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		aec3D.loadConfig();
		fix.loadConfig();
		tags.loadConfig();
	}
}
