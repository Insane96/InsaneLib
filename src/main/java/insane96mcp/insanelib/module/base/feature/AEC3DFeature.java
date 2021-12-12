package insane96mcp.insanelib.module.base.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.entity.AreaEffectCloud3DEntity;
import insane96mcp.insanelib.setup.Config;
import net.minecraft.server.TickTask;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

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

		AreaEffectCloud areaEffectCloud = (AreaEffectCloud) event.getEntity();
		if (areaEffectCloud.effects.isEmpty() && areaEffectCloud.potion.equals(Potions.EMPTY))
			return;
		event.setCanceled(true);
		AreaEffectCloud3DEntity areaEffectCloud3D = new AreaEffectCloud3DEntity(areaEffectCloud);

		BlockableEventLoop<? super TickTask> executor = LogicalSidedProvider.WORKQUEUE.get(event.getWorld().isClientSide ? LogicalSide.CLIENT : LogicalSide.SERVER);
		executor.tell(new TickTask(0, () -> areaEffectCloud3D.level.addFreshEntity(areaEffectCloud3D)));
	}
}
