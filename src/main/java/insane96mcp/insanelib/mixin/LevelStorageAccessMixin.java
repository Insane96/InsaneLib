package insane96mcp.insanelib.mixin;

import insane96mcp.insanelib.core.feature.config.ServerConfigFileReader;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelStorageSource.LevelStorageAccess.class)
public abstract class LevelStorageAccessMixin {
    @Shadow
    public abstract LevelStorageSource.LevelDirectory getLevelDirectory();

    /**
     * Data packs are loaded before the server exists, so this is the only way to know the world and its config overrides
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void insanelib$onLevelOpened(CallbackInfo ci) {
        ServerConfigFileReader.onLevelOpened(this.getLevelDirectory().path());
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void insanelib$onLevelClosed(CallbackInfo ci) {
        ServerConfigFileReader.onLevelClosed(this.getLevelDirectory().path());
    }
}
