package insane96mcp.insanelib.event;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.Nullable;

/**
 * Fired on ItemStack#hurt after unbreaking has been applied to the amount.
 */
public class HurtItemStackEvent extends Event {
    final ItemStack stack;
    final int originalAmount;
    int amount;
    RandomSource randomSource;
    @Nullable
    final LivingEntity livingEntity;

    public HurtItemStackEvent(ItemStack stack, int amount, RandomSource random, @Nullable LivingEntity livingEntity) {
        super();
        this.livingEntity = livingEntity;
        this.stack = stack;
        this.originalAmount = amount;
        this.amount = amount;
        this.randomSource = random;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getOriginalAmount() {
        return this.originalAmount;
    }

    public RandomSource getRandom() {
        return this.randomSource;
    }

    public ItemStack getStack() {
        return this.stack;
    }

    @Nullable
    public LivingEntity getLivingEntity() {
        return this.livingEntity;
    }
}
