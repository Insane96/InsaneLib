package insane96mcp.insanelib.event;

import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

/**
 * Fired when {@code IItemExtension#getMaxDamage(ItemStack)} is called, allowing modification of the returned value.
 */
public class GetMaxDamageEvent extends Event {
    private final ItemStack stack;
    private final int originalMaxDamage;
    private int maxDamage;

    public GetMaxDamageEvent(ItemStack stack, int originalMaxDamage) {
        this.stack = stack;
        this.originalMaxDamage = originalMaxDamage;
        this.maxDamage = originalMaxDamage;
    }

    public ItemStack getStack() {
        return stack;
    }

    public int getOriginalMaxDamage() {
        return originalMaxDamage;
    }

    public int getMaxDamage() {
        return maxDamage;
    }

    public void setMaxDamage(int maxDamage) {
        this.maxDamage = maxDamage;
    }
}