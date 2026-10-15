package insane96mcp.insanelib.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import insane96mcp.insanelib.module.base.items.ItemComponentsFeature;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GuiGraphicsExtractor.class)
public abstract class GuiGraphicsMixin {
    @Unique
    private static final float SHRUNK_SCALE = 0.7F;

    @WrapOperation(method = "itemCount",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)V"))
    private void insanelib$drawCount(GuiGraphicsExtractor instance, Font font, String text, int x, int y, int color, boolean dropShadow, Operation<Void> original) {
        if (ItemComponentsFeature.disableShrinkStackCount || text == null || (!ItemComponentsFeature.alwaysShrinkStackCount && text.length() <= 2)) {
            original.call(instance, font, text, x, y, color, dropShadow);
            return;
        }

        // Anchor the scale on the bottom-right corner of the unscaled text (where vanilla right-aligns it)
        // so shrinking the text keeps it flush against the slot's corner instead of drifting off-position.
        float anchorX = x + font.width(text);
        float anchorY = y + 8;

        Matrix3x2fStack pose = instance.pose();
        pose.pushMatrix();
        pose.translate(anchorX, anchorY);
        pose.scale(SHRUNK_SCALE, SHRUNK_SCALE);
        pose.translate(-anchorX, -anchorY);
        original.call(instance, font, text, x, y, color, dropShadow);
        pose.popMatrix();
    }
}
