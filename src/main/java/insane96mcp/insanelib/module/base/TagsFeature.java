package insane96mcp.insanelib.module.base;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.util.ModNBTData;
import net.minecraft.resources.ResourceLocation;
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
	public static ResourceLocation SPAWN_TYPE;
	public static ResourceLocation EXPLOSION_CAUSES_FIRE;
	public static ResourceLocation EXPERIENCE_MULTIPLIER;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		SPAWN_TYPE = createDataKey("spawn_type");
		EXPLOSION_CAUSES_FIRE = createDataKey("explosion_causes_fire");
		EXPERIENCE_MULTIPLIER = createDataKey("xp_multiplier");
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

		if (ModNBTData.get(entity, EXPLOSION_CAUSES_FIRE, Boolean.class))
			explosion.fire = true;
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onSpawn(MobSpawnEvent.FinalizeSpawn event) {
		ModNBTData.put(event.getEntity(), SPAWN_TYPE, (byte) event.getSpawnType().ordinal());
	}

	@SubscribeEvent
	public void onExperienceDrop(LivingExperienceDropEvent event) {
		if (ModNBTData.contains(event.getEntity(), EXPERIENCE_MULTIPLIER))
			event.setDroppedExperience((int) (event.getDroppedExperience() * ModNBTData.get(event.getEntity(), EXPERIENCE_MULTIPLIER, Double.class)));
	}

	public static boolean isSpawnType(MobSpawnType spawnType, LivingEntity entity) {
		return ModNBTData.get(entity, SPAWN_TYPE, Byte.class) == spawnType.ordinal();
	}

	public static void setExplosionCausesFire(boolean causesFire, LivingEntity entity) {
		ModNBTData.put(entity, EXPLOSION_CAUSES_FIRE, causesFire);
	}

	public static void setExperienceMultiplier(double multiplier, LivingEntity entity) {
		ModNBTData.put(entity, EXPERIENCE_MULTIPLIER, multiplier);
	}
}
