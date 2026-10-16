package insane96mcp.insanelib.mixin;

import insane96mcp.insanelib.data.poolinjection.PoolInjectionLoader;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(ReloadableServerRegistries.class)
public class ReloadableServerRegistriesMixin {
    /**
     * Loot tables are loaded before the reload listeners, so loot pool injections must be read before them
     */
    @Inject(method = "reload", at = @At("HEAD"))
    private static void insanelib$loadPoolInjections(LayeredRegistryAccess<RegistryLayer> registries, ResourceManager resourceManager, Executor backgroundExecutor, CallbackInfoReturnable<CompletableFuture<LayeredRegistryAccess<RegistryLayer>>> cir) {
        PoolInjectionLoader.load(resourceManager);
    }
}
