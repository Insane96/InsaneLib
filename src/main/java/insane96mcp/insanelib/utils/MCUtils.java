package insane96mcp.insanelib.utils;

import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;

import java.util.Map;

public class MCUtils {
	/**
	 * Returns the current speed of the player compared to his normal speed
	 */
	public static double getMovementSpeedRatio(PlayerEntity player) {
		double baseMS = 0.1d;
		if (player.isSprinting())
			baseMS += 0.03f;
		double playerMS = player.getAttributeValue(Attributes.MOVEMENT_SPEED);
		return playerMS / baseMS;
	}

	/**
	 * Different version of ItemStack#addAttributeModifiers that doesn't override the item's modifiers
	 * @param itemStack
	 * @param attribute
	 * @param modifier
	 * @param modifierSlot
	 */
	public static void addAttributeModifierToItemStack(ItemStack itemStack, Attribute attribute, AttributeModifier modifier, EquipmentSlotType modifierSlot) {
		if (itemStack.hasTag() && !itemStack.getTag().contains("AttributeModifiers", 9)) {
			for (Map.Entry<Attribute, AttributeModifier> entry : itemStack.getAttributeModifiers(modifierSlot).entries()) {
				itemStack.addAttributeModifier(entry.getKey(), entry.getValue(), modifierSlot);
			}
		}
		itemStack.addAttributeModifier(attribute, modifier, modifierSlot);
	}
}
