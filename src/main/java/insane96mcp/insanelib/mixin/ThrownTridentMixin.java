package insane96mcp.insanelib.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ThrownTrident.class)
public abstract class ThrownTridentMixin extends AbstractArrow {
	protected ThrownTridentMixin(EntityType<? extends AbstractArrow> entityType, Level level) {
		super(entityType, level);
	}

	/**
	 * Replaces the vanilla fixed 8 base damage with the thrown trident item's own attack damage attribute modifiers
	 * (including the ones added via ItemAttributeModifierEvent), so changes to the trident's damage are reflected when it's thrown, not just in melee.
	 * The vanilla trident has a single +8 attack damage modifier, so the damage stays the same without changes.
	 */
	@ModifyExpressionValue(method = "onHitEntity", at = @At(value = "CONSTANT", args = "floatValue=8.0"))
	private float insanelib$itemBasedThrownDamage(float original) {
		double addValue = 0d;
		double multipliedBase = 0d;
		double multipliedTotal = 1d;
		for (ItemAttributeModifiers.Entry entry : this.getWeaponItem().getAttributeModifiers().modifiers()) {
			if (!entry.attribute().is(Attributes.ATTACK_DAMAGE) || !entry.slot().test(EquipmentSlot.MAINHAND))
				continue;
			AttributeModifier modifier = entry.modifier();
			switch (modifier.operation()) {
				case ADD_VALUE -> addValue += modifier.amount();
				case ADD_MULTIPLIED_BASE -> multipliedBase += modifier.amount();
				case ADD_MULTIPLIED_TOTAL -> multipliedTotal *= 1d + modifier.amount();
			}
		}
		return (float) ((addValue + addValue * multipliedBase) * multipliedTotal);
	}
}
