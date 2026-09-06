package insane96mcp.insanelib.module;

import insane96mcp.insanelib.core.ModNBTData;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.mixin.accessor.ExplosionAccessor;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;

@LoadFeature(canBeDisabled = false, description = "Allows to override fuse and explosion sounds via NBT. Fuse sound can be overridden by setting the NBT tag 'NeoForgeData.insanelib.sound_overrides.fuse_sound' on the entity. Explosion sound can be overridden by setting the NBT tag 'NeoForgeData.insanelib.sound_overrides.explosion_sound' on the entity.")
public class SoundOverrides extends Feature {
	public static ResourceLocation FUSE_SOUND;
	public static ResourceLocation EXPLOSION_SOUND;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		FUSE_SOUND = this.createDataKey("fuse_sound");
		EXPLOSION_SOUND = this.createDataKey("explosion_sound");
	}

	@SubscribeEvent
	public void onExplosion(ExplosionEvent.Start event) {
		if (event.getExplosion().getDirectSourceEntity() == null
				|| !ModNBTData.contains(event.getExplosion().getDirectSourceEntity(), EXPLOSION_SOUND))
			return;
		String sSoundEvent = ModNBTData.get(event.getExplosion().getDirectSourceEntity(), EXPLOSION_SOUND, String.class);
		Holder<SoundEvent> sound = Holder.direct(SoundEvent.createVariableRangeEvent(ResourceLocation.parse(sSoundEvent)));
		((ExplosionAccessor) event.getExplosion()).setExplosionSound(sound);
	}

	public static void setExplosionSound(Entity entity, Holder<SoundEvent> sound) {
		ModNBTData.put(entity, EXPLOSION_SOUND, sound.value().getLocation().toString());
	}

	public static void clearExplosionSound(Entity entity) {
		ModNBTData.remove(entity, EXPLOSION_SOUND);
	}

	public static void setFuseSound(Entity entity, Holder<SoundEvent> sound) {
		ModNBTData.put(entity, FUSE_SOUND, sound.value().getLocation().toString());
	}

	public static void clearFuseSound(Entity entity) {
		ModNBTData.remove(entity, FUSE_SOUND);
	}

	/**
	 * Called from mixins right where a fuse sound (TNT_PRIMED, CREEPER_PRIMED, ...) is about to be played,
	 * to swap it with the one set via {@link #setFuseSound(Entity, Holder)} on that specific entity, if any.
	 */
	public static SoundEvent getFuseSound(Entity entity, SoundEvent defaultSound) {
		if (entity == null || !ModNBTData.contains(entity, FUSE_SOUND))
			return defaultSound;
		String sSoundEvent = ModNBTData.get(entity, FUSE_SOUND, String.class);
		return SoundEvent.createVariableRangeEvent(ResourceLocation.parse(sSoundEvent));
	}
}
