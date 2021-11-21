package insane96mcp.insanelib.setup;


import insane96mcp.insanelib.InsaneLib;
import insane96mcp.insanelib.entity.AreaEffectCloud3DEntity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModEntities {
	public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, InsaneLib.MOD_ID);

	public static final RegistryObject<EntityType<AreaEffectCloud3DEntity>> AREA_EFFECT_CLOUD_3D = ENTITIES.register("area_effect_cloud_3d", () -> EntityType.Builder.<AreaEffectCloud3DEntity>of(AreaEffectCloud3DEntity::new, EntityClassification.MISC).fireImmune().sized(6.0F, 0.5F).clientTrackingRange(10).updateInterval(Integer.MAX_VALUE).build("area_effect_cloud_3d"));

}
