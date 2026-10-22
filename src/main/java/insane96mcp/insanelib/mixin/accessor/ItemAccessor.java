package insane96mcp.insanelib.mixin.accessor;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Item.class)
public interface ItemAccessor {
    /**
     * The item's own components, bypassing ItemMixin#onComponents that returns the ones patched by ItemComponentsFeature
     */
    @Accessor("components")
    DataComponentMap insanelib$getUnpatchedComponents();
}
