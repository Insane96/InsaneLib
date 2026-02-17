package insane96mcp.insanelib.module.base;

import insane96mcp.insanelib.core.ModNBTData;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.mixin.accessor.ExplosionAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.LightLayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@LoadFeature(
		module = "insanelib:base",
		description = "Set and use some tags to get and set some mobs properties. E.g. cause fire explosion for mobs or know if a mob has been spawned from spawner.",
		canBeDisabled = false
)
public class TagsFeature extends Feature {
	public static ResourceLocation SPAWN_TYPE;
	public static ResourceLocation EXPLOSION_CAUSES_FIRE;
	public static ResourceLocation EXPERIENCE_MULTIPLIER;
	public static ResourceLocation SKY_LIGHT;
	public static ResourceLocation BLOCK_LIGHT;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		SPAWN_TYPE = createDataKey("spawn_type");
		EXPLOSION_CAUSES_FIRE = createDataKey("explosion_causes_fire");
		EXPERIENCE_MULTIPLIER = createDataKey("xp_multiplier");
		SKY_LIGHT = createDataKey("sky_light");
		BLOCK_LIGHT = createDataKey("block_light");
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onExplosionStart(ExplosionEvent.Start event) {
		if (!this.isEnabled())
			return;

		Explosion explosion = event.getExplosion();
		if (!(explosion.getDirectSourceEntity() instanceof LivingEntity entity))
			return;

		if (ModNBTData.get(entity, EXPLOSION_CAUSES_FIRE, Boolean.class))
			((ExplosionAccessor)explosion).setFire(true);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onSpawn(FinalizeSpawnEvent event) {
		ModNBTData.put(event.getEntity(), SPAWN_TYPE, (byte) event.getSpawnType().ordinal());
	}

	@SubscribeEvent
	public void onExperienceDrop(LivingExperienceDropEvent event) {
		if (ModNBTData.contains(event.getEntity(), EXPERIENCE_MULTIPLIER))
			event.setDroppedExperience((int) (event.getDroppedExperience() * ModNBTData.get(event.getEntity(), EXPERIENCE_MULTIPLIER, Double.class)));
	}

	@SubscribeEvent
	public void onLivingTick(EntityTickEvent.Pre event) {
		if (event.getEntity().level().isClientSide
				|| event.getEntity().getServer() == null
				|| (event.getEntity().getServer().getTickCount() + event.getEntity().getId()) % 2 == 0)
			return;

		ModNBTData.put(event.getEntity(), SKY_LIGHT, event.getEntity().level().getBrightness(LightLayer.SKY, event.getEntity().blockPosition()));
		ModNBTData.put(event.getEntity(), BLOCK_LIGHT, event.getEntity().level().getBrightness(LightLayer.BLOCK, event.getEntity().blockPosition()));
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
