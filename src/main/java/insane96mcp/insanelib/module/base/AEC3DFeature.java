package insane96mcp.insanelib.module.base;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import insane96mcp.insanelib.entity.AreaEffectCloud3DEntity;
import net.minecraft.server.TickTask;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@LoadFeature(module = "insanelib:base")
@Label(name = "Area Effect Cloud 3D", description = "No more boring 2D Area of Effect Clouds")
public class AEC3DFeature extends Feature {

	@Config
	@Label(name = "Replace Vanilla Area of Effect Clouds", description = "If true, vanilla Area of Effect Clouds will be replaced with 3D versions of them")
	public static Boolean replaceVanillaAEC = true;

	public AEC3DFeature(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@Override
	public void readConfig(final ModConfigEvent event) {
		super.readConfig(event);
	}

	@SubscribeEvent
	public void onSpawn(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
				|| !replaceVanillaAEC
				|| !event.getEntity().getType().equals(EntityType.AREA_EFFECT_CLOUD))
			return;

		AreaEffectCloud areaEffectCloud = (AreaEffectCloud) event.getEntity();
		if (areaEffectCloud.effects.isEmpty() && areaEffectCloud.potion.equals(Potions.EMPTY))
			return;
		event.setCanceled(true);
		AreaEffectCloud3DEntity areaEffectCloud3D = new AreaEffectCloud3DEntity(areaEffectCloud);

		BlockableEventLoop<? super TickTask> executor = LogicalSidedProvider.WORKQUEUE.get(event.getLevel().isClientSide ? LogicalSide.CLIENT : LogicalSide.SERVER);
		executor.tell(new TickTask(0, () -> areaEffectCloud3D.level().addFreshEntity(areaEffectCloud3D)));
	}
}
