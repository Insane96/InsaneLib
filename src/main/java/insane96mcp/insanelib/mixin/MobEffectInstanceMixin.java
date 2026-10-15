package insane96mcp.insanelib.mixin;

import insane96mcp.insanelib.world.effect.ICurableEffectInstance;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEffectInstance.class)
public class MobEffectInstanceMixin implements ICurableEffectInstance {
    @Unique
    private boolean insanelib$curable = true;

    @Override
    public boolean insanelib$isCurable() {
        return this.insanelib$curable;
    }

    @Override
    public void insanelib$setCurable(boolean curable) {
        this.insanelib$curable = curable;
    }

    @Inject(method = "setDetailsFrom", at = @At("TAIL"))
    private void insanelib$copyCurable(MobEffectInstance copy, CallbackInfo ci) {
        this.insanelib$curable = ((ICurableEffectInstance) copy).insanelib$isCurable();
    }
}
