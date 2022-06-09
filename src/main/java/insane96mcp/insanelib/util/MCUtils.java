package insane96mcp.insanelib.util;

import net.minecraft.advancements.Advancement;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.math.NumberUtils;

import javax.annotation.Nullable;
import java.util.*;

public class MCUtils {
	/**
	 * Returns the current speed of the player compared to his normal speed
	 */
	public static double getMovementSpeedRatio(Player player) {
		double baseMS = 0.1d;
		if (player.isSprinting())
			baseMS += 0.03f;
		double playerMS = player.getAttributeValue(Attributes.MOVEMENT_SPEED);
		return playerMS / baseMS;
	}

	/**
	 * Different version of ItemStack#addAttributeModifiers that doesn't override the item's base modifiers
	 */
	public static void addAttributeModifierToItemStack(ItemStack itemStack, Attribute attribute, AttributeModifier modifier, EquipmentSlot modifierSlot) {
		if (itemStack.hasTag() && !itemStack.getTag().contains("AttributeModifiers", 9)) {
			for (Map.Entry<Attribute, AttributeModifier> entry : itemStack.getAttributeModifiers(modifierSlot).entries()) {
				itemStack.addAttributeModifier(entry.getKey(), entry.getValue(), modifierSlot);
			}
		}
		itemStack.addAttributeModifier(attribute, modifier, modifierSlot);
	}

	/**
	 * Applies a modifier to the Living Entity. If the attribute is max_health also sets entity's health to his max health
	 * @return true if the modifier was applied
	 */
	public static boolean applyModifier(LivingEntity entity, Attribute attribute, UUID uuid, String name, double amount, AttributeModifier.Operation operation, boolean permanent) {
		AttributeInstance attributeInstance = entity.getAttribute(attribute);
		if (attributeInstance != null) {
			if (attributeInstance.getModifier(uuid) != null)
				return false;
			AttributeModifier modifier = new AttributeModifier(uuid, name, amount, operation);
			if (permanent)
				attributeInstance.addPermanentModifier(modifier);
			else
				attributeInstance.addTransientModifier(modifier);

			if (attribute == Attributes.MAX_HEALTH)
				entity.setHealth(entity.getMaxHealth());
			return true;
		}
		return false;
	}

	/**
	 * Applies a permanent modifier to the Living Entity. If the attribute is max_health also sets entity's health to his max health
	 * @return true if the modifier was applied
	 */
	public static boolean applyModifier(LivingEntity entity, Attribute attribute, UUID uuid, String name, double amount, AttributeModifier.Operation operation) {
		return applyModifier(entity, attribute, uuid, name, amount, operation, true);
	}

	/**
	 * Sets the value of an attribute
	 * @return true if the override was successful
	 */
	public static boolean setAttributeValue(LivingEntity entity, Attribute attribute, double value) {
		AttributeInstance attributeInstance = entity.getAttribute(attribute);
		if (attributeInstance != null) {
			attributeInstance.setBaseValue(value);

			if (attribute == Attributes.MAX_HEALTH)
				entity.setHealth(entity.getMaxHealth());

			return true;
		}
		return false;
	}

	public static boolean hurtIgnoreInvuln(LivingEntity hurtEntity, DamageSource source, float amount) {
		int hurtResistantTime = hurtEntity.invulnerableTime;
		hurtEntity.invulnerableTime = 0;
		boolean attacked = hurtEntity.hurt(source, amount);
		hurtEntity.invulnerableTime = hurtResistantTime;
		return attacked;
	}

	/**
	 * Checks if nbt1 tags are all present in and match nbt2
	 * @param nbt1
	 * @param nbt2
	 * @return
	 */
	public static boolean compareNBT(CompoundTag nbt1, CompoundTag nbt2) {
		for (String key : nbt1.getAllKeys()) {
			if (!nbt2.contains(key))
				return false;

			if (nbt1.get(key) instanceof CompoundTag && nbt2.get(key) instanceof CompoundTag) {
				if (!compareNBT(nbt1.getCompound(key), nbt2.getCompound(key)))
					return false;
			}
			else if (!nbt1.get(key).equals(nbt2.get(key)))
				return false;
		}
		return true;
	}

	public static boolean isAdvancementDone(ServerPlayer player, ResourceLocation advancementRL) {
		Advancement advancement = player.server.getAdvancements().getAdvancement(advancementRL);
		if (advancement == null)
			return false;

		return player.getAdvancements().getOrStartProgress(advancement).isDone();
	}

	/**
	 * Copy-paste of PotionUtils.setCustomEffects but setting the potion color too
	 */
	public static ItemStack setCustomEffects(ItemStack itemStack, Collection<MobEffectInstance> mobEffectInstances) {
		if (!mobEffectInstances.isEmpty()) {
			CompoundTag compoundtag = itemStack.getOrCreateTag();
			ListTag listtag = compoundtag.getList("CustomPotionEffects", 9);

			for (MobEffectInstance mobeffectinstance : mobEffectInstances) {
				listtag.add(mobeffectinstance.save(new CompoundTag()));
			}
			compoundtag.putInt(PotionUtils.TAG_CUSTOM_POTION_COLOR, PotionUtils.getColor(mobEffectInstances));

			compoundtag.put("CustomPotionEffects", listtag);

			itemStack.setHoverName(new TranslatableComponent("unknown_potion"));
		}
		return itemStack;
	}

	/**
	 * Returns true if the entity has a HARMFUL effect
	 */
	public static boolean hasNegativeEffect(LivingEntity entity) {
		for (MobEffectInstance mobEffectInstance : entity.getActiveEffects()) {
			if (entity.hasEffect(mobEffectInstance.getEffect()) && mobEffectInstance.getEffect().getCategory().equals(MobEffectCategory.HARMFUL))
				return true;
		}
		return false;
	}

	/**
	 * Same as hasNegativeEffect but also checks if the duration of the effect is higher than 7.5 seconds
	 */
	public static boolean hasLongNegativeEffect(LivingEntity entity) {
		for (MobEffectInstance mobEffectInstance : entity.getActiveEffects()) {
			if (entity.hasEffect(mobEffectInstance.getEffect()) && mobEffectInstance.getEffect().getCategory().equals(MobEffectCategory.HARMFUL) && mobEffectInstance.getDuration() > 150)
				return true;
		}
		return false;
	}

	/**
	 * Returns a spawnable Y spot for the entity at the given x, y, z. Returns level.getMinBuildHeight() - 1 when no spawn spots are found, otherwise the Y coord
	 */
	public static int getFittingY(EntityType<?> entityType, BlockPos pos, Level level, int minRelativeY) {
		int height = (int) Math.ceil(entityType.getHeight());
		int fittingYPos = level.getMinBuildHeight() - 1;
		for (int y = pos.getY(); y > pos.getY() - minRelativeY; y--) {
			boolean viable = true;
			BlockPos p = new BlockPos(pos.getX(), y, pos.getZ());
			for (int i = 0; i < height; i++) {
				if (level.getBlockState(p.above(i)).getMaterial().blocksMotion()) {
					viable = false;
					break;
				}
			}
			if (!viable)
				continue;
			fittingYPos = y;
			if (!level.getBlockState(p.below()).getMaterial().blocksMotion())
				continue;
			return y;
		}
		return fittingYPos;
	}

	public static boolean isDamageSourceBlocked(DamageSource damageSource, LivingEntity livingEntity) {
		Entity entity = damageSource.getDirectEntity();
		boolean flag = false;
		if (entity instanceof AbstractArrow abstractArrow) {
			if (abstractArrow.getPierceLevel() > 0) {
				flag = true;
			}
		}

		if (!damageSource.isBypassArmor() && livingEntity.isBlocking() && !flag) {
			Vec3 sourcePosition = damageSource.getSourcePosition();
			if (sourcePosition != null) {
				Vec3 livingEntityViewVector = livingEntity.getViewVector(1.0F);
				Vec3 vec3 = sourcePosition.vectorTo(livingEntity.position()).normalize();
				vec3 = new Vec3(vec3.x, 0.0D, vec3.z);
				return vec3.dot(livingEntityViewVector) < 0.0D;
			}
		}
		return false;
	}

	public static int getEnchantmentLevel(ResourceLocation enchantmentId, ItemStack stack) {
		if (stack.isEmpty())
			return 0;
		ListTag listTag = stack.getEnchantmentTags();
		for (int i = 0; i < listTag.size(); ++i) {
			CompoundTag compoundTag = listTag.getCompound(i);
			ResourceLocation itemEnchantment = ResourceLocation.tryParse(compoundTag.getString("id"));
			if (itemEnchantment != null && itemEnchantment.equals(enchantmentId)) {
				return Mth.clamp(compoundTag.getInt("lvl"), 0, 255);
			}
		}
		return 0;
	}

	/**
	 * Creates a MobEffectInstance with the possibility to prevent it from begin cured
	 */
	public static MobEffectInstance createEffectInstance(MobEffect potion, int duration, int amplifier, boolean ambient, boolean showParticles, boolean showIcon, boolean canBeCured) {
		MobEffectInstance effectInstance = new MobEffectInstance(potion, duration, amplifier, ambient, showParticles, showIcon);
		if (!canBeCured)
			effectInstance.setCurativeItems(new ArrayList<>());
		return effectInstance;
	}

	public static ArrayList<MobEffectInstance> parseMobEffectsList(List<? extends String> list) {
		ArrayList<MobEffectInstance> mobEffectInstances = new ArrayList<>();
		for (String s : list) {
			MobEffectInstance mobEffectInstance = parseEffectInstance(s);
			if (mobEffectInstance != null)
				mobEffectInstances.add(mobEffectInstance);
		}
		return mobEffectInstances;
	}

	/**
	 * Parses a string with the following format effect_id,duration,amplifier
	 */
	@Nullable
	public static MobEffectInstance parseEffectInstance(String s) {
		String[] split = s.split(",");
		if (split.length != 3) {
			LogHelper.warn("Invalid Mob Effect \"%s\"", s);
			return null;
		}

		ResourceLocation effectRL = ResourceLocation.tryParse(split[0]);
		if (effectRL == null) {
			LogHelper.warn("%s mob effect is not valid", split[0]);
			return null;
		}
		if (!ForgeRegistries.MOB_EFFECTS.containsKey(effectRL)) {
			LogHelper.warn("%s mob effect seems to not exist", split[0]);
			return null;
		}
		MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(effectRL);

		//Duration
		if (!NumberUtils.isParsable(split[1])) {
			LogHelper.warn(String.format("Invalid duration \"%s\" for Mob Effect", s));
			return null;
		}
		int duration = Integer.parseInt(split[1]);

		//Amplifier
		if (!NumberUtils.isParsable(split[2])) {
			LogHelper.warn(String.format("Invalid amplifier \"%s\" for Mob Effect", s));
			return null;
		}
		int amplifier = Integer.parseInt(split[2]);

		return new MobEffectInstance(effect, duration, amplifier);
	}
}
