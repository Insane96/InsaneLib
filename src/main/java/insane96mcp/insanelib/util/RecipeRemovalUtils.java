package insane96mcp.insanelib.util;

import insane96mcp.insanelib.InsaneLib;
import insane96mcp.insanelib.setup.ILTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;

import java.util.List;

public class RecipeRemovalUtils {
	/**
	 * Returns a copy of the given {@link RecipeMap} without every recipe whose result item is in the
	 * {@link ILTags.Items#RECIPE_REMOVAL} tag, even if the recipe was added by another mod or data pack.
	 * Called from {@link insane96mcp.insanelib.mixin.RecipeManagerMixin} right before the recipe manager finalizes
	 * its recipes, which happens both on server start and on every {@code /reload}, after item tags have been bound.
	 * The result is read from the recipe's {@link RecipeDisplay}s, so recipes without displays are always kept.
	 *
	 * @return the filtered map, or the same instance if nothing was removed
	 */
	public static RecipeMap removeTaggedRecipes(RecipeMap recipes, HolderLookup.Provider registries) {
		if (BuiltInRegistries.ITEM.get(ILTags.Items.RECIPE_REMOVAL).isEmpty())
			return recipes;

		ContextMap context = new ContextMap.Builder()
				.withParameter(SlotDisplayContext.REGISTRIES, registries)
				.create(SlotDisplayContext.CONTEXT);
		List<RecipeHolder<?>> allRecipes = List.copyOf(recipes.values());
		List<RecipeHolder<?>> keptRecipes = allRecipes.stream()
				.filter(holder -> holder.value().display().stream()
						.noneMatch(display -> display.result().resolveForFirstStack(context).is(ILTags.Items.RECIPE_REMOVAL)))
				.toList();

		if (keptRecipes.size() == allRecipes.size())
			return recipes;

		InsaneLib.LOGGER.info("RecipeRemoval: removed {} recipe(s) producing tagged items", allRecipes.size() - keptRecipes.size());
		return RecipeMap.create(keptRecipes);
	}
}
