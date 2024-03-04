package insane96mcp.insanelib.module.base;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.LoadFeature;
import insane96mcp.insanelib.setup.ILStrings;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Explosion;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@LoadFeature(module = "insanelib:base", canBeDisabled = false)
@Label(name = "Tags", description = "Set and use some tags to set some mobs properties. E.g. cause fire explosion for mobs or get if a mob has been spawned from spawner.")
public class TagsFeature extends Feature {

	public TagsFeature(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@Override
	public void readConfig(final ModConfigEvent event) {
		super.readConfig(event);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onExplosionStart(ExplosionEvent.Start event) {
		if (!this.isEnabled())
			return;

		Explosion explosion = event.getExplosion();
		if (!(explosion.getExploder() instanceof LivingEntity entity))
			return;

		CompoundTag compoundNBT = entity.getPersistentData();
		if (compoundNBT.getBoolean(ILStrings.Tags.EXPLOSION_CAUSES_FIRE))
			explosion.fire = true;
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onSpawn(MobSpawnEvent.FinalizeSpawn event) {
		if (event.getSpawnType() == MobSpawnType.SPAWNER)
			event.getEntity().getPersistentData().putBoolean(ILStrings.Tags.SPAWNED_FROM_SPAWNER, true);
		if (event.getSpawnType() == MobSpawnType.STRUCTURE)
			event.getEntity().getPersistentData().putBoolean(ILStrings.Tags.SPAWNED_FROM_STRUCTURE, true);
	}

	@SubscribeEvent
	public void onExperienceDrop(LivingExperienceDropEvent event) {
		if (event.getEntity().getPersistentData().contains(ILStrings.Tags.EXPERIENCE_MULTIPLIER))
			event.setDroppedExperience((int) (event.getDroppedExperience() * event.getEntity().getPersistentData().getDouble(ILStrings.Tags.EXPERIENCE_MULTIPLIER)));
	}
}
