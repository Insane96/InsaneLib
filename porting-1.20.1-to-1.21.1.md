# InsaneLib 1.20.1 — Full Feature Inventory for Porting to 1.21.1

This document lists every component in the 1.20.1 branch that needs to be ported to 1.21.1.

---

## 0. Porting Notes & Improvements

### Key Decision: Replace `IdTagMatcher` with `ObjTag`
The `IdTagMatcher` system must be replaced by the new generic, type-safe `ObjTag<T>` system (already prototyped in the `ObjTag` branch). This affects:
- [ ] **`IdTagMatcher.java`** — replaced entirely by `ObjTag<T>`
- [ ] **`IdTagValue.java`** — needs rework to use `ObjTag<T>` instead of `IdTagMatcher`
- [ ] **`IdTagRange.java`** — needs rework to use `ObjTag<T>` instead of `IdTagMatcher`
- [ ] **`TwinIdTagMatcher.java`** — needs rework to use `ObjTag<T>` instead of `IdTagMatcher`
- [ ] **`Blacklist.java`** — internally uses `IdTagMatcher`, must be migrated to `ObjTag<T>`
- [ ] **`Feature.java`** — has `IdTagMatcher.Config` option type in `loadConfigOptions()`, must be updated
- [ ] **`JsonFeature.java`** — tag resolution helpers (`isItemInTag`, `isBlockInTag`, etc.) can be simplified since `ObjTag.matches()` handles this natively

### Feature/Module Framework Improvements

#### Remove deprecated code
- [x] **`Label.java`** — deprecated annotation, remove entirely. All references in `Feature.java` that fall back to `@Label` should be deleted.
- [x] **`ILStrings.java`** — deprecated since 1.20.2, remove entirely.
- [x] **`Feature(Module, boolean, boolean)` constructor** — marked `@Deprecated(forRemoval = true)`. Remove and keep only the no-arg constructor + `init()` pattern.
- [ ] **`JsonFeature(Module, boolean, boolean)` constructor** — same as above, remove.

#### `Feature.java` improvements
- [x] **`loadConfigOptions()` reflection**: The large if/else chain for field types (Double, Integer, List, Enum, MinMax, Difficulty, Blacklist, IdTagMatcher) is fragile and hard to extend. Consider using a registry of type handlers or a `Map<Class<?>, ConfigOptionFactory>` to make it extensible without modifying the method.
- [x] **`LogHelper` → `InsaneLib.LOGGER`**
- [x] **`fieldNameToConfigOption()`**: Works fine, no changes needed.

#### `Module.java` improvements
- [x] **`FMLJavaModLoadingContext.get()`** — deprecated/removed in NeoForge 1.21.1. The `Module` constructor needs the mod event bus passed in explicitly (there's already a TODO comment: `//TODO 1.21.1, pass context`).
- [x] **`loadFeatures()` static method**: Uses `ModFileScanData` which still exists in NeoForge but the API may differ slightly. Verify compatibility.
- [x] **Feature instantiation**: `instantiateFeature()` first tries the 3-arg constructor, then falls back to no-arg + `init()`. Since the 3-arg constructor is being removed, simplify to only use no-arg + `init()`.

#### `JsonFeature.java` improvements
- [ ] **`JSON_CONFIGS` visibility**: Change from `public` to `private` and add `addJsonConfig()` method (already done in ObjTag branch).
- [ ] **`JsonConfig<T>` constructor overloads**: Replace 4 constructors with single constructor + builder methods (already done in ObjTag branch: `syncToClient()`, `onLoad()`, `withRegistryFor()`).
- [ ] **Tag resolution helpers**: The 8 static methods (`isItemInTag`, `isBlockInTag`, `isEntityInTag`, `getAllItems`, `getAllBlocks` with client/server variants) are verbose and repetitive. With `ObjTag<T>`, most of these become unnecessary since `ObjTag.matches()` handles tag resolution generically. Consider removing or significantly simplifying.
- [ ] **`getGson()` centralization**: Already done in ObjTag branch — Gson creation with optional `ObjTag.AdapterFactory` registration.

#### `Blacklist.java` rework
- [ ] Currently stores `List<IdTagMatcher>` and has 7 overloaded `isBlackOrWhiteListed()` methods (for Block, Item, Entity, EntityType, Fluid, Biome, Enchantment). With `ObjTag<T>`, this can be simplified to a single generic `isBlackOrWhiteListed(T obj)` method, since `ObjTag<T>.matches()` handles all registry types.

#### `ConfigOption.java` — consider simplification
- [ ] The abstract `ConfigOption<T>` with many inner classes (`DoubleOption`, `IntOption`, `StringListOption`, `EnumOption`, `GenericOption`) works but is tightly coupled to Forge's `ModConfigSpec`. Review if NeoForge's config API has changed enough to warrant a refactor.

---

## 1. Entry Point

- [x] `InsaneLib.java` — Main mod class (`@Mod`)
- [x] `ILConfig.java` — NEW, replaces `setup/Config.java`. Config initialization with `ModConfigSpec`
- [x] `InsaneLibClient.java` — Client-side mod class

---

## 2. Feature/Module Framework

- [x] `Feature.java` — Base class for all features (moved to `core`)
- [x] `Module.java` — Module system, groups features (moved to `core`)
- [ ] `JsonFeature.java` — Extension of Feature for JSON configs (blocked on ObjTag)
- [x] `LoadFeature.java` — Annotation for auto-loading features (moved to `core`)
- [x] `ConfigOption.java` — Abstract base for typed config options (moved to `core.config`)
- [x] ~~`Label.java`~~ — REMOVED (deprecated, not porting)

---

## 3. Config Types

- [x] `Config.java` annotation — Annotates static fields for auto config generation (moved to `core.config`)
- [ ] `Blacklist.java` — IdTagMatcher-based black/whitelist (blocked on ObjTag)
- [x] `Difficulty.java` — Per-difficulty config option (moved to `core.config`)
- [x] `MinMax.java` — Min/max range config option (moved to `core.config`)
- [x] `ConfigUtils.java` — Config path splitting utility (moved to `core.config`)

---

## 4. Data Classes

- [ ] `IdTagMatcher.java` — To be replaced by `ObjTag<T>`
- [ ] `IdTagValue.java` — Needs rework with `ObjTag<T>`
- [ ] `IdTagRange.java` — Needs rework with `ObjTag<T>`
- [ ] `TwinIdTagMatcher.java` — Needs rework with `ObjTag<T>`
- [ ] `SerializableAttributeModifier.java` — JSON-serializable attribute modifier
- [ ] `JsonFeatureDataReloadListener.java` — Datapack reload listener
- [ ] `InjectLootTableModifier.java` — Global loot modifier

---

## 5. Modules / Features

- [ ] `Modules.java` — Initializes the `"insanelib:base"` module
- [x] `FixesFeature.java` — Follow range fix, zombie bonus health, jump movement factor
- [x] `TagsFeature.java` — Entity metadata (spawn type, explosion fire, XP multiplier, light)
- [ ] `TimeStopNoPlayerOnline.java` — Stops time when no players online
- [ ] `BetterFallingBlocks.java` — Smarter falling block behavior
- [ ] `BetterFallingBlockExtensor.java` — Interface for falling block source tracking

---

## 6. Custom Events

- [ ] `AddEatEffectEvent` — Before eating effects applied (cancelable)
- [ ] `BlockBurntEvent` — Block burnt by fire
- [ ] `CakeEatEvent` — After cake eaten
- [ ] `FallingBlockLandEvent` — Falling block lands
- [ ] `HurtItemStackEvent` — Durability damage modification
- [ ] `PlayerExhaustionEvent` — Food exhaustion modification
- [ ] `PlayerSprintEvent` — Sprint check (client, cancelable)
- [ ] `PlayerUseItemSpeedModifierEvent` — Item use speed (client, cancelable)
- [ ] `ILEventFactory` — Static factory for posting events

---

## 7. Mixins

- [ ] `CakeBlockMixin` — CakeEatEvent injection
- [ ] `EntityMixin` — FallingBlock spawnAtLocation override
- [ ] `FallingBlockEntityMixin` — BetterFallingBlocks logic (~400 lines)
- [ ] `FireBlockMixin` — BlockBurntEvent injection
- [ ] `ItemStackMixin` — HurtItemStackEvent injection
- [ ] `LivingEntityMixin` — AddEatEffectEvent injection
- [ ] `PlayerMixin` — Flying speed + exhaustion event
- [ ] `LocalPlayerMixin` — Sprint + item use speed events (client)
- [ ] `ServerLevelAccessor` — setTickTime accessor
- [ ] `IntArrayTagAccessor` — toArray accessor

---

## 8. Network

- [ ] `NetworkHandler.java` — Channel registration (3 messages)
- [ ] `MessageCreeperDataSync.java` — Creeper fuse/radius sync
- [ ] `JsonConfigSyncMessage.java` — JSON config sync
- [ ] `EntityModNBTDataSync.java` — Entity NBT data sync
- [ ] `ClientNetworkHandler.java` — Client-side message handler

---

## 9. Commands

- [ ] `ILCommand.java` — `/insanelib set_time_played`

---

## 10. AI

- [x] `ILNearestAttackableTargetGoal.java` — Extended target goal

---

## 11. Items

- [ ] `ILItemTier.java` — Custom tool tier

---

## 12. World

- [x] `ILMobEffect.java` — MobEffect with cure prevention
- [ ] `ScheduledTasks.java` — Server tick task scheduler
- [ ] `ScheduledTickTask.java` — Abstract delayed task

---

## 13. Utilities

- [ ] `LogHelper.java` — Logging wrapper (→ rename to `ILLogger`)
- [ ] `MCUtils.java` — Large utility class
- [ ] `MathHelper.java` — Math utilities
- [ ] `ModNBTData.java` — Entity/player/item NBT storage
- [ ] `TagUtils.java` — Tag checking utilities
- [ ] `ClientUtils.java` — Client rendering helpers
- [ ] `FileUtils.java` — Recursive file listing
- [ ] `IntegratedPack.java` — Built-in datapack system
- [ ] `Utils.java` — Enum search, decimal formatting
- [ ] `ILGsonHelper.java` — Nullable JSON field helpers
- [ ] `Validator.java` — Abstract JSON validator
- [ ] `IntMinMaxValidator.java` — Int range validation
- [ ] `FloatMinMaxValidator.java` — Float range validation
- [ ] `DoubleMinMaxValidator.java` — Double range validation
- [ ] `JsonValidationException.java` — JSON validation exception
- [ ] `IWeightedRandom.java` — Weighted random interface
- [ ] `WeightedRandom.java` — Weighted random utility

---

## 14. Setup

- [x] `Config.java` (setup) — Replaced by `ILConfig.java`
- [ ] `ILGlobalLootModifiers.java` — DeferredRegister for loot modifiers
- [x] ~~`ILStrings.java`~~ — REMOVED (deprecated, not porting)
- [ ] `FeatureEnabledCondition.java` — Crafting recipe condition
- [ ] `FeatureEnabledLootCondition.java` — Loot table condition

---

## 15. Resources

- [ ] `data/insanelib/tags/blocks/blacklisted_better_falling_blocks.json`
- [ ] `data/insanelib/tags/entity_types/fix_follow_range.json`
- [ ] `no_player_time_stop/` datapack
- [ ] `no_player_time_stop_season/` datapack (Serene Seasons)
- [ ] `no_player_time_stop_tc/` datapack (Time Control)
- [ ] `mixins.insanelib.json` — Mixin config
- [ ] `META-INF/mods.toml` — Mod metadata
- [ ] `META-INF/accesstransformer.cfg` — Access transformers
- [ ] `pack.mcmeta` — Resource pack metadata

---

## 16. Summary Table

| Category | Component | Files | Porting Complexity |
|----------|-----------|-------|--------------------|
| **Core** | Entry point, config init | `InsaneLib.java`, `ILConfig.java` | Medium (Forge→NeoForge API changes) |
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
| **Utilities** | All util classes | 17 files | Low-Medium |
| **World** | MobEffect, ScheduledTasks | 3 files | Low-Medium |
| **AI** | ILNearestAttackableTargetGoal | 1 file | Low |
| **Items** | ILItemTier | 1 file | Low (Tier API changes) |
| **Resources** | Tags, datapacks, mixin config | 9 files | Low |