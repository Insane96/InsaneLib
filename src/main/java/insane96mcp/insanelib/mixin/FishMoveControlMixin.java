package insane96mcp.insanelib.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanelib.module.base.Fixes;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.neoforged.neoforge.common.NeoForgeMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.world.entity.animal.fish.AbstractFish$FishMoveControl")
public abstract class FishMoveControlMixin extends MoveControl {
	public FishMoveControlMixin(Mob pMob) {
		super(pMob);
	}

	@ModifyExpressionValue(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/ai/attributes/Attributes;MOVEMENT_SPEED:Lnet/minecraft/core/Holder;"))
    public Holder<Attribute> insanelib$changeSwimSpeedAttribute(Holder<Attribute> original) {
		if (!Fixes.shouldFixSwimmersSwimmingAttribute())
			return original;
		return NeoForgeMod.SWIM_SPEED;
    }
}
