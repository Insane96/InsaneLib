# InsaneLib — Remaining Porting Work (1.20.1 → 1.21.1)

Only items still pending. Check the old version in D:/tmp for reference.

---

## 1. Data Classes

- [ ] `IdTagValue.java` — rework with `ObjTag<T>` instead of `IdTagMatcher`
- [ ] `IdTagRange.java` — rework with `ObjTag<T>`
- [ ] `TwinIdTagMatcher.java` — rework with `ObjTag<T>`
- [ ] `InjectLootTableModifier.java` — global loot modifier (`data/lootmodifier/`)

**Intentionally not porting:**
- ~~`TagUtils`~~ — replaced by `ObjTag<T>`
- ~~`ILGsonHelper`~~ — use plain `JsonObject` methods

---

## 2. Custom Events

- [ ] `AddEatEffectEvent` — before eating effects applied (cancelable)
- [ ] `BlockBurntEvent` — block burnt by fire
- [ ] `CakeEatEvent` — after cake eaten
- [ ] `HurtItemStackEvent` — durability damage modification
- [ ] `PlayerExhaustionEvent` — food exhaustion modification
- [ ] `PlayerSprintEvent` — sprint check (client, cancelable)
- [ ] `PlayerUseItemSpeedModifierEvent` — item use speed (client, cancelable)

---

## 3. Mixins

- [ ] `CakeBlockMixin` — CakeEatEvent injection
- [ ] `EntityMixin` — FallingBlock spawnAtLocation override
- [ ] `FireBlockMixin` — BlockBurntEvent injection
- [ ] `ItemStackMixin` — HurtItemStackEvent injection
- [ ] `LocalPlayerMixin` — sprint + item use speed events (client)
- [ ] `IntArrayTagAccessor` — toArray accessor

**Verify completeness (exist but may be incomplete):**
- [ ] `LivingEntityMixin` — AddEatEffectEvent injection
- [ ] `PlayerMixin` — flying speed + exhaustion event

---

## 4. Network

- [ ] `EntityModNBTDataSync.java` — generic entity NBT sync (needs design decision on StreamCodec)

---

## 5. Commands

- [ ] `ILCommand.java` — `/insanelib set_time_played`

---

## 6. Items

- [ ] `ILItemTier.java` — custom tool tier

---

## 7. Utilities

- [ ] `ClientUtils.java` — client rendering helpers
- [ ] `FileUtils.java` — recursive file listing
- [ ] `Utils.java` — enum search, decimal formatting
- [ ] `Validator.java` + `IntMinMaxValidator`, `FloatMinMaxValidator`, `DoubleMinMaxValidator`
- [ ] `JsonValidationException.java`

---

## 8. Setup

- [ ] `ILGlobalLootModifiers.java` — DeferredRegister for loot modifiers
- [ ] `FeatureEnabledCondition.java` — crafting recipe condition
- [ ] `FeatureEnabledLootCondition.java` — loot table condition

---

## 9. Resources / Data

- [ ] `data/insanelib/tags/blocks/blacklisted_better_falling_blocks.json`
- [ ] `data/insanelib/tags/entity_types/fix_follow_range.json`

---

## 10. ItemDefinitions — hooks not yet implemented (new in 1.21.1)

- [ ] `ItemMixin` — intercept `Item#components()` to return patched map from `PATCHED_COMPONENTS`
- [ ] `ItemStackMixin` — patch `getMaxDamage()`/`getMaxStackSize()` from `PATCHED_COMPONENTS` for existing stacks
- [ ] `chanceToNotUseDurability` / `chanceToUseDoubleDurability` — hook on `ItemStack#hurtAndBreak` or NeoForge durability event

---

## 11. Optional improvements

- [x] `JsonConfig<T>` — replace 4 constructors with single constructor + builder methods (`syncToClient()`, `onLoad()`, `withRegistryFor()`)