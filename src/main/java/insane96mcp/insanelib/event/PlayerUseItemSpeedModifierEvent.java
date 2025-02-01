package insane96mcp.insanelib.event;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/**
 * Fired when the game checks if the player can sprint.
 * Cancel to prevent player sprinting.
 * This event is fired Client Side only
 */
@Cancelable
public class PlayerUseItemSpeedModifierEvent extends Event {
    public static final float VANILLA_SPEED_MODIFIER = 0.2f;
    private final LocalPlayer player;
    private float speedModifier = VANILLA_SPEED_MODIFIER;
    private final ItemStack useItem;

    public PlayerUseItemSpeedModifierEvent(LocalPlayer player, ItemStack useItem) {
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
