# Upcoming
* Ported to Minecraft 26.1.2 (NeoForge 26.1.2.116, Java 25)

## Data Pack / Modpack creators
* Vanilla renamed many ids and changed many component formats between 1.21.1 and 26.1.2: data packs, `item_components` definitions and json configs must be updated to the new vanilla formats
  * Attribute ids no longer have the `generic.`/`player.` prefix (e.g. `minecraft:generic.movement_speed` is now `minecraft:movement_speed`), including the ones in `players_attribute_modifiers.json`
  * `minecraft:attribute_modifiers` is now a list instead of an object with a `modifiers` field
  * Food effects and eat time are no longer part of `minecraft:food`, they're in `minecraft:consumable`
* Removed `insanelib:enchantability` item component: vanilla now has `minecraft:enchantable` (e.g. `"minecraft:enchantable": {"value": 15}`) which does the same
* Global loot modifiers (`insanelib:replace_loot`, `insanelib:drop_multiplier`, `insanelib:loot_purger`, `insanelib:disenchant`) now support NeoForge's `priority` field (default 1000, higher is applied first)
  * NeoForge now loads every file in `data/<namespace>/loot_modifiers/` and no longer uses `global_loot_modifiers.json`; the order is given by `priority`
* `insanelib:recipe_removal` now checks the result shown by the recipe (recipe book display). Recipes without a display (e.g. special recipes) are never removed
* Fixed loot pool injections (`loot_pool_injections`) not being applied when the world is loaded (they only worked after a `/reload`, using the injections of the previous load)
* Fixed `merge_components` in `item_components` re-adding a component removed by a previous definition for the same item (e.g. one targeting the item and one targeting a tag containing it). A removed component now counts as absent: the merge json alone becomes the new value, and if it's not a valid component the removal is kept and an error is logged
* Fixed a `merge_components` that fails to decode also undoing a previous removal of that component
* Fixed `insanelib:enchant_with_treasure` and `insanelib:enchant_randomly_weightless` loot functions not enchanting books
* Items in the copper `insanelib:equipment/*` tags are now the vanilla copper tools and armor
* Time Stop No Player Online
  * Now stops the day time (world clocks) too, other than the game time
  * The weather integrated pack now uses the `advance_weather` game rule (was `doWeatherCycle`)
* Non-curable effects (`ILMobEffect` / `MCUtils#createEffectInstance`) can no longer be removed by anything that clears all the effects when consumed (milk, totems, `minecraft:clear_all_effects` consume effect); `/effect clear` still removes them

## Technical
* `ResourceLocation` is now `Identifier` in every method signature
* Removed the `MixinExtras` jar-in-jar, NeoForge already provides it
* Loot conditions and functions now implement `codec()` (`ILConditions` and `ILLootFunctions` now register `MapCodec`s)
* Loot modifiers constructors now take the `priority` after the conditions
* `AddEatEffectEvent` is now fired when a food item is consumed (from `Consumable#onConsume`); canceling it prevents the `minecraft:apply_effects` consume effects of the food. Added `getStack()`
* `PlayerUseItemMovSpeedEvent` starting speed modifier is now the one from the item's `minecraft:use_effects` component. `ILEventFactory#onUseItemMovementSpeedModifier` takes the original speed modifier
* `PlayerSprintEvent` is now checked on `LocalPlayer#isSprintingPossible`, so it also stops the player from sprinting, not only from starting to sprint
* Effects cures
  * Removed `MobEffectInstanceAccessor` and `ILMobEffect#fillEffectCures` (NeoForge removed `EffectCure`)
  * Added `ICurableEffectInstance` (implemented on `MobEffectInstance`), `ILMobEffect#canBeCured()`, `MCUtils#isCurable` and `MCUtils#removeCurableEffects`
  * Non-curable effect instances created with `MCUtils#createEffectInstance` are not saved: they become curable again after a reload of the entity
* `MCUtils#computeFoodFormula` now takes the `Consumable` (nullable) to get `eat_seconds`
* `MCUtils#hurtIgnoreInvulnerability` now uses `hurtServer` and always returns false on client side
* `ItemComponentsReloadListener.PROGRAMMATIC_PROVIDERS` now receive a `HolderLookup.Provider` instead of `RegistryAccess`
* Item components patches are now computed on `DefaultDataComponentsBoundEvent` (server side only) instead of `TagsUpdatedEvent`, since default components are now bound after tags
* `RecipeRemovalUtils` is now applied by a `RecipeManager` mixin before recipes are finalized; `RecipeRemovalUtils#removeTaggedRecipes(RecipeMap, HolderLookup.Provider)` replaces the old event handlers
* `ClientUtils`
  * Removed `setRenderColor` and `resetRenderColor` (shader color no longer exists), use the color overload of `blitVerticallyMirrored` instead
  * `blitVerticallyMirrored` now takes a `GuiGraphicsExtractor`
* `ExplosionAccessor` now targets `ServerExplosion`
* `ILBlockTagProvider` and `ILItemTagProvider` no longer take an `ExistingFileHelper`; data generation now uses `GatherDataEvent.Client`
* `ModNBTData#getList` ignores the type parameter, since lists no longer have a fixed type
* `PoolInjectionReloadListener` has been renamed to `PoolInjectionLoader` and is no longer a reload listener: injections are loaded by `ReloadableServerRegistriesMixin` (`PoolInjectionLoader#load(ResourceManager)`) right before loot tables, since loot tables are loaded before reload listeners
* Removed `LootModifierManagerMixin` (NeoForge now sorts loot modifiers by priority)
