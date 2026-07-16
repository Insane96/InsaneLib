package insane96mcp.insanelib.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import insane96mcp.insanelib.module.base.items.ItemComponentsFeature;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin {
    @Unique
    private static final float SHRUNK_SCALE = 0.7F;

    @Redirect(method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I"))
    private int insanelib$drawCount(GuiGraphics instance, Font font, String text, int x, int y, int color, boolean dropShadow) {
        if (text == null || (!ItemComponentsFeature.alwaysShrinkStackCount && text.length() <= 2))
            return instance.drawString(font, text, x, y, color, dropShadow);

        // Anchor the scale on the bottom-right corner of the unscaled text (where vanilla right-aligns it)
        // so shrinking the text keeps it flush against the slot's corner instead of drifting off-position.
        float anchorX = x + font.width(text);
        float anchorY = y + 8;

        PoseStack pose = instance.pose();
        pose.pushPose();
        pose.translate(anchorX, anchorY, 0.0F);
        pose.scale(SHRUNK_SCALE, SHRUNK_SCALE, 1.0F);
        pose.translate(-anchorX, -anchorY, 0.0F);
        int result = instance.drawString(font, text, x, y, color, dropShadow);
        pose.popPose();
        return result;
    }
}
