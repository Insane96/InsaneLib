package insane96mcp.insanelib.setup;

import insane96mcp.insanelib.entity.AreaEffectCloud3DRenderer;
import net.minecraftforge.client.event.EntityRenderersEvent;

public class ClientSetup {
	public static void init(final EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(ILEntities.AREA_EFFECT_CLOUD_3D.get(), AreaEffectCloud3DRenderer::new);
	}
}
