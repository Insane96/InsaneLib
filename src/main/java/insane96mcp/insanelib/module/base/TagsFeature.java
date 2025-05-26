package insane96mcp.insanelib.module.base;

import insane96mcp.insanelib.InsaneLib;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
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

@LoadFeature(
		module = "insanelib:base",
		description = "Set and use some tags to set some mobs properties. E.g. cause fire explosion for mobs or know if a mob has been spawned from spawner.",
		canBeDisabled = false
)
public class TagsFeature extends Feature {
	public static final String SPAWN_TYPE = InsaneLib.RESOURCE_PREFIX + "spawn_type";
	public static final String EXPLOSION_CAUSES_FIRE = InsaneLib.RESOURCE_PREFIX + "explosion_causes_fire";
	public static final String EXPERIENCE_MULTIPLIER = InsaneLib.RESOURCE_PREFIX + "xp_multiplier";

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
		if (compoundNBT.getBoolean(EXPLOSION_CAUSES_FIRE))
			explosion.fire = true;
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onSpawn(MobSpawnEvent.FinalizeSpawn event) {
		event.getEntity().getPersistentData().putByte(SPAWN_TYPE, (byte) event.getSpawnType().ordinal());
	}

	public static boolean isSpawnType(MobSpawnType spawnType, LivingEntity entity) {
		return entity.getPersistentData().getByte(SPAWN_TYPE) == spawnType.ordinal();
	}

	@SubscribeEvent
	public void onExperienceDrop(LivingExperienceDropEvent event) {
		if (event.getEntity().getPersistentData().contains(EXPERIENCE_MULTIPLIER))
			event.setDroppedExperience((int) (event.getDroppedExperience() * event.getEntity().getPersistentData().getDouble(EXPERIENCE_MULTIPLIER)));
	}
}
