package insane96mcp.insanelib.mixin;

import net.neoforged.neoforge.common.loot.LootModifierManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.HashMap;
import java.util.LinkedHashMap;

@Mixin(value = LootModifierManager.class, remap = false)
public class LootModifierManagerMixin {
    // NeoForge builds finalLocations in order but dumps it into a HashMap, losing that order.
    @Redirect(method = "prepare(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)Ljava/util/Map;", at = @At(value = "NEW", target = "java/util/HashMap", remap = false))
    private HashMap<?, ?> useLinkedHashMap() {
        return new LinkedHashMap<>();
    }
}