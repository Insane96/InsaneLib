package insane96mcp.insanelib.util;

import net.minecraft.advancements.Advancement;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.UUID;

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
}
