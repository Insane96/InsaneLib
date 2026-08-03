package insane96mcp.insanelib.data.poolinjection;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import insane96mcp.insanelib.InsaneLib;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import org.jetbrains.annotations.NotNull;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads {@code data/<namespace>/loot_pool_injections/*.json} files, each describing a single item to inject
 * into an existing, named pool of a target loot table (see {@link PoolInjection}). Example:
 * <pre>{@code
 * {
 *   "loot_table": "minecraft:chests/simple_dungeon",
 *   "pool": "main",
 *   "entry": {
 *     "type": "minecraft:item",
 *     "name": "minecraft:heart_of_the_sea",
 *     "weight": 5,
 *     "conditions": [{ "condition": "minecraft:random_chance", "chance": 0.05 }]
 *   }
 * }
 * }</pre>
 * On {@link LootTableLoadEvent}, the target pool is re-encoded to JSON, the entry is appended to its
 * {@code "entries"} array, and the pool is decoded again and swapped back in via
 * {@link LootTable#removePool(String)}/{@link LootTable#addPool(LootPool)}. This makes the injected entry
 * compete for selection using the same weighted roll as the pool's original entries.
 */
public class PoolInjectionReloadListener extends SimplePreparableReloadListener<List<PoolInjection>> {
    public static final PoolInjectionReloadListener INSTANCE = new PoolInjectionReloadListener();

    public static List<PoolInjection> INJECTIONS = new ArrayList<>();

    @Override
    protected @NotNull List<PoolInjection> prepare(@NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        List<PoolInjection> injections = new ArrayList<>();
        resourceManager.listResources("loot_pool_injections", path -> path.getPath().endsWith(".json"))
                .forEach((location, resource) -> {
                    try (InputStreamReader reader = new InputStreamReader(resource.open())) {
                        JsonObject json = GsonHelper.parse(reader);
                        ResourceLocation lootTable = ResourceLocation.parse(GsonHelper.getAsString(json, "loot_table"));
                        String pool = GsonHelper.getAsString(json, "pool");
                        JsonObject entry = GsonHelper.getAsJsonObject(json, "entry");
                        injections.add(new PoolInjection(lootTable, pool, entry));
                    } catch (Exception e) {
                        InsaneLib.LOGGER.error("Failed to load loot pool injection {}: {}", location, e.getMessage());
                    }
                });
        return injections;
    }

    @Override
    protected void apply(@NotNull List<PoolInjection> injections, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        INJECTIONS = injections;
        InsaneLib.LOGGER.info("Loaded {} loot pool injections", INJECTIONS.size());
    }

    public static void onLootTableLoad(LootTableLoadEvent event) {
        for (PoolInjection injection : INJECTIONS) {
            if (!injection.lootTable().equals(event.getName()))
                continue;

            LootTable table = event.getTable();
            LootPool pool = table.getPool(injection.pool());
            if (pool == null) {
                InsaneLib.LOGGER.warn("Loot pool injection targets pool '{}' in loot table {}, but no such pool exists", injection.pool(), injection.lootTable());
                continue;
            }

            RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, event.getRegistries());

            LootPoolEntryContainer entry = LootPoolEntries.CODEC.parse(ops, injection.entry()).getOrThrow();
            JsonObject poolJson = (JsonObject) LootPool.CODEC.encodeStart(ops, pool).getOrThrow();
            JsonObject entryJson = (JsonObject) LootPoolEntries.CODEC.encodeStart(ops, entry).getOrThrow();
            poolJson.getAsJsonArray("entries").add(entryJson);
            LootPool newPool = LootPool.CODEC.parse(ops, poolJson).getOrThrow();

            table.removePool(injection.pool());
            table.addPool(newPool);
        }
    }
}
