package insane96mcp.insanelib.mixin;

import insane96mcp.insanelib.event.ILEventFactory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import javax.annotation.Nullable;

@Mixin(ItemStack.class)
public class ItemStackMixin {
	@ModifyVariable(at = @At(value = "STORE", ordinal = 0), method = "hurt", ordinal = 0, argsOnly = true)
	public int onHurtAmount(int amount, int pAmount, RandomSource random, @Nullable ServerPlayer player) {
		return ILEventFactory.getHurtAmount((ItemStack) (Object) this, amount, random, player);
	}
}
