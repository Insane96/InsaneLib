package insane96mcp.insanelib.util;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public class ClientUtils {
	/**
	 * Blits the texture mirrored (U coordinates swapped).
	 * <p>
	 * Note: setRenderColor/resetRenderColor were removed in the 26.1 port — the global RenderSystem
	 * color/blend state no longer exists in the new GPU pipeline; color is now per-draw.
	 */
	public static void blitVerticallyMirrored(Identifier texture, GuiGraphicsExtractor guiGraphics, int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight) {
		guiGraphics.blit(texture, x, y, x + width, y + height,
				(u + (float)width) / (float)textureWidth, (u + 0.0F) / (float)textureWidth,
				(v + 0.0F) / (float)textureHeight, (v + (float)height) / (float)textureHeight);
	}
}
