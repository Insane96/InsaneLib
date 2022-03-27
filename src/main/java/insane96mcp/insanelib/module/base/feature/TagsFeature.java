package insane96mcp.insanelib.module.base.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.setup.Config;
import insane96mcp.insanelib.setup.ILStrings;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Explosion;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
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
		if (!(explosion.getExploder() instanceof Creeper creeper))
			return;

		CompoundTag compoundNBT = creeper.getPersistentData();
		if (compoundNBT.getBoolean(ILStrings.Tags.EXPLOSION_CAUSES_FIRE))
			explosion.fire = true;
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onSpawn(LivingSpawnEvent.CheckSpawn event) {
		if (event.getSpawnReason() == MobSpawnType.SPAWNER)
			event.getEntityLiving().getPersistentData().putBoolean(ILStrings.Tags.SPAWNED_FROM_SPAWNER, true);
		if (event.getSpawnReason() == MobSpawnType.STRUCTURE)
			event.getEntityLiving().getPersistentData().putBoolean(ILStrings.Tags.SPAWNED_FROM_STRUCTURE, true);
	}

	@SubscribeEvent
	public void onExperienceDrop(LivingExperienceDropEvent event) {
		if (event.getEntityLiving().getPersistentData().contains(ILStrings.Tags.EXPERIENCE_MULTIPLIER))
			event.setDroppedExperience((int) (event.getDroppedExperience() * event.getEntityLiving().getPersistentData().getDouble(ILStrings.Tags.EXPERIENCE_MULTIPLIER)));
	}
}
