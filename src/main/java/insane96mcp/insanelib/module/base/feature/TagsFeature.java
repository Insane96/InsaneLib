package insane96mcp.insanelib.module.base.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.setup.Config;
import insane96mcp.insanelib.setup.Strings;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.Explosion;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Tags", description = "Set and use some tags to set some mobs properties. E.g. cause fire explosion for mobs or get if a mob has been spawned from spawner.")
public class TagsFeature extends Feature {

	public TagsFeature(Module module) {
		super(Config.builder, module, true, false);
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onExplosionStart(ExplosionEvent.Start event) {
		if (!this.isEnabled())
			return;

		Explosion explosion = event.getExplosion();
		if (!(explosion.getExploder() instanceof CreeperEntity))
			return;

		CreeperEntity creeper = (CreeperEntity) explosion.getExploder();

		CompoundNBT compoundNBT = creeper.getPersistentData();
		if (compoundNBT.getBoolean(Strings.Tags.EXPLOSION_CAUSES_FIRE))
			explosion.causesFire = true;
	}
}
