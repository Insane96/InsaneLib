# 3.0.1.0-beta
* Ported 2.4.34.0, 2.4.35.0 and 2.4.36.0 changes from 1.21.1

## Data Pack / Modpack creators
* Mods using `ILModConfig` (InsaneLib included) now automatically get a config screen (NeoForge's `ConfigurationScreen`) in the Mods list (basically all my mods), unless they register their own. Entries without a translation show their config name instead of the raw translation key
* Item Components' "Stack Limit" config option has been replaced by "Increase Stack Limit" (true by default), which raises the vanilla limit of 99 to 1,073,741,823. Requires a Minecraft restart
  * Previously the configured value was ignored by the item, item stack template and component codecs, which always used 9999 since they are built before the config is loaded
  * Item counts over 999 now show as 1k, 1m, 1b
  * The limit is half the max integer value, as vanilla sums the counts of two stacks when merging them (e.g. shift-click), which would overflow and delete the items
* Better Falling Blocks
  * Can now be disabled
  * Fixed falling blocks not going through portals
  * Fixed falling blocks that can't be placed never disappearing when `entity_drops` is false (or the falling block doesn't drop)
  * Fixed falling blocks getting stuck forever on top of insta-break blocks with a collision (e.g. slime blocks) when breaking insta-break blocks is enabled. They now land on them
* Moved "Fix Dupe Exploit" from Better Falling Blocks to Fixes ("Fix Falling Block Dupe"). It now also works when Better Falling Blocks is disabled (vanilla falling blocks going through an End portal)
* Fixed `insanelib:drop_multiplier` loot modifier deleting unstackable items (e.g. tools and armor) matching the item/tag from the loot when `ignore_unstackable` is true (the default). They are now left untouched as intended
* Fixed `insanelib:loot_purger` loot modifier
  * With `start_range` > 0 the start range was subtracted twice, purging all the loot right after `start_range` and reaching 0 only at `start_range + end_range` instead of `end_range`
  * With `apply_to_damageable`, items generated beyond `end_range` were repaired (even fully). Beyond `end_range` the loot is now left untouched
  * Fixed a division by zero when `end_range` is equal to `start_range`
  * Fixed loot generated more than ~46000 blocks from spawn being treated as if it was at spawn
* `insanelib:replace_loot`
  * With `amount_to_replace`, the remaining items no longer lose their components (enchantments, damage, etc.)
  * Empty `chances` or `multipliers` are now reported as an error when loading instead of crashing when generating loot
* Fixed item stacks keeping the components of an `item_components` definition after a `/reload` (or a server change) removed it, until the stack was reloaded from disk
* Fixed possible issues in singleplayer caused by the client and the integrated server modifying `item_components` patches at the same time
* Fixed Player Attributes modifiers being lost after a `/reload`
* Fixed creative tabs being rebuilt from the integrated server thread on `/reload` in singleplayer
* Fixed an invalid `loot_pool_injections` entry breaking the loot table loading. It's now logged and skipped
* Item knockback multiplier (`insanelib:knockback_multiplier`) is no longer applied to knockbacks unrelated to the attack (e.g. an explosion after being hit)
* Time Stop No Player Online
  * Disabling the feature while time is stopped now makes game time and day time advance again without restarting
  * Fixed the functions not being updated after a `/reload`
* The order of features and modules in the config files is now always the same
* An invalid id in a config (e.g. uppercase letters) is now logged and ignored (or the default value is used) instead of crashing

## Technical
* `ILModConfig` can now be used with `ModConfig.Type.SERVER` configs
  * Fixed `Cannot get config value before config is loaded` crash when a SERVER config gets unloaded (server stopping, leaving a singleplayer world). `ModConfigEvent.Unloading` is now ignored and `@Config` fields keep the last values read
  * `ConfigOption#set` (and `Blacklist`, `MinMaxConfig`, `DifficultyBasedConfig`, `ObjTag` options) and `Feature#setEnabledConfig` now do nothing and log a warning if the config is not loaded instead of throwing
  * When the server reloads a SERVER config because its file changed, the config is now synced again to connected clients (NeoForge syncs it only on login) and operators get a chat message
* Added `ConfigUtils#isLoaded` and `ConfigUtils#canSet`
* Added `ILModConfig#getModContainers` and `ILConfigScreen`
* Fixed clients being disconnected when a synced JsonConfig is longer than 32767 characters
* JsonFeature: fixed reloading a JsonConfig removing all the entries read that are equal to the old ones (e.g. records). An empty JsonConfig file is now reported as a parsing error and keeps the old entries
* JsonFeature: `JsonConfig#onLoad` on the client is now called after the config has been synced for configs synced to clients. When joining a server it was called before receiving the server's values
* JsonConfig sync no longer re-reads the json in singleplayer / LAN host, as the lists are shared with the integrated server
* Added `SnapshotList`, a thread-safe list replaced as a whole. JsonConfigs using it as their list are now updated at once on reload, so in singleplayer the client never sees them empty or gets a `ConcurrentModificationException` while the integrated server reloads them. Lists of JsonConfigs read on the client should use it
* `ItemComponentsReloadListener.PATCHED_COMPONENTS` and `SYNCED_PATCHES` are now immutable maps replaced as a whole instead of being modified. Added `ItemComponentsReloadListener#getUnpatchedComponents` (the components bound to the item's holder)
* `ItemComponentsSyncMessage` is now ignored in singleplayer / LAN host, as the patches are shared with the integrated server
* Removed `ItemComponentsFeature.stackLimit`, use `ItemComponentsFeature#getStackLimit`. Added `ItemComponentsFeature#stackSizeCodec`. `Item.ABSOLUTE_MAX_STACK_SIZE` is no longer modified
* Fixed `MCUtils#addAttributeModifierToItemStack` never adding the modifier
* Fixed `DisenchantModifier` and `LootPurgerModifier` created through their `Builder` crashing (blacklist tags left `null`)
* `AttributeModifierOperationSerializer` now serializes the operation as a string (e.g. `"add_value"`), the same format it deserializes, instead of an object
* `ModNBTData#get(ItemStack, ...)` now returns the type default (0/false) like the other variants instead of `null` when the stack has no custom data
* `ModNBTData#remove` no longer creates missing compounds and removes the ones left empty. Removing the last mod data from an item stack now removes the `custom_data` component, so it stacks again with stacks without it
* `Feature.isEnabled(Class)` now returns false instead of crashing for features that are not loaded
* `InsaneLib.ONE_DECIMAL_FORMATTER` is now also available on dedicated servers
* `CurrentAttacker#resolve` now falls back to `getLastHurtByMob()` only if the victim was hurt by it in the current tick
* `ILNearestAttackableTargetGoal`'s default target chance now goes through `reducedTickDelay` like vanilla's (10 -> 5)
* Fixed `SerializableAttributeModifier#fromNetwork` reading the wrong id in the error message for an unknown attribute
* `TimeStopNoPlayerOnline.STOP`, `TC` and `SEASONS` are now the functions' ids
* `ObjTag#isValid` now returns false when the id wasn't found in the registry, as documented. Missing items in `item_components` are now logged and missing ids in `ObjTagValue` lists are skipped

# 3.0.0.0-beta
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
