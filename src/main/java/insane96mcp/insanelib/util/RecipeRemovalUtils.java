package insane96mcp.insanelib.util;

import insane96mcp.insanelib.InsaneLib;
import insane96mcp.insanelib.setup.ILTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.List;

public class RecipeRemovalUtils {
	/**
	 * Handles the very first resource load of a server (dedicated or integrated). At this point
	 * {@link TagsUpdatedEvent} has already fired once from {@code WorldLoader.load()}, but before this event that
	 * fired it, {@link ServerLifecycleHooks#getCurrentServer()} is still null since the {@link MinecraftServer}
	 * hasn't been constructed yet, so {@link #removeRecipeRemovalTaggedRecipes(TagsUpdatedEvent)} silently skips it.
	 * {@link ServerAboutToStartEvent} instead hands us the server directly, already with recipes and tags loaded.
	 */
	public static void onServerAboutToStart(ServerAboutToStartEvent event) {
		MinecraftServer server = event.getServer();
		removeTaggedRecipes(server.getRecipeManager(), server.registryAccess());
	}

	/**
	 * Handles every subsequent reload (e.g. {@code /reload}) once the server is already running.
	 * Runs on {@link TagsUpdatedEvent} once the resource reload (recipes included) has fully completed, only for the
	 * cause that means the server's own data actually changed (not the client just receiving a tag packet).
	 */
	public static void removeRecipeRemovalTaggedRecipes(TagsUpdatedEvent event) {
		if (event.getUpdateCause() != TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD)
			return;

		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if (server == null)
			return;

		removeTaggedRecipes(server.getRecipeManager(), event.getRegistryAccess());
	}

	/**
	 * Removes every recipe whose result item is in the {@link ILTags.Items#RECIPE_REMOVAL} tag from the given
	 * {@link RecipeManager}, even if the recipe was added by another mod or data pack.
	 */
	private static void removeTaggedRecipes(RecipeManager recipeManager, HolderLookup.Provider registries) {
		if (BuiltInRegistries.ITEM.getTag(ILTags.Items.RECIPE_REMOVAL).isEmpty())
			return;

		List<RecipeHolder<?>> allRecipes = List.copyOf(recipeManager.getRecipes());
		List<RecipeHolder<?>> keptRecipes = allRecipes.stream()
				.filter(holder -> !holder.value().getResultItem(registries).is(ILTags.Items.RECIPE_REMOVAL))
				.toList();

		if (keptRecipes.size() == allRecipes.size())
			return;

		recipeManager.replaceRecipes(keptRecipes);
		InsaneLib.LOGGER.info("RecipeRemoval: removed {} recipe(s) producing tagged items", allRecipes.size() - keptRecipes.size());
	}
}
