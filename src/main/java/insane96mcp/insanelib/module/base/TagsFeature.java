package insane96mcp.insanelib.module.base;

import insane96mcp.insanelib.core.ModNBTData;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.mixin.accessor.ExplosionAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.LightLayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@LoadFeature(
		description = "Set and use some tags to get and set some mobs properties. E.g. cause fire explosion for mobs, or get current light level.",
		canBeDisabled = false
)
public class TagsFeature extends Feature {
	public static ResourceLocation EXPLOSION_CAUSES_FIRE;
	public static ResourceLocation EXPERIENCE_MULTIPLIER;
	public static ResourceLocation SKY_LIGHT;
	public static ResourceLocation BLOCK_LIGHT;
	public static ResourceLocation NO_AMMO_CONSUMPTION;

	@Config
	public static Boolean applyNoAmmoConsumptionToPillagers = false;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		EXPLOSION_CAUSES_FIRE = createDataKey("explosion_causes_fire");
		EXPERIENCE_MULTIPLIER = createDataKey("xp_multiplier");
		SKY_LIGHT = createDataKey("sky_light");
		BLOCK_LIGHT = createDataKey("block_light");
		NO_AMMO_CONSUMPTION = this.createDataKey("no_ammo_consumption");
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

	@SubscribeEvent
	public void onEntityJoinLevel(EntityJoinLevelEvent event) {
		applyNoAmmoConsumptionToPillagers(event.getEntity());
	}

	public static void applyNoAmmoConsumptionToPillagers(Entity livingEntity) {
		if (!applyNoAmmoConsumptionToPillagers
				|| !(livingEntity instanceof Pillager))
			return;
		ModNBTData.put(livingEntity, NO_AMMO_CONSUMPTION, true);
	}

	public static void setExplosionCausesFire(boolean causesFire, LivingEntity entity) {
		ModNBTData.put(entity, EXPLOSION_CAUSES_FIRE, causesFire);
	}

	public static void setExperienceMultiplier(double multiplier, LivingEntity entity) {
		ModNBTData.put(entity, EXPERIENCE_MULTIPLIER, multiplier);
	}

	public static boolean isNoAmmoConsumption(LivingEntity livingEntity) {
		return ModNBTData.get(livingEntity, NO_AMMO_CONSUMPTION, Boolean.class);
	}
}
