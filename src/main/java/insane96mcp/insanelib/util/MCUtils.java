package insane96mcp.insanelib.util;

import insane96mcp.insanelib.InsaneLib;
import insane96mcp.insanelib.mixin.accessor.MobEffectInstanceAccessor;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.math.NumberUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MCUtils {
	/**
	 * Returns the current speed of the player compared to his normal speed
	 */
	public static double getMovementSpeedRatio(LivingEntity livingEntity) {
		double baseMS = 0.1d;
		if (livingEntity.isSprinting()) {
			baseMS += 0.029999999329447746;
		}

		double entityMS = livingEntity.getAttributeValue(Attributes.MOVEMENT_SPEED);
		return entityMS / baseMS;
	}

	/**
	 * Applies a modifier to the Living Entity. If the attribute is max_health also sets entity's health to his max health
	 * @return true if the modifier was applied
	 */
	public static boolean applyModifier(LivingEntity entity, Holder<Attribute> attribute, ResourceLocation id, double amount, AttributeModifier.Operation operation, boolean permanent) {
		return applyModifier(entity, attribute, new AttributeModifier(id, amount, operation), permanent);
	}

	/**
	 * Applies a permanent modifier to the Living Entity. If the attribute is max_health also heals the entity to the new bonus health (if any)
	 * @return true if the modifier was applied
	 */
	public static boolean applyModifier(LivingEntity entity, Holder<Attribute> attribute, ResourceLocation id, double amount, AttributeModifier.Operation operation) {
		return applyModifier(entity, attribute, new AttributeModifier(id, amount, operation), true);
	}

	/**
	 * Applies a modifier to the Living Entity. If the attribute is max_health also heals the entity to the new bonus health (if any)
	 * @return true if the modifier was applied
	 */
	public static boolean applyModifier(LivingEntity entity, Holder<Attribute> attribute, AttributeModifier modifier, boolean permanent) {
		AttributeInstance attributeInstance = entity.getAttribute(attribute);
		if (attributeInstance != null) {
			if (attributeInstance.hasModifier(modifier.id()))
				return false;
			float oldMaxHealth = entity.getMaxHealth();
			if (permanent)
				attributeInstance.addPermanentModifier(modifier);
			else
				attributeInstance.addTransientModifier(modifier);

			if (attribute == Attributes.MAX_HEALTH) {
				float newMaxHealth = entity.getMaxHealth();
				if (newMaxHealth > oldMaxHealth)
					entity.heal(newMaxHealth - oldMaxHealth);
			}
			return true;
		}
		return false;
	}

	/**
	 * Removes a modifier from the Living Entity if the entity has the attribute
	 */
    public static void removeModifier(LivingEntity entity, Holder<Attribute> attribute, ResourceLocation id) {
        AttributeInstance attributeInstance = entity.getAttribute(attribute);
        if (attributeInstance != null)
            attributeInstance.removeModifier(id);
    }

	/**
	 * Sets the value of an attribute
	 * @return true if the override was successful
	 */
	public static boolean setAttributeBaseValue(LivingEntity entity, Holder<Attribute> attribute, double value) {
		AttributeInstance attributeInstance = entity.getAttribute(attribute);
		if (attributeInstance != null) {
			attributeInstance.setBaseValue(value);

			if (attribute == Attributes.MAX_HEALTH)
				entity.setHealth(entity.getMaxHealth());

			return true;
		}
		return false;
	}

	/**
	 * Adds the attribute modifier to the stack, but also adds the default attribute modifiers from the item
	 */
	public static void addAttributeModifierToItemStack(ItemStack itemStack, Holder<Attribute> attribute, AttributeModifier modifier, EquipmentSlotGroup equipmentSlotGroup) {
		ItemAttributeModifiers modifiers = itemStack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
		if (modifiers.modifiers().isEmpty())
			modifiers = itemStack.getItem().getDefaultAttributeModifiers(itemStack);
		modifiers.withModifierAdded(attribute, modifier, equipmentSlotGroup);
		itemStack.set(DataComponents.ATTRIBUTE_MODIFIERS, modifiers);
	}

	public static boolean hurtIgnoreInvulnerability(LivingEntity hurtEntity, DamageSource source, float amount) {
		int hurtResistantTime = hurtEntity.invulnerableTime;
		hurtEntity.invulnerableTime = 0;
		boolean attacked = hurtEntity.hurt(source, amount);
		hurtEntity.invulnerableTime = hurtResistantTime;
		return attacked;
	}

	/**
	 * Checks if nbt1 tags are all present in and match nbt2
	 */
	public static boolean compareNBT(CompoundTag nbt1, CompoundTag nbt2) {
		for (String key : nbt1.getAllKeys()) {
			if (!nbt2.contains(key))
				return false;

			if (nbt1.get(key) instanceof CompoundTag && nbt2.get(key) instanceof CompoundTag) {
				if (!compareNBT(nbt1.getCompound(key), nbt2.getCompound(key)))
					return false;
			}
			//Can't be null. Looping over all the keys
			else if (!nbt1.get(key).equals(nbt2.get(key)))
				return false;
		}
		return true;
	}

	/**
	 * Returns true if the player has completed the advancement
	 */
	public static boolean isAdvancementDone(ServerPlayer player, ResourceLocation advancementRL) {
		AdvancementHolder advancement = player.server.getAdvancements().get(advancementRL);
		if (advancement == null)
			return false;

		return player.getAdvancements().getOrStartProgress(advancement).isDone();
	}

	public static ItemStack createPotionStackFromEffectInstances(Item item, List<MobEffectInstance> mobEffectInstances) {
		return createPotionStackFromEffectInstances(item, null, mobEffectInstances);
	}

	public static ItemStack createPotionStackFromEffectInstances(Item item, @Nullable Integer color, List<MobEffectInstance> mobEffectInstances) {
		ItemStack itemstack = new ItemStack(item);
		itemstack.set(DataComponents.POTION_CONTENTS, new PotionContents(Optional.empty(), Optional.ofNullable(color), mobEffectInstances));
		return itemstack;
	}

	/**
	 * Returns true if the entity has a HARMFUL effect
	 */
	public static boolean hasNegativeEffect(LivingEntity entity) {
		for (MobEffectInstance mobEffectInstance : entity.getActiveEffects()) {
			if (entity.hasEffect(mobEffectInstance.getEffect()) && mobEffectInstance.getEffect().value().getCategory().equals(MobEffectCategory.HARMFUL))
				return true;
		}
		return false;
	}

	/**
	 * Same as hasNegativeEffect but also checks if the duration of the effect is higher than 7.5 seconds
	 */
	public static boolean hasLongNegativeEffect(LivingEntity entity) {
		for (MobEffectInstance mobEffectInstance : entity.getActiveEffects()) {
			if (entity.hasEffect(mobEffectInstance.getEffect()) && mobEffectInstance.getEffect().value().getCategory().equals(MobEffectCategory.HARMFUL) && mobEffectInstance.getDuration() > 150)
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
				if (level.getBlockState(p.above(i)).blocksMotion()) {
					viable = false;
					break;
				}
			}
			if (!viable)
				continue;
			fittingYPos = y;
			if (!level.getBlockState(p.below()).blocksMotion())
				continue;
			return y;
		}
		return fittingYPos;
	}

	/**
	 * Creates a MobEffectInstance with the possibility to prevent it from being cured
	 */
	public static MobEffectInstance createEffectInstance(Holder<MobEffect> potion, int duration, int amplifier, boolean ambient, boolean showParticles, boolean showIcon, boolean canBeCured) {
		MobEffectInstance effectInstance = new MobEffectInstance(potion, duration, amplifier, ambient, showParticles, showIcon);
		if (!canBeCured)
			((MobEffectInstanceAccessor) effectInstance).getCuresField().clear();
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
			InsaneLib.LOGGER.warn("Invalid Mob Effect \"{}\"", s);
			return null;
		}

		ResourceLocation effectRL = ResourceLocation.tryParse(split[0]);
		if (effectRL == null) {
			InsaneLib.LOGGER.warn("{} mob effect is not valid", split[0]);
			return null;
		}
		if (!BuiltInRegistries.MOB_EFFECT.containsKey(effectRL)) {
			InsaneLib.LOGGER.warn("{} mob effect seems to not exist", split[0]);
			return null;
		}
		MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(effectRL);

		//Duration
		if (!NumberUtils.isParsable(split[1])) {
			InsaneLib.LOGGER.warn("Invalid duration \"{}\" for Mob Effect", s);
			return null;
		}
		int duration = Integer.parseInt(split[1]);

		//Amplifier
		if (!NumberUtils.isParsable(split[2])) {
			InsaneLib.LOGGER.warn("Invalid amplifier \"{}\" for Mob Effect", s);
			return null;
		}
		int amplifier = Integer.parseInt(split[2]);

		//noinspection ConstantConditions
		return new MobEffectInstance(Holder.direct(effect), duration, amplifier);
	}

	/**
	 * Returns the Tag in the player persistent data that is kept on death / dimension change
	 */
	public static CompoundTag getOrCreatePersistedData(Player player) {
		CompoundTag tag;
		if (!player.getPersistentData().contains(Player.PERSISTED_NBT_TAG)) {
			tag = new CompoundTag();
			player.getPersistentData().put(Player.PERSISTED_NBT_TAG, tag);
		}
		else {
			tag = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
		}
		return tag;
	}

	public static float getFoodEffectiveness(FoodProperties foodProperties) {
		return foodProperties.nutrition() + foodProperties.saturation();
	}

	/**
	 * Returns a "synced" random. It's not really synced, it uses level game time, which is usually synced
	 */
	public static RandomSource syncedRandom(Player player) {
		RandomSource random = player.getRandom();
		if (player.level().isClientSide)
			random.setSeed(player.level().getGameTime() + 1);
		else
			random.setSeed(player.level().getGameTime());
		random.setSeed(random.nextLong());
		random.setSeed(random.nextLong());
		return random;
	}
}
