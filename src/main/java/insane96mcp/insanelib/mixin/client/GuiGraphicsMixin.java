package insane96mcp.insanelib.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import insane96mcp.insanelib.module.base.items.ItemComponentsFeature;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin {
    @Unique
    private static final float SHRUNK_SCALE = 0.7F;

    /**
     * Counts over 999 are shown as 1k, 1m, 1b (rounded down) so they fit in the slot, with one decimal when the
     * integer part has a single digit (e.g. 1.5k, 15k, 150k)
     */
    @ModifyExpressionValue(method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
            at = @At(value = "INVOKE", target = "Ljava/lang/String;valueOf(I)Ljava/lang/String;"))
    private String insanelib$abbreviateCount(String original, @Local(argsOnly = true) ItemStack stack) {
        int count = stack.getCount();
        if (count >= 1_000_000_000)
            return insanelib$abbreviate(count, 1_000_000_000, "b");
        if (count >= 1_000_000)
            return insanelib$abbreviate(count, 1_000_000, "m");
        if (count >= 1_000)
            return insanelib$abbreviate(count, 1_000, "k");
        return original;
    }

    @Unique
    private static String insanelib$abbreviate(int count, int unit, String suffix) {
        int tenths = count / (unit / 10);
        if (tenths < 100)
            return tenths / 10 + "." + tenths % 10 + suffix;
        return count / unit + suffix;
    }

    @WrapOperation(method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I"))
    private int insanelib$drawCount(GuiGraphics instance, Font font, String text, int x, int y, int color, boolean dropShadow, Operation<Integer> original, @Local(argsOnly = true) ItemStack stack) {
        // Based on the count and not on the text length, as abbreviated counts (e.g. 1k) are short
        if (ItemComponentsFeature.disableShrinkStackCount || text == null || (!ItemComponentsFeature.alwaysShrinkStackCount && stack.getCount() <= 99))
            return original.call(instance, font, text, x, y, color, dropShadow);

        // Anchor the scale on the bottom-right corner of the unscaled text (where vanilla right-aligns it)
        // so shrinking the text keeps it flush against the slot's corner instead of drifting off-position.
        float anchorX = x + font.width(text);
        float anchorY = y + 8;

        PoseStack pose = instance.pose();
        pose.pushPose();
        pose.translate(anchorX, anchorY, 0.0F);
        pose.scale(SHRUNK_SCALE, SHRUNK_SCALE, 1.0F);
        pose.translate(-anchorX, -anchorY, 0.0F);
        int result = original.call(instance, font, text, x, y, color, dropShadow);
        pose.popPose();
        return result;
    }
}
