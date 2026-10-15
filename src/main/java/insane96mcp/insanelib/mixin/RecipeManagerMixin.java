package insane96mcp.insanelib.mixin;

import insane96mcp.insanelib.util.RecipeRemovalUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {
	@Shadow @Final private HolderLookup.Provider registries;
	@Shadow private RecipeMap recipes;

	@Inject(method = "finalizeRecipeLoading", at = @At("HEAD"))
	private void insanelib$removeTaggedRecipes(FeatureFlagSet enabledFlags, CallbackInfo ci) {
		this.recipes = RecipeRemovalUtils.removeTaggedRecipes(this.recipes, this.registries);
	}
}
