package insane96mcp.insanelib.module.base.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.entity.AreaEffectCloud3DEntity;
import insane96mcp.insanelib.setup.Config;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.EntityType;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Area Effect Cloud 3D", description = "No more boring 3D Area of Effect Clouds")
public class AEC3DFeature extends Feature {

	private final ForgeConfigSpec.ConfigValue<Boolean> replaceVanillaAECConfig;

	public boolean replaceVanillaAEC = true;

	public AEC3DFeature(Module module) {
		super(Config.builder, module);
		Config.builder.comment(this.getDescription()).push(this.getName());
		replaceVanillaAECConfig = Config.builder
				.comment("If true, vanilla Area of Effect Clouds will be replaced with 3D versions of them")
				.define("Replace Vanilla Area of Effect Clouds", replaceVanillaAEC);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.replaceVanillaAEC = this.replaceVanillaAECConfig.get();
	}

	@SubscribeEvent
	public void onSpawn(EntityJoinWorldEvent event) {
		if (!this.isEnabled())
			return;

		if (!event.getEntity().getType().equals(EntityType.AREA_EFFECT_CLOUD))
			return;

		AreaEffectCloudEntity areaEffectCloud = (AreaEffectCloudEntity) event.getEntity();
		AreaEffectCloud3DEntity areaEffectCloud3D = new AreaEffectCloud3DEntity(areaEffectCloud);
		areaEffectCloud.remove();
		areaEffectCloud3D.world.addEntity(areaEffectCloud3D);
	}
}
