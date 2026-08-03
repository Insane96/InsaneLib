package insane96mcp.insanelib.data.poolinjection;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

/**
 * A request to inject a loot pool entry into an existing, named pool of a target loot table.
 * <p>
 * NeoForge auto-assigns a name to every loaded {@link net.minecraft.world.level.storage.loot.LootPool}:
 * {@code "main"} if the loot table has a single pool, otherwise {@code "pool0"}, {@code "pool1"}, etc.
 * <p>
 * The {@code entry} is decoded lazily, against the {@link net.minecraft.core.HolderLookup.Provider}
 * available from {@link net.neoforged.neoforge.event.LootTableLoadEvent#getRegistries()}, since registry
 * access isn't available yet when the injection JSON files are first read.
 */
public record PoolInjection(ResourceLocation lootTable, String pool, JsonObject entry) {
}
