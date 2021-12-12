package insane96mcp.insanelib.entity;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

public class AreaEffectCloud3DRenderer extends EntityRenderer<AreaEffectCloud3DEntity> {
	public AreaEffectCloud3DRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public ResourceLocation getTextureLocation(AreaEffectCloud3DEntity entity) {
		return TextureAtlas.LOCATION_BLOCKS;
	}
}
