package insane96mcp.insanelib.world.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Basically a MobEffect with the possibility to set the mob effect as non-curable
 */
public class ILMobEffect extends MobEffect {
    boolean canBeCured;

    public ILMobEffect(MobEffectCategory typeIn, int liquidColorIn) {
        this(typeIn, liquidColorIn, true);
    }

    public ILMobEffect(MobEffectCategory typeIn, int liquidColorIn, boolean canBeCured) {
        super(typeIn, liquidColorIn);
        this.canBeCured = canBeCured;
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return !canBeCured ? new ArrayList<>() : super.getCurativeItems();
    }
}
