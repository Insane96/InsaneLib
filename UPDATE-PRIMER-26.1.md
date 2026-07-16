# InsaneLib 2.4.x (1.21.1) → 3.0.0.0 (26.1) update primer

A migration guide for mods depending on InsaneLib, updating from Minecraft 1.21.1 / NeoForge 21.1.x to Minecraft 26.1.2 / NeoForge 26.1.2.78.

This covers InsaneLib's own API changes plus the vanilla/NeoForge breakages you will almost certainly hit because the lib's mods all touch the same surfaces. For the full vanilla changes, read the official primers from 1.21.2 through 26.1: https://docs.neoforged.net/primer/docs/

---

## Build setup

| | 1.21.1 | 26.1 |
|---|---|---|
| Minecraft | 1.21.1 | 26.1.2 |
| NeoForge | 21.1.x | 26.1.2.78 |
| Java | 21 | **25** |
| Gradle | 8.x | 9.2.1 |
| ModDevGradle | 2.0.x | 2.0.141 |
| Parchment | yes | **not available** — remove the `parchment {}` block and properties |
| InsaneLib | 2.4.x | 3.0.0.0 |

- `neoforge.mods.toml`: **remove** the `modLoader="javafml"` and `loaderVersion=...` lines (and `loader_version_range` from `gradle.properties` / `generateModMetadata`).
- `*.mixins.json`: `"compatibilityLevel": "JAVA_25"`.
- ModDevGradle runs: `data { data() }` → `data { clientData() }`.
- `pack.mcmeta` (datapacks): `"pack_format": 48` → `"min_format": [101, 0], "max_format": [101, 1]`. Resource packs are at format 84.x.

## The rename you'll see everywhere

Vanilla is now deobfuscated with Mojang's real internal names. The two that dominate every diff:

- **`ResourceLocation` → `Identifier`** (same package, same methods: `fromNamespaceAndPath`, `parse`, `tryParse`, ...). `FriendlyByteBuf#readResourceLocation/writeResourceLocation` → `readIdentifier/writeIdentifier`. `ResourceKey#location()` → `identifier()`.
- **`net.minecraft.advancements.critereon` → `.criterion`** (typo finally fixed).

All InsaneLib signatures that took/returned `ResourceLocation` (e.g. `InsaneLib.id()`, `ModNBTData`, `ObjTag`, `MCUtils`) now use `Identifier`. A project-wide regex replace of `\bResourceLocation\b` → `Identifier` plus the two buf methods gets you 90% there.

---

## InsaneLib API changes

### Loot conditions & functions (vanilla "loot type unrolling")

The `LootItemConditionType` / `LootItemFunctionType` wrappers no longer exist; registries hold `MapCodec`s directly.

- All InsaneLib conditions/functions renamed `CODEC` → `MAP_CODEC` and implement `codec()` instead of `getType()`.
- `ILConditions.LOOT_CONDITIONS` / `ILLootFunctions.REGISTRY` are now `DeferredRegister<MapCodec<? extends LootItemCondition/LootItemFunction>>`.

Your own conditions/functions need the same treatment:

```java
// 1.21.1
@Override
public LootItemConditionType getType() { return MyConditions.MY_CONDITION.get(); }

// 26.1
@Override
public MapCodec<? extends LootItemCondition> codec() { return MAP_CODEC; }
```

### Global loot modifiers

- **`LootModifier` constructors now take `(LootItemCondition[] conditions, int priority)`** and `codecStart(inst)` emits both fields. All InsaneLib modifiers gained an `int priority` parameter right after `conditions`:
  - `DropMultiplierModifier(conditions, priority, item, tag, multiplier, amountToKeep, ignoreUnstackable)`
  - `ReplaceLootModifier(conditions, priority, itemToReplace, newItem, amountToReplace, chances, multipliers, keepDurability, chestsOnly)`
  - `InjectLootTableModifier(conditions, priority, lootTable)`
  - `LootPurgerModifier(conditions, priority, startRange, endRange, multiplierAtStart, applyToDamageable, blacklistedItemsTag, blacklistedEntityTypeTag)`
  - `DisenchantModifier(conditions, priority, blacklistedItemsTag)`
  - Convenience constructors/builders default to `DEFAULT_PRIORITY` (1000).
- **The `LootModifierManagerMixin` (LinkedHashMap ordering fix) is gone.** GLM ordering is now controlled by the JSON `"priority"` field (higher runs first, default 1000). If your datapacks relied on file/alphabetical ordering of GLMs, set explicit priorities.
- `codecStart(inst).and(...)` now produces a P2 — if your modifier has 7+ extra fields you'll blow past P8; flatten into a single `inst.group(...)` with `LOOT_CONDITIONS_CODEC.fieldOf("conditions")` and the priority field (see `ReplaceLootModifier.CODEC` for the pattern).
- `LootContextParams.TOOL` is now a `ContextKey<? extends ItemInstance>` (not `ItemStack`). `ItemInstance` still has `getEnchantmentLevel(Holder)` via the Neo extension.
- Loot context access renamed: `LootContextParam` → `ContextKey`, `context.hasParam/getParam/getParamOrNull` → `hasParameter/getParameter/getOptionalParameter`.

### Effects: EffectCure is gone

NeoForge removed the whole `EffectCure` system. InsaneLib emulates non-curability by **cancelling `MobEffectEvent.Remove`**:

- `ILMobEffect(category, color, canBeCured)` works as before, plus a new `canBeCured()` getter. The `fillEffectCures` override no longer exists.
- `MCUtils.createEffectInstance(..., canBeCured = false)` still works, backed by a weak set; new `MCUtils.isNonCurable(instance)` to query it.
- **Behavior changes:** removal via `/effect clear` is also blocked now (there is no way to distinguish a "cure" from a command), and the per-instance flag does **not** survive entity save/reload (effect-level `ILMobEffect` flags do).
- `MobEffectInstanceAccessor` (mixin accessor for `cures`) was deleted.

### Eating / food

- `AddEatEffectEvent` still exists and is still cancellable, but it now fires from `ApplyStatusEffectsConsumeEffect#apply` (the consumable rework removed `LivingEntity#addEatEffect`). It only fires for stacks that have the `FOOD` component.
- `FoodProperties` is now just `(nutrition, saturation, canAlwaysEat)`. Eat time lives on the `CONSUMABLE` component:
  - `MCUtils.computeFoodFormula(food, formula)` is **deprecated**: `eat_seconds` always evaluates to the 1.6s default.
  - Use `computeFoodFormula(food, consumable, formula)` instead to feed the real `Consumable#consumeSeconds()`.

### Client utilities

- `ClientUtils.setRenderColor` / `resetRenderColor` are **removed** — global `RenderSystem` color/blend state doesn't exist in the new GPU pipeline; color is per-draw now.
- `ClientUtils.blitVerticallyMirrored` now takes a **`GuiGraphicsExtractor`** — vanilla renamed `GuiGraphics` to `GuiGraphicsExtractor` (render-state extraction pattern). The `GuiGraphicsAccessor` invoker was deleted; the public `blit(Identifier, x0, y0, x1, y1, u0, u1, v0, v1)` overload covers the same use case.

### insanelib:enchantability — REMOVED

The `insanelib:enchantability` component and the Enchantability feature are gone. They existed only because 1.21.1 had no data-driven enchantability; vanilla now has the **`minecraft:enchantable`** component (record `Enchantable(int value)`). Migrate:

- Datapacks / Item Components JSONs: `"insanelib:enchantability": 20` → `"minecraft:enchantable": {"value": 20}`
- Code: `stack.get(ILDataComponents.ENCHANTABILITY.get())` → `stack.get(DataComponents.ENCHANTABLE)` (or `Item.Properties#enchantable(int)` when building items)

### ModNBTData

- Public signatures unchanged apart from `Identifier`, and the on-disk format is unchanged (UUIDs are still int arrays, now via `UUIDUtil.CODEC`).
- The `getList`/`getListPersisted` overloads **lost their `int type` parameter** — NBT lists are untyped since 1.21.5, so it was meaningless. Just drop the argument at call sites.

### Datagen

- `ILItemTagProvider` constructor is now `(PackOutput, CompletableFuture<HolderLookup.Provider>)` — no block-tags getter, no `ExistingFileHelper` (deleted from NeoForge).
- Listen to **`GatherDataEvent.Client`** (the run config is `clientData()`; it generates server data too) and prefer `event.createProvider(MyProvider::new)`.
- Tag appenders: `IntrinsicHolderTagsProvider`-based `tag(...)` returns the new `TagAppender` (`add` is still varargs; `addOptional` takes an element, not an `Identifier`).
- **Copper equipment tags** (`insanelib:equipment/*/copper`) now contain the real vanilla copper tools/armor from the Copper Age drop — drop any `addOptional` workarounds.

### Reload listeners

`AddReloadListenerEvent` → **`AddServerReloadListenersEvent`**, and every listener needs an `Identifier` key:

```java
event.addListener(MyMod.id("my_data"), MyReloadListener.INSTANCE);
```

### Unchanged (recompile only)

`ILModConfig` / the module & feature system, `IntegratedPack` (internals moved to `JarContentsPackResources`, public API identical), `ObjTag`, `ILNearestAttackableTargetGoal` (still takes a `Predicate<LivingEntity>`), `JsonFeature`, the network handler and messages, `ILAttributes`, `ILDataComponents`, `ILCriteriaTriggers`.

---

## Vanilla/NeoForge gotchas you WILL hit (field-tested on this port)

- `Level#isClientSide` field → `isClientSide()` method. `Level#random` is protected → `getRandom()`.
- **`Entity#getServer()` is gone** → `entity.level().getServer()` (nullable). Same for `ServerPlayer`.
- `LivingEntity#hurt(source, amount)` → `hurtServer(ServerLevel, source, amount)` (client split off).
- `Entity#spawnAtLocation(ItemLike)` → `spawnAtLocation(ServerLevel, ItemLike)`.
- **GameRules moved and renamed:** only on `ServerLevel` now, `getGameRules().get(GameRules.ENTITY_DROPS)` (typed `GameRule<T>`, no more `RULE_DOENTITYDROPS`/`getBoolean`). Package is `net.minecraft.world.level.gamerules`.
- `getMinBuildHeight()`/`getMaxBuildHeight()` → `getMinY()`/`getMaxY()` — **watch the off-by-one**: `getMaxY()` is inclusive where `getMaxBuildHeight()` was exclusive.
- **CompoundTag getters return `Optional`**: use `getIntOr(key, 0)`, `getCompoundOrEmpty(key)`, `getListOrEmpty(key)`, etc. `getAllKeys()` → `keySet()`; `putUUID/getUUID` removed → `store/read(key, UUIDUtil.CODEC)`. ⚠️ If you funnel getters through `Class#cast` or generics, the Optional change compiles fine and **explodes at runtime** — audit manually.
- **Registry lookups**: `Registry#get(Identifier)` now returns `Optional<Holder.Reference<T>>`; the direct nullable value is `getValue(Identifier)`. `getHolder(id)` → `get(id)`. `registry.getTag(tagKey)` → `get(tagKey)`.
- `EntityType#is(TagKey)` is gone → `entityType.builtInRegistryHolder().is(tag)`.
- `EntityTypePredicate.of(type)` → needs a `HolderGetter`, or build directly: `new EntityTypePredicate(HolderSet.direct(type.builtInRegistryHolder()))`.
- `new ChunkPos(BlockPos)` → `ChunkPos.containing(pos)`.
- `PotionContents` gained a 4th `Optional<String> customName` component.
- Spawn point: `LevelData#getSpawnPos()` → `getRespawnData().pos()`.
- Commands: `source.hasPermission(2)` → `.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))`; `ItemInput#getItem()` → `item().value()`.
- Events: `BlockEvent.BreakEvent` → `net.neoforged.neoforge.event.level.block.BreakBlockEvent`; `TagsUpdatedEvent#getRegistryAccess()` → `getRegistries()`, and `getUpdateCause()` → `event instanceof TagsUpdatedEvent.ClientPacketReceived`.
- Entity save data: `readAdditionalSaveData(CompoundTag)` → `readAdditionalSaveData(ValueInput)`; block entities: `saveWithoutMetadata`/`loadWithComponents` go through `TagValueOutput`/`TagValueInput` with a `ProblemReporter.ScopedCollector` (see `FallingBlockEntityMixin#insanelib$restoreBlockEntityData` for the pattern).
- Package moves: `Zombie` → `entity.monster.zombie`, `Pillager` → `entity.monster.illager`, `AbstractFish` → `entity.animal.fish`, `net.minecraft.Util` → `net.minecraft.util.Util`. **Mixins targeting inner classes (move controls etc.) need their target strings updated.**
- `Explosion` is now an interface; the concrete server-side class is `ServerExplosion` (its `fire` field is final — accessors need `@Mutable`).
- Client: `GuiGraphics` → `GuiGraphicsExtractor`; `renderLabels`-style screen methods are now `extractLabels`/`extractBackground`; `LocalPlayer#hasEnoughFoodToStartSprinting` moved to `Player#hasEnoughFoodToDoExhaustiveManoeuvres` (reachable via `LocalPlayer#isSprintingPossible`); the 0.2 item-use slowdown constant is now `LocalPlayer#itemUseSpeedMultiplier()` reading the `USE_EFFECTS` component.

## Suggested port order

1. Build files + metadata (template diff, Java 25, remove Parchment) — get Gradle syncing.
2. Global renames (`Identifier`, `criterion`, `isClientSide()`) — mechanical seds.
3. Compile-fix loop per subsystem: loot → NBT → events → datagen → client.
4. Re-verify every mixin target string against the decompiled sources (`~/.gradle/caches/neoformruntime/intermediate_results/transformSources_*_output.zip` + `neoforge-*-sources.jar`) — compile success proves nothing for `@At` targets.
5. `runData`, then boot `runServer` (with `defaultRequire: 1` any broken injection crashes immediately), then a `runClient` world join for client mixins.
