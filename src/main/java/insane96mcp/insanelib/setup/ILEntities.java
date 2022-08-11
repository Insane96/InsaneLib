package insane96mcp.insanelib.setup;


import insane96mcp.insanelib.InsaneLib;
import insane96mcp.insanelib.entity.AreaEffectCloud3DEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ILEntities {
	public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, InsaneLib.MOD_ID);

	public static final RegistryObject<EntityType<AreaEffectCloud3DEntity>> AREA_EFFECT_CLOUD_3D = ENTITIES.register("area_effect_cloud_3d", () -> EntityType.Builder.<AreaEffectCloud3DEntity>of(AreaEffectCloud3DEntity::new, MobCategory.MISC).fireImmune().sized(6.0F, 0.5F).clientTrackingRange(10).updateInterval(Integer.MAX_VALUE).build("area_effect_cloud_3d"));

}
