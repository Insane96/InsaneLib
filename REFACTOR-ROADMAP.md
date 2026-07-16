# InsaneLib 3.x refactor roadmap

Cleanups made possible by 26.1 vanilla/NeoForge features, to be tackled one by one. API breaks are acceptable.
Numbering matches the original survey; **#1 (fixFollowRange / NearestAttackableTargetGoalMixin) is already applied** and is not listed here.

Status legend: ⬜ todo · 🟨 in progress · ✅ done

---

## ⬜ #2 — Delete `util.weightedrandom`, use vanilla `net.minecraft.util.random`

**What it is:** `IWeightedRandom` (an interface with a single `getWeight()`) plus a static `WeightedRandom` helper (`getTotalWeight`, `getRandomItem`, `getWeightedItem`). It exists because old vanilla weighted random (`WeightedEntry`/`SimpleWeightedRandomList`) forced you to wrap everything in vanilla's entry types, which didn't fit the lib's Gson-loaded classes.

**Why it's obsolete:** vanilla 26.1 rewrote the package. `net.minecraft.util.random.WeightedRandom` now has the exact same shape as ours, but takes a `ToIntFunction<T>` instead of requiring an interface:

```java
// vanilla 26.1
public static <T> Optional<T> getRandomItem(RandomSource random, List<T> items, ToIntFunction<T> weightGetter)
public static <T> int getTotalWeight(List<T> items, ToIntFunction<T> weightGetter)
```

There's also `Weighted<T>(value, weight)` + `WeightedList<T>` (with `codec()`/`streamCodec()`, `getRandom(random)`, `getRandomOrThrow`) for data-driven weighted lists — useful if a class's weight is a plain field serialized by codec.

**Who uses it:** searched every mod in the repos folder — only **MobsPropertiesRandomness** (on 1.21.1), 6 files:
- implements `IWeightedRandom`: `MPRItem`, `MPRWeightedPreset`, `WeightedResourceKey`
- calls `WeightedRandom.getRandomItem`: `MPREquipmentProperty` (×2), `MPRLootTableProperty`, `MPRPresetsProperty`

**Migration for MPR (when it gets ported):**
```java
// before
implements IWeightedRandom                      // drop the interface, keep getWeight()
WeightedRandom.getRandomItem(random, items)     // insanelib

// after
net.minecraft.util.random.WeightedRandom.getRandomItem(random, items, MPRItem::getWeight).orElse(null)
```

⚠️ Behavior difference: the insanelib version **threw** on total weight ≤ 0; vanilla returns `Optional.empty()` (and throws only on negative). MPR call sites assume non-null — decide per call site whether `orElseThrow()` or a null check fits.

**Action:** delete `insane96mcp.insanelib.util.weightedrandom`, note the migration in the update primer.

---

## ⬜ #3 — Remove `AnvilScreenMixin` (needs confirmation)

The mixin cancels the anvil cost-label rendering when `cost == 0 && result slot has item`. In 26.1 vanilla, `AnvilScreen#extractLabels` wraps the entire cost label in `if (cost > 0)` — so at cost 0 nothing is rendered anyway and the cancel is dead code. (This looks true in 1.21.1 vanilla too, so it may have been dead for a while, or it exists to counter a *different mod's* patch to `getCost()`.)

**Before removing, confirm:** does any Insane96 mod (Survival Reimagined?) patch `AnvilMenu#getCost` or the anvil screen so that a label would render at cost 0? If not → delete the mixin, its mixins.json entry, and drop the (now empty) `client.AnvilScreenMixin` dependency. `AnvilMenuMixin` (allow pickup at cost 0) stays regardless — that's a real behavior change.

---

## ⬜ #4 — Item Components feature → `ModifyDefaultComponentsEvent` (the big one)

**Current architecture (all hand-rolled):**
1. `ItemComponentsReloadListener` parses `item_components` JSONs from datapacks.
2. `ItemComponentsFeature.onTagsUpdated` decodes them into `PATCHED_COMPONENTS` / `SYNCED_PATCHES` maps.
3. `ItemMixin` overrides `Item#components()` to return the patched map.
4. `ItemStackMixin` patches `getComponents`/`isSameItemSameComponents` for per-stack correctness.
5. `ItemComponentsSyncMessage` manually syncs patches to clients.

Steps 3–5 caused every Item Components bug of the 2.4.18–2.4.20 era: items not stacking (2.4.18.3), components not reaching clients (2.4.20.1), durability loss silently reverting because `getComponents` handed out fresh object identities (2.4.20.2).

**What changed in 26.1:** item default components are no longer baked at registration. They are **(re)built on every datapack reload**, with full registry context, and the resulting bound components are synced to clients by the platform (client join / reload — `Holder#areComponentsBound`, `DefaultDataComponentsBoundEvent`). NeoForge's `ModifyDefaultComponentsEvent` collects modifiers once at startup (mod bus), but the registered **initializers run every time components are rebuilt** — see `net.neoforged.neoforge.internal.DataComponentModifiers#apply(HolderLookup.Provider context, Item item, DataComponentMap.Builder builder)`. Since NeoForge PR #3122 the initializer receives the reload's `HolderLookup.Provider`, so it can resolve registry-dependent values per reload.

**The refactor:**
- Keep: the JSON format, `ItemComponentsReloadListener` (datapack parsing), `PROGRAMMATIC_PROVIDERS`, the `/insanelib get_data_components` command.
- Register one `ModifyDefaultComponentsEvent` listener with `modifyMatching((item, components) -> true /* or patch lookup */, (builder, context, item) -> applyParsedPatch(builder, context, item))`.
- Delete: `ItemMixin`, the components-related parts of `ItemStackMixin`, `ItemComponentsSyncMessage` (+ its NetworkHandler registration), the `TagsUpdatedEvent` application path.
- The identity/stacking/sync bug class disappears structurally: patched components *are* the item's default components.

**Must verify before starting:**
- Reload ordering: the JSON reload listener must have produced its parsed patches **before** the component rebinding pass runs in the same reload. If binding runs before custom reload listeners, the data would lag one reload behind — test, and if needed load the JSONs earlier (e.g. from the listener's prepare phase or a `PreparableReloadListener` dependency via `AddServerReloadListenersEvent#addDependency`).
- Component *removal* patches: `ModifyDefaultComponentsEvent`'s builder supports `#set` and `#remove` — confirm `remove` covers the feature's removal syntax.
- `GetMaxDamageEvent` / `IItemExtensionMixin` are unrelated (dynamic per-stack) and stay.

**Reading list:**
- 26.1 primer, "Data Components on Holders" section: https://docs.neoforged.net/primer/docs/26.1/
- Official docs on data components (incl. the event): https://docs.neoforged.net/docs/items/datacomponents/
- NeoForge PRs (the 26.1 evolution of the event):
  - #899 — original event (1.20.6): https://github.com/neoforged/NeoForge/pull/899
  - #3041 — Expose applied default components (26.1): https://github.com/neoforged/NeoForge/pull/3041
  - #3048 — Improve component access (26.1): https://github.com/neoforged/NeoForge/pull/3048
  - #3122 — Add more context (`HolderLookup.Provider`) to modifiers (26.1): https://github.com/neoforged/NeoForge/pull/3122
- Decompiled references (extract from the neoforge sources jar): `net/neoforged/neoforge/internal/DataComponentModifiers.java`, `event/ModifyDefaultComponentsEvent.java`, `event/DefaultDataComponentsBoundEvent.java`

---

## ⬜ #5 — `TimeStopNoPlayerOnline`: drop the function-polling machinery

**Current architecture, and why it's convoluted:**
- On server start / player login / logout, a mixin accessor (`ServerLevelAccessor#setTickTime`) force-writes the overworld's private `tickTime` field to freeze/unfreeze **game time** (the tick counter — no gamerule controls this).
- Separately, **every 20 ticks**, the feature executes up to three shipped-datapack `.mcfunction`s (`no_player_time_stop*` integrated packs) that run `execute if entity @a run gamerule <rule> true / execute unless ... false` — i.e. it *polls* player presence once a second and toggles `doWeatherCycle` (vanilla), `doDaylightCycle_tc` (Time Control), `doSeasonCycle` (Serene Seasons) via chat-command syntax.

The function/datapack indirection existed because there was no sane way to reference *third-party* gamerules from code without a hard dependency, and vanilla gamerules were awkward to set programmatically. Downsides: 1-second polling instead of event-driven, three shipped packs, string commands that silently break on renames (the `doWeatherCycle` → `advance_weather` rename did exactly that in this port — already fixed).

**The refactor:**
- Vanilla rules: set `GameRules.ADVANCE_WEATHER` (and optionally `ADVANCE_TIME`) **directly from the login/logout/server-started handlers** via the typed GameRules API — no polling, no datapack, no command parsing. Note 26.1 renamed the rules: `doDaylightCycle`→`advance_time`, `doWeatherCycle`→`advance_weather`.
- Third-party rules (Time Control, Serene Seasons): either keep the function approach for just those two packs, or run the gamerule command through `server.getCommands()` from the same handlers (still string-based, but event-driven and pack-free).
- Keep `ServerLevelAccessor#setTickTime`: freezing the raw game-time counter has **no gamerule equivalent** even in 26.1 (`advance_time` only gates the world-clock/day-time advancement; the private `tickTime` field gates the whole `ServerLevel#tickTime()` call).
- End state: the `no_player_time_stop` pack disappears entirely; `_tc` and `_season` packs stay only if the function approach is kept for third-party rules.

---

## ⬜ #6 — `ModNBTData` / NBT Tags → NeoForge data attachments (long-term)

**What attachments are:** typed, registered per-holder data (`Entity`, `BlockEntity`, `ChunkAccess`, `Level`) — `entity.getData(TYPE)` / `setData(TYPE, value)` — replacing the untyped "write flags into `getPersistentData()`" pattern. The 26.1 `AttachmentType.Builder` covers everything the lib hand-rolls today:

- `.serialize(MapCodec)` — persistence to disk (with optional `shouldSerialize` predicate)
- `.copyOnDeath()` — the `MCUtils.getOrCreatePersistedData` / `PERSISTED_NBT_TAG` semantics
- `.sync(StreamCodec)` or `.sync(sendToPlayer, StreamCodec)` — **built-in client sync, automatic on tracking start** — replaces `CreeperDataSyncMessage`, its `Fixes.onStartTracking` handler, and that part of `NetworkHandler`

**What would migrate:**
- `NbtTags` keys (`explosion_causes_fire`, `xp_multiplier`, `sky_light`, `block_light`, `no_ammo_consumption`) → one small attachment record (or individual attachments); readers like `ProjectileWeaponItemMixin`/`NbtTags` use `getData`.
- `CreeperDataSyncMessage` (maxSwell/explosionRadius) → a synced attachment; the message class, handler and manual tracking-start sync all go away.
- `ModNBTData`'s entity/player paths → thin wrappers or full deprecation.

**Why it's the most disruptive item:**
- Every dependent mod that touches `ModNBTData` (MPR writes mob properties, Enhanced AI reads the tags, Progressive Bosses, etc.) has to move call sites.
- **Data-format break:** attachments serialize under `neoforge:attachments`, not under the mod's ad-hoc keys in entity NBT. Any *user-facing* JSON/datapack/command that referenced raw NBT paths (e.g. MPR configs or the wiki-documented NBT Data paths from https://github.com/Insane96/InsaneLib/wiki) breaks. This needs a documented mapping or a read-fallback for one version.
- `SKY_LIGHT`/`BLOCK_LIGHT` are written every other tick purely so JSON conditions can read light levels — worth reconsidering entirely (a condition could query the level directly instead of round-tripping through stored data).

**Middle ground option:** keep the `ModNBTData` API as a facade backed by a single attachment, so dependent mods keep compiling while the storage modernizes underneath.

**Reading:** https://docs.neoforged.net/docs/datastorage/attachments — plus `net/neoforged/neoforge/attachment/AttachmentType.java` and `AttachmentSync.java` in the neoforge sources jar.
