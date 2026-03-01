package insane96mcp.insanelib.event;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

/**
 * This event is triggered when the game calculates the movement speed multiplier for a player using an item.
 * The default movement speed modifier can be adjusted for specific items or player conditions by modifying the event's speed modifier.
 *
 * This event is fired on the NeoForge event bus and is client-side only.
 *
 * Use this event to dynamically modify movement speed when using an item based on custom conditions.
 */
public class PlayerUseItemMovSpeedEvent extends Event {
    public static final float VANILLA_SPEED_MODIFIER = 0.2f;
    private final LocalPlayer player;
    private float speedModifier = VANILLA_SPEED_MODIFIER;
    private final ItemStack useItem;

    public PlayerUseItemMovSpeedEvent(LocalPlayer player, ItemStack useItem) {
        super();
        this.player = player;
        this.useItem = useItem;
    }

    public LocalPlayer getPlayer() {
        return player;
    }

    public float getSpeedModifier() {
        return this.speedModifier;
    }

    public void setSpeedModifier(float speedModifier) {
        this.speedModifier = speedModifier;
    }

    public ItemStack getUseItem() {
        return this.useItem;
    }
}
