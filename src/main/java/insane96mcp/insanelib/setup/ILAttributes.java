package insane96mcp.insanelib.setup;

import insane96mcp.insanelib.InsaneLib;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ILAttributes {
    public static final DeferredRegister<Attribute> REGISTRY = DeferredRegister.create(BuiltInRegistries.ATTRIBUTE, InsaneLib.MOD_ID);
    public static final DeferredHolder<Attribute, Attribute> PUSH_RESISTANCE = REGISTRY.register("push_resistance", () -> new RangedAttribute("attribute.name.push_resistance", 0d, 0d, 1d));
}
