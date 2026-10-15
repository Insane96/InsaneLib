package insane96mcp.insanelib.util;

import insane96mcp.insanelib.mixin.accessor.GuiGraphicsAccessor;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class ClientUtils {
	public static void blitVerticallyMirrored(Identifier texture, GuiGraphicsExtractor guiGraphics, int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight) {
		blitVerticallyMirrored(texture, guiGraphics, x, y, u, v, width, height, textureWidth, textureHeight, -1);
	}

	/**
	 * Same as {@link #blitVerticallyMirrored(Identifier, GuiGraphicsExtractor, int, int, int, int, int, int, int, int)} but tinted with the given ARGB color
	 */
	public static void blitVerticallyMirrored(Identifier texture, GuiGraphicsExtractor guiGraphics, int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight, int color) {
		((GuiGraphicsAccessor) guiGraphics).invokeInnerBlit(RenderPipelines.GUI_TEXTURED, texture, x, x + width, y, y + height, (u + (float)width) / (float)textureWidth, (u + 0.0F) / (float)textureWidth, (v + 0.0F) / (float)textureHeight, (v + (float)height) / (float)textureHeight, color);
	}
}
