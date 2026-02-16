# InsaneLib 1.20.1 — Full Feature Inventory for Porting to 1.21.1

This document lists every component in the 1.20.1 branch that needs to be ported to 1.21.1.

---

## 0. Porting Notes & Improvements

### Key Decision: Replace `IdTagMatcher` with `ObjTag`
The `IdTagMatcher` system must be replaced by the new generic, type-safe `ObjTag<T>` system (already prototyped in the `ObjTag` branch). This affects:
- **`IdTagMatcher.java`** — replaced entirely by `ObjTag<T>`
- **`IdTagValue.java`** — needs rework to use `ObjTag<T>` instead of `IdTagMatcher`
- **`IdTagRange.java`** — needs rework to use `ObjTag<T>` instead of `IdTagMatcher`
- **`TwinIdTagMatcher.java`** — needs rework to use `ObjTag<T>` instead of `IdTagMatcher`
- **`Blacklist.java`** — internally uses `IdTagMatcher`, must be migrated to `ObjTag<T>`
- **`Feature.java`** — has `IdTagMatcher.Config` option type in `loadConfigOptions()`, must be updated
- **`JsonFeature.java`** — tag resolution helpers (`isItemInTag`, `isBlockInTag`, etc.) can be simplified since `ObjTag.matches()` handles this natively

### Feature/Module Framework Improvements

#### Remove deprecated code
- **`Label.java`** — deprecated annotation, remove entirely. All references in `Feature.java` that fall back to `@Label` should be deleted.
- **`ILStrings.java`** — deprecated since 1.20.2, remove entirely.
- **`Feature(Module, boolean, boolean)` constructor** — marked `@Deprecated(forRemoval = true)`. Remove and keep only the no-arg constructor + `init()` pattern.
- **`JsonFeature(Module, boolean, boolean)` constructor** — same as above, remove.

#### `Feature.java` improvements
- **`loadConfigOptions()` reflection**: The large if/else chain for field types (Double, Integer, List, Enum, MinMax, Difficulty, Blacklist, IdTagMatcher) is fragile and hard to extend. Consider using a registry of type handlers or a `Map<Class<?>, ConfigOptionFactory>` to make it extensible without modifying the method.
- **`LogHelper` → `ILLogger`**: Already done in ObjTag branch. Apply everywhere.
- **`fieldNameToConfigOption()`**: Works fine, no changes needed.

#### `Module.java` improvements
- **`FMLJavaModLoadingContext.get()`** — deprecated/removed in NeoForge 1.21.1. The `Module` constructor needs the mod event bus passed in explicitly (there's already a TODO comment: `//TODO 1.21.1, pass context`).
- **`loadFeatures()` static method**: Uses `ModFileScanData` which still exists in NeoForge but the API may differ slightly. Verify compatibility.
- **Feature instantiation**: `instantiateFeature()` first tries the 3-arg constructor, then falls back to no-arg + `init()`. Since the 3-arg constructor is being removed, simplify to only use no-arg + `init()`.

#### `JsonFeature.java` improvements
- **`JSON_CONFIGS` visibility**: Change from `public` to `private` and add `addJsonConfig()` method (already done in ObjTag branch).
- **`JsonConfig<T>` constructor overloads**: Replace 4 constructors with single constructor + builder methods (already done in ObjTag branch: `syncToClient()`, `onLoad()`, `withRegistryFor()`).
- **Tag resolution helpers**: The 8 static methods (`isItemInTag`, `isBlockInTag`, `isEntityInTag`, `getAllItems`, `getAllBlocks` with client/server variants) are verbose and repetitive. With `ObjTag<T>`, most of these become unnecessary since `ObjTag.matches()` handles tag resolution generically. Consider removing or significantly simplifying.
- **`getGson()` centralization**: Already done in ObjTag branch — Gson creation with optional `ObjTag.AdapterFactory` registration.

#### `Blacklist.java` rework
- Currently stores `List<IdTagMatcher>` and has 7 overloaded `isBlackOrWhiteListed()` methods (for Block, Item, Entity, EntityType, Fluid, Biome, Enchantment).
- With `ObjTag<T>`, this can be simplified to a single generic `isBlackOrWhiteListed(T obj)` method, since `ObjTag<T>.matches()` handles all registry types.

#### `ConfigOption.java` — consider simplification
- The abstract `ConfigOption<T>` with many inner classes (`DoubleOption`, `IntOption`, `StringListOption`, `EnumOption`, `GenericOption`) works but is tightly coupled to Forge's `ModConfigSpec`. Review if NeoForge's config API has changed enough to warrant a refactor.

---

## 0.1 Porting Progress

### DONE — Core / Entry Point

#### `InsaneLib.java` — PORTED
- Removed all NeoForge template/example code (example blocks, items, creative tabs)
- Renamed `MODID` → `MOD_ID`
- Removed `commonSetup`, `addCreative`, `onServerStarting` template methods
- Removed `NeoForge.EVENT_BUS.register(this)` (no longer needed, no `@SubscribeEvent` methods)
- Config registration now uses `ILConfig.COMMON_SPEC` with custom file name (`insanelib.toml`)
- Added `location(String path)` helper for `ResourceLocation` creation
- Added `lang(String path)` helper for language key construction
- Constructor simplified to just config registration

#### `ILConfig.java` — NEW (replaces `setup/Config.java`)
- Uses `ModConfigSpec.Builder` (NeoForge) instead of `ForgeConfigSpec.Builder` (Forge)
- Static init block creates `COMMON_SPEC` and `CommonConfig`
- `CommonConfig` calls `Module.loadFeatures()` during construction
- `Modules.init()` call is currently commented out (no modules defined yet)
- Removed `@Mod.EventBusSubscriber` annotation (class has no event methods)

#### `InsaneLibClient.java` — UPDATED
- References updated from `MODID` to `MOD_ID`

### DONE — Feature/Module Framework

#### `Feature.java` — PORTED (moved to `insane96mcp.insanelib.core`)
- Package changed: `insane96mcp.insanelib.base` → `insane96mcp.insanelib.core`
- All Forge imports replaced with NeoForge equivalents (`ModConfigSpec`, `NeoForge.EVENT_BUS`, etc.)
- Removed all `@Label` annotation fallback code (deprecated annotation removed)
- `LogHelper` calls replaced with direct `InsaneLib.LOGGER` calls
- `Blacklist` config option commented out (pending ObjTag migration)
- `IdTagMatcher` config option commented out (pending ObjTag migration)
- Added `getName()` accessor to `ConfigOption` (was accessing `name` field directly before)
- Event registration uses `NeoForge.EVENT_BUS.register(this)` instead of `MinecraftForge.EVENT_BUS`

#### `Module.java` — PORTED (moved to `insane96mcp.insanelib.core`)
- Package changed: `insane96mcp.insanelib.base` → `insane96mcp.insanelib.core`
- All Forge imports replaced with NeoForge equivalents
- **Constructor now takes `IEventBus modEventBus`** parameter instead of using `FMLJavaModLoadingContext.get()` (resolves the 1.20.1 TODO)
- Builder pattern updated: all `create()` methods now require `IEventBus` parameter
- `LogHelper` calls replaced with `InsaneLib.LOGGER` direct calls (using SLF4J `{}` placeholders)
- Uses `net.neoforged.neoforgespi.language.ModFileScanData` instead of Forge's version

#### `LoadFeature.java` — PORTED (moved to `insane96mcp.insanelib.core`)
- Package changed: `insane96mcp.insanelib.base` → `insane96mcp.insanelib.core`
- No other changes

### DONE — Config System

#### `ConfigOption.java` — PORTED (moved to `insane96mcp.insanelib.core.config`)
- Package changed: `insane96mcp.insanelib.base` → `insane96mcp.insanelib.core.config`
- All `ForgeConfigSpec` references replaced with `ModConfigSpec`
- Added `getName()` public accessor method
- `java.lang.Double` simplified to just `Double` in `DoubleOption`

#### `ConfigUtils.java` — PORTED (moved to `insane96mcp.insanelib.core.config`)
- Package changed: `insane96mcp.insanelib.util` → `insane96mcp.insanelib.core.config`
- No other changes

#### `Difficulty.java` — PORTED (moved to `insane96mcp.insanelib.core.config`)
- All `ForgeConfigSpec` references replaced with `ModConfigSpec`
- Fixed imports (removed old `insane96mcp.insanelib.base` and `insane96mcp.insanelib.util` imports)

#### `MinMax.java` — PORTED (moved to `insane96mcp.insanelib.core.config`)
- All `ForgeConfigSpec` references replaced with `ModConfigSpec`
- Fixed imports (removed old `insane96mcp.insanelib.base` and `insane96mcp.insanelib.util` imports)

#### `Config.java` annotation — PORTED (moved to `insane96mcp.insanelib.core.config`)
- Already in `core.config`, no changes needed beyond the package move

### REMOVED (not porting)
- **`Label.java`** — deprecated annotation, not needed in 1.21.1

### NOT YET PORTED
- `Blacklist.java` — blocked on ObjTag migration
- `JsonFeature.java` — blocked on ObjTag migration
- All other components (events, mixins, network, data, modules, utilities, etc.)

---

## 1. Entry Point

### `InsaneLib.java`
The main mod class (`@Mod`). Responsibilities:
- Registers the Forge config (`COMMON_SPEC`)
- Registers mod event listeners (preInit, clientSetup, addPackFinders, registerStuff)
- Initializes `NetworkHandler` during `FMLCommonSetupEvent`
- Registers `JsonFeatureDataReloadListener` on `AddReloadListenerEvent`
- Registers `ILCommand` on `RegisterCommandsEvent`
- Registers `FeatureEnabledCondition` as a crafting recipe condition
- Registers `FeatureEnabledLootCondition` as a loot condition type
- Provides `handleMissingMappings()` utility for registry migration
- Provides `location()` helper for `ResourceLocation` creation
- Creates `ONE_DECIMAL_FORMATTER` on client setup

---

## 2. Feature/Module Framework

### `Module.java`
The module system — groups features under named modules:
- Static `HashMap<ResourceLocation, Module>` of all registered modules
- Builder pattern for creating modules with config spec
- Scans mod classes for `@LoadFeature` annotations via ASM data
- Instantiates and registers Feature classes automatically
- Supports `canBeDisabled`, `modConfigType` (COMMON/CLIENT/SERVER)
- Handles config reload events

### `Feature.java`
Base class for all features:
- Manages enabled/disabled state via Forge config
- Auto-generates config options from `@Config`-annotated static fields
- Supports field types: `Boolean`, `Integer`, `Double`, `List<String>`, `Blacklist`, `Difficulty`, `MinMax`
- Registers itself to the Forge event bus when enabled
- Provides `isEnabled(Class)` and `isEnabled(String)` static lookups
- Stores `ConfigOption` instances with `getConfigOption()`/`setConfigOption()` API
- Creates data keys via `createDataKey()` for `ModNBTData`

### `JsonFeature.java`
Extension of `Feature` for JSON-based configuration:
- Manages a list of `JsonConfig<T>` entries
- Loads JSON config files from disk (mod config folder)
- Syncs JSON configs to clients via `JsonConfigSyncMessage`
- Registers itself with `JsonFeatureDataReloadListener` for reload support
- Provides tag resolution helpers: `getTagElements()` for blocks, items, entities
- Inner class `JsonConfig<T>`: handles file I/O, Gson serialization, default file generation
- Inner class `SyncType`: maps `ResourceLocation` to sync callbacks

### `LoadFeature.java` (annotation)
Marks a class as a Feature to be auto-loaded:
- `module` — module ID (e.g. `"insanelib:base"`)
- `name`, `description` — metadata
- `enabledByDefault`, `canBeDisabled` — defaults
- `requiresMods` — optional mod dependencies

### `ConfigOption.java`
Abstract base for typed config options:
- `get()`, `set()`, `getConfigPath()` methods
- Concrete implementations embedded in `Feature.java` for each field type

### `Label.java` (deprecated)
Old annotation, marked for removal. Replaced by `@Config` and `@LoadFeature`.

---

## 3. Config Types

### `Config.java` (annotation)
Annotates static fields in Feature classes for automatic config generation:
- `name`, `description` — config metadata
- `min`, `max` — numeric range constraints

### `Blacklist.java`
`IdTagMatcher`-based black/whitelist config option:
- `blacklist` list + `blacklistAsWhitelist` toggle
- Provides `isBlackOrWhiteListed()` for blocks, items, entities, entity types, fluids, biomes
- Config option creates two Forge config entries (list + boolean toggle)

### `Difficulty.java`
Per-difficulty (Easy/Normal/Hard) config option:
- Stores three `double` values
- `getByDifficulty(Level)` returns the value for current difficulty

### `MinMax.java`
Min/max range config option:
- `getRandBetween()`, `getIntRandBetween()` for random values in range

---

## 4. Data Classes

### `IdTagMatcher.java`
Core data class for matching registry entries by ID or tag:
- Supports `Type.ID` and `Type.TAG`
- `location` (ResourceLocation) + optional `dimension` filter
- Match methods: `matchesBlock()`, `matchesItem()`, `matchesEntity()`, `matchesEntityType()`, `matchesFluid()`, `matchesBiome()`, `matchesEnchantment()`
- JSON serializer/deserializer (string format: `"modid:name"` or `"#modid:tag"`)
- `parseLine()` for config-based parsing (comma-separated: `"id,dimension"`)
- Config list option for Forge config spec

### `IdTagValue.java`
`IdTagMatcher` with an associated `double` value:
- JSON format: `{"id": "...", "value": 0.5}` or `{"tag": "#...", "value": 0.5}`
- Used for per-entity/per-item numeric config

### `IdTagRange.java`
`IdTagMatcher` with min/max `double` range:
- JSON format: `{"id": "...", "min": 0.5, "max": 1.0}`
- `getIntValue(RandomSource)` for random int in range
- `getFloatValue(RandomSource)` for random float in range

### `TwinIdTagMatcher.java`
Pair of `IdTagMatcher` — matches two items together:
- JSON format: `{"item_1": "...", "item_2": "..."}`
- `matchesItems(Item, Item)` for checking both match

### `SerializableAttributeModifier.java`
JSON-serializable attribute modifier:
- Record: `uuid`, `name`, `slots`, `attribute` (Supplier), `amount`, `operation`
- Gson serializer/deserializer
- Network buffer encode/decode
- `getModifier()` creates Minecraft `AttributeModifier`

### `JsonFeatureDataReloadListener.java`
Datapack reload listener:
- Singleton (`INSTANCE`)
- Triggers `loadJsonConfigs()` on all registered `JsonFeature` instances
- Stores `ICondition.IContext` for recipe/condition evaluation

### `InjectLootTableModifier.java`
Global loot modifier — injects one loot table into another:
- Uses Codec for serialization
- Resolves and appends loot from the injected table

---

## 5. Modules / Features

### `Modules.java`
Initializes the `"insanelib:base"` module with `COMMON` config type.

### `FixesFeature.java`
Various game fixes:
- **Fix Follow Range** (`MC-145656`): Patches `NearestAttackableTargetGoal` to use actual follow range attribute. Uses `insanelib:fix_follow_range` entity type tag.
- **Remove Zombie Bonus Health**: Removes the broken "Leader zombie bonus" attribute modifier from zombies.
- **Fix Jump Movement Factor**: Adjusts jump length based on movement speed when slowed. Optional slowdown-only mode. Hooks into `PlayerMixin.changeFlyingSpeed()`.

### `TagsFeature.java`
Entity metadata via `ModNBTData`:
- **Spawn Type**: Stores `MobSpawnType` on spawn (`FinalizeSpawn` event)
- **Explosion Causes Fire**: Sets explosion fire flag based on NBT tag
- **Experience Multiplier**: Multiplies XP drops based on NBT value
- **Sky/Block Light**: Updates light levels on entities every 2 ticks
- Static helpers: `isSpawnType()`, `setExplosionCausesFire()`, `setExperienceMultiplier()`

### `TimeStopNoPlayerOnline.java`
Stops time progression when no players are online:
- Stops game time via `ServerLevelAccessor.setTickTime(false)`
- Stops weather via datapack function (`stop_weather_if_no_player_online`)
- Optional Time Control mod integration (`stop_time_if_no_player_online_tc`)
- Optional Serene Seasons integration (`stop_season_if_no_player_online`)
- Uses `CommandFunction.CacheableFunction` for function execution
- Hooks: `PlayerLoggedOutEvent`, `PlayerLoggedInEvent`, `ServerTickEvent`, `ServerStartedEvent`

### `BetterFallingBlocks.java`
Smarter falling block behavior:
- **Break Instabreak Blocks**: Falling blocks break insta-break blocks instead of dropping
- **Fix Dupe Exploit**: Prevents duplication through dimensions
- Uses `insanelib:blacklisted_better_falling_blocks` block tag
- Config flags: `breakInstabreakBlocks`, `fixDupeExploit`

### `BetterFallingBlockExtensor.java`
Interface for falling block entity extension:
- `insanelib$setSource(Entity)` / `insanelib$getSource()` — tracks the entity that caused the falling block

---

## 6. Custom Events

### `AddEatEffectEvent`
Fired before eating effects are applied. **Cancelable** — canceling prevents vanilla effects.

### `BlockBurntEvent`
Fired when a block is burnt by fire. Provides `BlockPos` and `BlockState`.

### `CakeEatEvent`
Fired after a cake has been successfully eaten. Non-cancelable.

### `FallingBlockLandEvent`
Fired when a falling block successfully lands.

### `HurtItemStackEvent`
Fired on `ItemStack#hurt` after unbreaking has been applied. Allows modifying the `amount` of durability damage.

### `PlayerExhaustionEvent`
Fired when food exhaustion is applied. Allows modifying the `amount`.

### `PlayerSprintEvent`
Client-side only. Fired when the game checks if the player can sprint. **Cancelable** — canceling prevents sprinting.

### `PlayerUseItemSpeedModifierEvent`
Client-side only. Fired to modify the movement speed penalty when using items (default 0.2). **Cancelable**.

### `ILEventFactory`
Static factory for posting all custom events. Called from mixins.

---

## 7. Mixins

### `CakeBlockMixin`
Injects `CakeEatEvent` after successful cake eating.

### `EntityMixin`
Modifies `spawnAtLocation` for `FallingBlockEntity` — uses loot table drops instead of direct item spawn (for BetterFallingBlocks). Also handles dimension dupe fix.

### `FallingBlockEntityMixin`
Major mixin (~400 lines) for BetterFallingBlocks:
- Overrides falling block tick logic
- Smart placement: tries adjacent positions, handles waterlogging
- Breaks insta-break blocks before placing
- Tracks source entity via `BetterFallingBlockExtensor`
- Posts `FallingBlockLandEvent`

### `FireBlockMixin`
Injects `BlockBurntEvent` in `tryCatchFire()`.

### `ItemStackMixin`
Modifies durability damage amount via `HurtItemStackEvent` (in `ItemStack.hurt()`).

### `LivingEntityMixin`
Intercepts `addEatEffect()` — posts `AddEatEffectEvent`, cancels if event is canceled.

### `PlayerMixin`
- Modifies `getFlyingSpeed()` for jump movement factor fix
- Modifies `causeFoodExhaustion()` to post `PlayerExhaustionEvent`

### `LocalPlayerMixin` (client)
- Intercepts sprint check → posts `PlayerSprintEvent`
- Modifies item use speed constant → posts `PlayerUseItemSpeedModifierEvent`

### `ServerLevelAccessor`
Accessor for `ServerLevel.setTickTime(boolean)` — used by TimeStopNoPlayerOnline.

### `IntArrayTagAccessor`
Accessor for `IntArrayTag.toArray()`.

---

## 8. Network

### `NetworkHandler.java`
Registers 3 network messages on a `SimpleChannel`:
1. `MessageCreeperDataSync` — syncs creeper fuse/explosion radius to client
2. `JsonConfigSyncMessage` — syncs JSON configs to client
3. `EntityModNBTDataSync` — syncs entity ModNBTData to client

### `MessageCreeperDataSync.java`
Syncs creeper data (fuse, explosion radius) to all nearby players. Used when creeper stats are modified server-side.

### `JsonConfigSyncMessage.java`
Syncs JSON config data as string. Uses `JsonFeature.SyncType` to route to the correct handler.

### `EntityModNBTDataSync.java`
Syncs a single `ModNBTData` entry to client. Supports all NBT primitive types + CompoundTag.

### `ClientNetworkHandler.java`
Client-side handler for `MessageCreeperDataSync` — reads and applies creeper NBT data.

---

## 9. Commands

### `ILCommand.java`
Single command: `/insanelib set_time_played <players> <time>`
- Requires permission level 2
- Sets `PLAY_TIME` stat for target players

---

## 10. AI

### `ILNearestAttackableTargetGoal.java`
Extended `NearestAttackableTargetGoal` with:
- Configurable target chance (`setTargetChance()`, `setInstaTarget()`)
- Optional ignore line of sight (`setIgnoreLineOfSight()`)
- Builder-style API

---

## 11. Items

### `ILItemTier.java`
Custom `Tier` implementation for tool items:
- Constructor: `level`, `uses`, `speed`, `damage`, `enchantmentValue`, `repairIngredient` (Supplier)
- Uses `LazyLoadedValue` for repair ingredient

---

## 12. World

### `ILMobEffect.java`
`MobEffect` subclass with optional cure prevention:
- `canBeCured` flag controls whether `getCurativeItems()` returns empty list

### `ScheduledTasks.java`
Server tick task scheduler:
- Static list of `ScheduledTickTask`
- Ticks all tasks on `ServerTickEvent.END`
- Removes completed tasks
- Clears on server stop

### `ScheduledTickTask.java`
Abstract delayed task:
- `tickDelay` — ticks to wait before execution
- `run()` — abstract implementation
- `hasBeenExecuted()` — completion flag

---

## 13. Utilities

### `LogHelper.java`
Simple logging wrapper around `InsaneLib.LOGGER`:
- `error()`, `warn()`, `info()` with `String.format`

### `MCUtils.java`
Large utility class:
- `getMovementSpeedRatio()` — speed ratio for jump fix
- `addAttributeModifierToItemStack()` — non-overriding modifier addition
- `parseEffectInstance()` — parses `"effect,duration,amplifier"` strings
- `getOrCreatePersistedData()` — player persisted NBT
- Various entity, item, block helper methods

### `MathHelper.java`
- `round(double/float, places)` — decimal rounding
- `getAmountWithDecimalChance()` — probabilistic integer from decimal

### `ModNBTData.java`
Entity/player/item NBT data storage system:
- `getModData(Entity/Player/ItemStack, modId)` — namespaced compound NBT
- `get(Entity, ResourceLocation, Class)` / `put(Entity, ResourceLocation, value)` — typed data access with ResourceLocation keys
- `contains()`, `remove()` helpers
- `classToNBTType()` — maps Java types to NBT tag IDs
- Supports: Byte, Short, Int, Long, Float, Double, String, byte[], CompoundTag

### `TagUtils.java`
Tag checking utilities via ForgeRegistries:
- `isItemInTag()`, `isBlockInTag()`, `isEntityInTag()`, `isEntityTypeInTag()`
- Overloads for `ResourceLocation` and `TagKey`

### `ClientUtils.java`
Client rendering helpers:
- `setRenderColor()` / `resetRenderColor()` — blend setup
- `blitVericallyMirrored()` — flipped texture rendering

### `ConfigUtils.java`
- `split(path)` — splits dot-separated config paths

### `FileUtils.java`
- `ListFilesForFolder()` — recursive file listing

### `IntegratedPack.java`
Built-in datapack system:
- `addServerPack()` / `addResourcePack()` — registers packs with priority and enable condition
- `onAddPackFinders()` — hooks into `AddPackFindersEvent`
- Packs loaded from mod JAR resources

### `Utils.java`
- `searchEnum()` — case-insensitive enum lookup
- `formatDecimal()` — decimal formatting

### `ILGsonHelper.java`
Nullable JSON field helpers with validation:
- `getAsNullableInt()`, `getAsNullableDouble()`, `getAsNullableFloat()`

### JSON Validators
- `Validator<T>` — abstract base with `test()` and `getErrorMessage()`
- `IntMinMaxValidator` — int range validation
- `FloatMinMaxValidator` — float range validation
- `DoubleMinMaxValidator` — double range validation

### `JsonValidationException.java`
Simple exception for JSON validation errors.

### Weighted Random
- `IWeightedRandom` — interface with `getWeight()`
- `WeightedRandom` — static `getTotalWeight()`, `getRandomItem()` utility

---

## 14. Setup

### `Config.java` (setup)
Static config initialization:
- Creates `ModConfigSpec.Builder`
- Instantiates `CommonConfig` which triggers `Modules.init()` and `Module.loadFeatures()`

### `ILGlobalLootModifiers.java`
DeferredRegister for global loot modifiers:
- Registers `InjectLootTableModifier` codec

### `ILStrings.java` (deprecated)
Old string constants for NBT tags. Replaced by `TagsFeature`. Kept to prevent crashes.

---

## 15. Resources

### Data Tags
- `data/insanelib/tags/blocks/blacklisted_better_falling_blocks.json` — blocks excluded from BetterFallingBlocks
- `data/insanelib/tags/entity_types/fix_follow_range.json` — entities affected by follow range fix (all vanilla mobs)

### Integrated Datapacks
- `no_player_time_stop/` — `stop_weather_if_no_player_online.mcfunction`
- `no_player_time_stop_season/` — `stop_season_if_no_player_online.mcfunction` (Serene Seasons)
- `no_player_time_stop_tc/` — `stop_time_if_no_player_online_tc.mcfunction` (Time Control)

### Mixin Config
- `mixins.insanelib.json` — declares all mixin classes

### Other
- `META-INF/mods.toml` — mod metadata
- `META-INF/accesstransformer.cfg` — access transformers
- `pack.mcmeta` — resource pack metadata

---

## 16. Summary Table

| Category | Component | Files | Porting Complexity |
|----------|-----------|-------|--------------------|
| **Core** | Entry point, config init | `InsaneLib.java`, `setup/Config.java` | Medium (Forge→NeoForge API changes) |
| **Framework** | Feature/Module system | `Feature.java`, `Module.java`, `JsonFeature.java`, annotations, `ConfigOption.java` | Medium (config API changes) |
| **Features** | FixesFeature | 1 file | Low-Medium (attribute API changes) |
| **Features** | TagsFeature | 1 file | Medium (ModNBTData, explosion API) |
| **Features** | TimeStopNoPlayerOnline | 1 file + datapacks | Medium (CommandFunction API) |
| **Features** | BetterFallingBlocks | 2 files + mixin (~400 lines) | High (FallingBlockEntity changes) |
| **Events** | 8 custom events + factory | 9 files | Low (event base class changes) |
| **Mixins** | 10 mixins | 10 files | High (target method signatures may change) |
| **Network** | 3 messages + handler | 5 files | High (Forge→NeoForge networking overhaul) |
| **Commands** | ILCommand | 1 file | Low |
| **Data** | IdTagMatcher + variants | 6 files | Medium (registry API changes) |
| **Data** | Loot modifier | 2 files | Medium (Codec/loot API changes) |
| **Utilities** | All util classes | 14 files | Low-Medium |
| **World** | MobEffect, ScheduledTasks | 3 files | Low-Medium |
| **AI** | ILNearestAttackableTargetGoal | 1 file | Low |
| **Items** | ILItemTier | 1 file | Low (Tier API changes) |
| **Resources** | Tags, datapacks, mixin config | 8 files | Low |
