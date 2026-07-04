package insane96mcp.insanelib.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import insane96mcp.insanelib.module.base.NbtTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ProjectileWeaponItem.class)
public class ProjectileWeaponItemMixin {
    @WrapOperation(method = "useAmmo", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;split(I)Lnet/minecraft/world/item/ItemStack;"))
	private static ItemStack insanelib$onUseAmmo(ItemStack instance, int amount, Operation<ItemStack> original, ItemStack weapon, ItemStack ammo, LivingEntity shooter, boolean intangable) {
        if (!(shooter instanceof Mob) || !NbtTags.isNoAmmoConsumption(shooter))
 	       return original.call(instance, amount);
		return instance.copyWithCount(1);
    }
}
