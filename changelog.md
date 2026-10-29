# 2.5.0.3-alpha
* Fixed item component patches of programmatic providers (e.g. ISO's stack sizes) reading SERVER config values not applied when the world is loaded (only after `/reload`): tags are loaded before the SERVER config, so the patches used its default values or the ones of the previous world. They are now computed again once the SERVER config is loaded
* Item component patches are now computed again when a SERVER config made with ILModConfig is reloaded while the server is running (e.g. the file edited by hand), and synced to the players if changed

# 2.5.0.2-alpha
* SERVER configs can now be edited from the config screen in the main menu (previously "only available while a world is loaded"). The global `config/<modid>-server.toml` file is edited, so worlds with an override in `saves/<world>/serverconfig/` are not affected
* The SERVER config screen now shows at the top if the global config file or the world's own one (in `serverconfig`) is being edited
* Fixed the "Server config reloaded and synced" message being sent twice when saving from the config screen, and being sent in singleplayer ("synced to 0 players")

# 2.5.0.1-alpha
* Fixed JsonFeatures in the SERVER config not reading their jsons when the world is loaded (only after `/reload`), as the data packs are loaded before the SERVER config, when the feature is still disabled

# 2.5.0.0-alpha
* The config type (COMMON / SERVER / CLIENT) is now per feature instead of per module, so the same module can have features in different config files
  * New `@ConfigType(ModConfig.Type)` annotation for features. Without it the feature uses the config type of its module as before, so existing mods work without changes
  * A module appears in each config file it has features of, with its own section and "Enable" toggle containing only the features of that file. Each toggle enables / disables only the features in its own file, e.g. the client can't disable COMMON or SERVER features. The module's description, "can be disabled" and "enabled by default" apply to all its files
  * New `ILModConfig(modId, type, modEventBus, classLoader)` constructor, that loads the features of the given type of the modules already registered by the mod
  * New `Module.Builder.create(id, name, defaultConfigType, modEventBus)`: modules no longer need a config builder, so they can be created once before all the ILModConfigs. The overload with the builder is deprecated, as is `Module#setConfigBuilder`
  * Modules without features are no longer added to the config ("Enable" toggle included) and a warning is logged
  * CLIENT features are skipped on dedicated servers per feature, also when the mod doesn't create the CLIENT config there
  * A warning is logged for features with a config type for which the mod didn't create an ILModConfig, and for features not loaded because their module was registered after the ILModConfig of their config type was created

# 2.4.37.0
* Thrown tridents damage is now calculated from the attack damage modifiers of the item thrown (moved from Insane's Survival Overhaul), instead of a fixed 8. Vanilla tridents still deal 8, but changes to the trident attack damage (data packs, mods) now apply when thrown too
* Fixed `MCUtils.hurtIgnoreInvulnerability` leaving the entity's last hurt damage set to the damage just dealt, so during the remaining invulnerability frames hits stronger than that damage (instead of stronger than the previous hit) went through

# 2.4.36.1
* Fixed mods with multiple configs (e.g. a SERVER and a CLIENT one) logging "No module found with ID ..." warnings for features of the configs created later, even if everything was loaded correctly
  * A `module` in `@LoadFeature` that no config registers (e.g. a typo) is now reported once all the mods are loaded, unless the feature's `requiresMods` are missing
  * A `@LoadFeature` without `module` in a mod with multiple modules is now always reported, also when it was loaded in the only module existing at that time
* `Feature.isEnabled(Class)` crashes again (now with a clear message) when the feature failed to load, instead of silently returning false since 2.4.36.0. It still returns false for features not loaded because their `requiresMods` are missing or for CLIENT features on dedicated servers

# 2.4.36.0
* Item Components' "Stack Limit" config option has been replaced by "Increase Stack Limit" (true by default), which raises the vanilla limit of 99 to 1,073,741,823. Requires a Minecraft restart
  * Previously the configured value was ignored by the item and component codecs, which always used 9999 since they are built before the config is loaded
  * Item counts over 999 now show as 1k, 1m, 1b
  * The limit is half the max integer value, as vanilla sums the counts of two stacks when merging them (e.g. shift-click), which would overflow and delete the items
* Fixed `insanelib:drop_multiplier` loot modifier deleting unstackable items (e.g. tools and armor) matching the item/tag from the loot when `ignore_unstackable` is true (the default). They are now left untouched as intended
* Fixed `insanelib:loot_purger` loot modifier
  * With `start_range` > 0 the start range was subtracted twice, purging all the loot right after `start_range` and reaching 0 only at `start_range + end_range` instead of `end_range`
  * With `apply_to_damageable`, items generated beyond `end_range` were repaired (even fully). Beyond `end_range` the loot is now left untouched
  * Fixed a division by zero when `end_range` is equal to `start_range`
  * Fixed loot generated more than ~46000 blocks from spawn being treated as if it was at spawn
* Fixed item stacks keeping the components of an `item_components` definition after a `/reload` (or a server change) removed it, until the stack was reloaded from disk
* Fixed clients keeping the `item_components` patches of the previous server when joining a server without InsaneLib
* Fixed possible issues in singleplayer caused by the client and the integrated server modifying `item_components` patches at the same time
* Fixed Player Attributes modifiers being lost after a `/reload`
* Fixed creative tabs being rebuilt from the integrated server thread on `/reload` in singleplayer
* Better Falling Blocks
  * Fixed falling blocks that can't be placed never disappearing when `doEntityDrops` is false (or the falling block doesn't drop)
  * Fixed falling blocks getting stuck forever on top of insta-break blocks with a collision (e.g. slime blocks) when breaking insta-break blocks is enabled. They now land on them
* Fixed an invalid `loot_pool_injections` entry breaking the loot table loading. It's now logged and skipped
* `insanelib:replace_loot`
  * With `amount_to_replace`, the remaining items no longer lose their components (enchantments, damage, etc.)
  * Empty `chances` or `multipliers` are now reported as an error when loading instead of crashing when generating loot
* Item knockback multiplier (`insanelib:knockback_multiplier`) is no longer applied to knockbacks unrelated to the attack (e.g. an explosion after being hit)
* Time Stop No Player Online
  * Disabling the feature while time is stopped now makes time advance again without restarting
  * Fixed the functions not being updated after a `/reload`
* The order of features and modules in the config files is now always the same
* An invalid id in a config (e.g. uppercase letters) is now logged and ignored (or the default value is used) instead of crashing

## Technical
* Fixed clients being disconnected when a synced JsonConfig is longer than 32767 characters
* JsonFeature: fixed reloading a JsonConfig removing all the entries read that are equal to the old ones (e.g. records). An empty JsonConfig file is now reported as a parsing error and keeps the old entries
* JsonFeature: `JsonConfig#onLoad` on the client is now called after the config has been synced for configs synced to clients. When joining a server it was called before receiving the server's values
* JsonConfig sync no longer re-reads the json in singleplayer / LAN host, as the lists are shared with the integrated server
* `ItemComponentsReloadListener.PATCHED_COMPONENTS` and `SYNCED_PATCHES` are now immutable maps replaced as a whole instead of being modified. Added `ItemComponentsReloadListener#getUnpatchedComponents`
* Fixed `MCUtils#addAttributeModifierToItemStack` never adding the modifier
* Fixed `DisenchantModifier` and `LootPurgerModifier` created through their `Builder` crashing (blacklist tags left `null`)
* `AttributeModifierOperationSerializer` now serializes the operation as a string (e.g. `"add_value"`), the same format it deserializes, instead of an object
* `ModNBTData#get(ItemStack, ...)` now returns the type default (0/false) like the other variants instead of `null` when the stack has no custom data
* Fixed `ModNBTData#get` crashing with `UUID.class` when the key is missing. It now returns `null` as documented
* `ModNBTData#remove` no longer creates missing compounds and removes the ones left empty. Removing the last mod data from an item stack now removes the `custom_data` component, so it stacks again with stacks without it
* `Feature.isEnabled(Class)` now returns false instead of crashing for features that are not loaded
* `InsaneLib.ONE_DECIMAL_FORMATTER` is now also available on dedicated servers
* `CurrentAttacker#resolve` now falls back to `getLastHurtByMob()` only if the victim was hurt by it in the current tick
* `ILNearestAttackableTargetGoal`'s default target chance now goes through `reducedTickDelay` like vanilla's (10 -> 5)
* Fixed `SerializableAttributeModifier#fromNetwork` reading the wrong id in the error message for an unknown attribute
* `TimeStopNoPlayerOnline.STOP`, `TC` and `SEASONS` are now the functions' ids
* Added `SnapshotList`, a thread-safe list replaced as a whole. JsonConfigs using it as their list are now updated at once on reload, so in singleplayer the client never sees them empty or gets a `ConcurrentModificationException` while the integrated server reloads them. Lists of JsonConfigs read on the client should use it
* `ObjTag#isValid` now returns false when the id wasn't found in the registry, as documented. Missing items in `item_components` are now logged and missing ids in `ObjTagValue` lists are skipped

# 2.4.35.0
* Mods using `ILModConfig` (InsaneLib included) now automatically get a config screen (NeoForge's `ConfigurationScreen`) in the Mods list (basically all my mods), unless they register their own. Entries without a translation show their config name instead of the raw translation key
* Better Falling Blocks can now be disabled
* Fixed falling blocks not going through portals when Better Falling Blocks is enabled
* Moved "Fix Dupe Exploit" from Better Falling Blocks to Fixes ("Fix Falling Block Dupe"). It now also works when Better Falling Blocks is disabled (vanilla falling blocks going through an End portal)

# 2.4.34.0
## Technical
* `ILModConfig` can now be used with `ModConfig.Type.SERVER` configs
  * Fixed `Cannot get config value before config is loaded` crash when a SERVER config gets unloaded (server stopping, leaving a singleplayer world). `ModConfigEvent.Unloading` is now ignored and `@Config` fields keep the last values read
  * `ConfigOption#set` (and `Blacklist`, `MinMaxConfig`, `DifficultyBasedConfig`, `ObjTag` options) and `Feature#setEnabledConfig` now do nothing and log a warning if the config is not loaded instead of throwing
  * When the server reloads a SERVER config because its file changed, the config is now synced again to connected clients (NeoForge syncs it only on login) and operators get a chat message
* Added `ConfigUtils#isLoaded` and `ConfigUtils#canSet`

# 2.4.33.2
* Fixed loot pool injections (`loot_pool_injections`) not being applied when the world is loaded or on dedicated servers (they only worked after a `/reload`, using the injections of the previous load)
* Fixed `merge_components` in `item_components` re-adding a component removed by a previous definition for the same item (e.g. one targeting the item and one targeting a tag containing it). A removed component now counts as absent: the merge json alone becomes the new value, and if it's not a valid component the removal is kept and an error is logged
* Fixed a `merge_components` that fails to decode also undoing a previous removal of that component
* Fixed `item_components` patches being lost by clients on dedicated servers / LAN after a `/reload`
* Fixed `insanelib:enchant_with_treasure` and `insanelib:enchant_randomly_weightless` loot functions not enchanting books

## Technical
* `PoolInjectionReloadListener` is now `PoolInjectionLoader` and iss no longer a reload listener: injections are loaded right before loot tables via `ReloadableServerRegistriesMixin`
* `item_components` patches are now computed only on the server (`TagsUpdatedEvent` with `SERVER_DATA_LOAD`); `ItemComponentsSyncMessage` is now always sent, even when there are no patches

# 2.4.33.1
* Client modules are no longer loaded on server side

# 2.4.33.0
* Removed Sound Overrides feature. Seemed a smart thing but to make it work properly would require too much work.

# 2.4.32.0
* Added Sound Overrides feature, allowing to override fuse and explosion sounds via NBT. Fuse sound can be overridden by setting the NBT tag 'NeoForgeData.insanelib.sound_overrides.fuse_sound' on the entity. Explosion sound can be overridden by setting the NBT tag 'NeoForgeData.insanelib.sound_overrides.explosion_sound' on the entity.

# 2.4.31.0
* Added missing lang for the `insanelib:mob_detection_range` and `insanelib:push_resistance` attributes
* Fixed a vanilla bug that made experience from the Grindstone always pop out from the top of the block instead of the side it's actually attached to (floor, ceiling, or the wall it's facing)

## Technical
* Fixed ModNBTData method checking for persisted data named `contains` instead of `containsPersisted`
* Added `MCUtils#getGrindstoneOutputPos(Level, BlockPos)`, returning the position facing the side a grindstone is attached to

# 2.4.30.0
* Added `insanelib:recipe_removal`
  * New item tag removing any recipe which output matches the items in the tag
* `insanelib:creative_removal` 
  * No longer requires a world restart to take effect after being updated: it's now reapplied automatically on `/reload` (and on any other tag reload)
  * Better matches items (e.g. Storage Drawers' Detached Drawer having custom components)
* Fixed `insanelib:has_hidden_tooltip` showing the same tooltip as `insanelib:has_tooltip` instead of its own `.tooltip.hidden` translation key; items can now be in both tags to get both tooltips
* Fixed `Item#getDefaultMaxStackSize()` ignoring stack size patches applied by the `ItemComponents` feature: it read the item's raw components field instead of going through the patched `components()`, so mods computing capacity from an item prototype without an `ItemStack` (e.g. Storage Drawers' drawer capacity) still saw the vanilla max stack size

# 2.4.29.0
* Added `insanelib:no_invincibility_frames` damage type tag: damage sources whose damage type is in this tag will not grant invincibility frames (hurt cooldown) to the entity they damage. Fires the new `NoInvincibilityFramesEvent` when this happens

# 2.4.28.0
* Added Creative Removal (moved from Insane Survival Overhaul): items in the `insanelib:creative_removal` item tag are removed from creative mode tabs. Updating this tag requires a world restart. 

# 2.4.27.2
* Fixed missing better falling blocks default blacklist

# 2.4.27.1
* Fixed `insanelib:mob_detection_range` attribute modifiers showing as a flat value (e.g. `+0.4`) instead of a percentage (`+40%`) in item tooltips

# 2.4.27.0
* Added `ItemTooltips` base feature (moved from Insane Survival Overhaul): items in the `insanelib:has_tooltip` item tag get a tooltip matching their vanilla name + `.tooltip` (e.g. `item.minecraft.arrow.tooltip`); items in the new `insanelib:has_hidden_tooltip` tag get the same, but only shown while holding SHIFT

# 2.4.26.0
* Added `insanelib:mob_detection_range` attribute (-Double.MAX_VALUE~1, default 0) to living entities, reducing (negative values) or increasing (positive values) in percentage how far other entities can detect/see them

# 2.4.25.0
* Added `Disable shrink in stack count` config option to `ItemComponents` feature

# 2.4.23.1
* Fixed a rare `ConcurrentModificationException` in `JsonFeature`-based configs synced to client: `loadAndReadJson` (network sync) and `JsonConfig#loadAndReadFile` (file/reload) no longer `clear()` the list before repopulating it, which could race with another thread iterating the same list (e.g. singleplayer, where the client-side sync handler and the server tick thread share the same static list). They now add the new entries and remove the old ones instead, so the list is never left in a transient empty state

# 2.4.23.0
* Added `InCombat` base feature (moved from IguanaTweaksReborn): tracks the last time an entity dealt or took damage via a data key, so other mods can query `InCombat.isInCombat(livingEntity, seconds)`

# 2.4.22.0
* Added loot pool injections (`data/<namespace>/loot_pool_injections/*.json`): injects an item entry into an existing, named pool of a target loot table, so it competes for selection using the pool's own weighted roll alongside its original entries. Pools are auto-named by NeoForge (`main` if the loot table has a single pool, `pool0`/`pool1`/... otherwise) if they don't have names specified in the loot table (vanilla loot tables don't have names, so follow the previously mentioned pattern)
  ```json
  {
    "loot_table": "minecraft:chests/simple_dungeon",
    "pool": "main",
    "entry": {
      "type": "minecraft:item",
      "name": "minecraft:heart_of_the_sea",
      "weight": 5,
      "conditions": [{ "condition": "minecraft:random_chance", "chance": 0.05 }]
    }
  }
  ```
* Removed `insanelib:inject_loot_table` — NeoForge's own `neoforge:add_table` does the same thing (and, unlike this one, also splits the result across stacks bigger than the max stack size). Same `conditions`, same loot table value, just rename the `loot_table` field to `table`

# 2.4.21.3
* Fixed `insanelib:knockback_multiplier` (and any other `LivingKnockBackEvent` listener resolving the attacker via `getLastHurtByMob()`) picking the wrong, or no, attacker on sweep attack hits, since vanilla applies sweep knockback before the `hurt` call that updates `getLastHurtByMob()` for that hit
  * Added `CurrentAttacker` util, tracking the live attacker for the duration of each knockback call in `Player#attack`

# 2.4.21.2
* Bump for local maven publish

# 2.4.21.1
* Fixed items losing their real data components (and gaining `Items.AIR`'s) when they transiently pass through an empty (count 0) `ItemStack` state, such as during `Inventory#add` on a previously empty slot. This most visibly broke music discs, which would silently lose `jukebox_playable` and become unusable in jukeboxes

# 2.4.21.0
* `stackLimit` config option (Item Components) now actually works, allowing item stacks bigger than vanilla's 99 limit
  * Added an `alwaysShrinkStackCount` config option to shrink the item count text when it's bigger than 99, or always

# 2.4.20.2
* Fixed tool durability loss being silently reverted when the tool has a patched `max_damage` (e.g. hoes tilling, axes stripping), caused by `ItemStack#getComponents` handing out a new object identity on every call

# 2.4.20.1
* Fixed item components not being applied to connected clients

# 2.4.20.0
* Added a new fix for https://bugs.mojang.com/browse/MC/issues/MC-145114
* Renamed 'Fixes feature' to 'Fixes'
* Renamed 'Tags feature' to 'Nbt tags'

# 2.4.19.0
* Added `no_ammo_consumption` NBT Tag, allowing for infinite crossbow offhand ammo
  * Also updated the wiki: https://github.com/Insane96/InsaneLib/wiki/%5B1.21.1%5D-NBT-Data
  * Also adds a config option to automatically apply the tag to Pillagers

# 2.4.18.3
* Fixed some items modified via Item Components not stacking

# 2.4.18.2
* Crash fix

# 2.4.18.1
## Technical
* Optimized `ModNBTData`

# 2.4.18.0
## Technical
* Added `ObjTag#getAllObjects`

# 2.4.17.2
* Startup crash fix

# 2.4.17.1
* Allow picking up items from anvil with 0 cost and don't render cost label when 0

# 2.4.17.0
## Technical
* Added `CreativeTabsUtils` with methods to add items to a creative tab before or after another item, or remove an item from a creative tab

# 2.4.16.0
* Added `ILRangedAttribute`
  * Same as a `RangedAttribute`, but the `descriptionId` is calculated from the attribute id and can define a `baseId`.

# 2.4.15.1
* Fixed Item components' programmatic providers overwriting each-other

# 2.4.15.0
* Fixed a NeoForge bug that caused `global_loot_modifiers.json` to not respect the order of the loot modifiers

# 2.4.14.1
* Added copper equipment item tags and fixed wooden and golden being wood and gold

# 2.4.14.0
* Added GetMaxDamageEvent to modify item's max damage
* Added Item Tags for equipment by material

# 2.4.13.1
* Fixed Integrated resource packs not being enabled by default

# 2.4.13.0-beta
* Added `block_broken` advancement trigger
* `insanelib:enchant_with_treasure`
  * Renamed `allow_curses` and `allow_treasure` to `ignore_curses` and `ignore_treasures`

# 2.4.12.2-beta
* Crash fix

# 2.4.12.1-beta
* Crash fix

# 2.4.12.0-beta
## Technical
* Added `SerializableMobEffectInstance`

# 2.4.11.0-beta
* Added `insanelib:enchantability` item component

# 2.4.10.0-beta
* Added `merge_components` array for item_components
  * Arrays are concatenated, objects are recursively merged, and primitives are overridden.  
    This is useful for adding entries to a list component (e.g. appending attribute modifiers) without discarding the item's existing values

# 2.4.9.3-beta
* Optimized ModNBTData path parsing to avoid regex overhead on every NBT access

# 2.4.9.2-beta
## Technical
* Allow `ObjTag` to use dynamic registries

# 2.4.9.1-beta
## Technical
* Added `JsonFeature#loadAndReadJson` overloads for specifying the registry

# 2.4.9.0-beta
## Technical
* Added back json utilities (`ILGsonHelper` and validators)

# 2.4.8.0-beta
## Technical
* Added loot modifiers:
  * `insanelib:replace_loot` — replaces items in a loot table with another item, optionally copying components (durability, enchantments) and scaling the count
    ```json
    {
      "type": "insanelib:replace_loot",
      "conditions": [...],
      "original_item": "minecraft:iron_sword",
      "replacement_item": "minecraft:diamond_sword",
      "copy_components": true,
      "count_multiplier": 1.0
    }
    ```
  * `insanelib:inject_loot_table` — injects the contents of another loot table into the current one
    ```json
    {
      "type": "insanelib:inject_loot_table",
      "conditions": [...],
      "loot_table": "minecraft:chests/simple_dungeon"
    }
    ```
  * `insanelib:drop_multiplier` — multiplies the count of matching items in loot
    ```json
    {
      "type": "insanelib:drop_multiplier",
      "conditions": [...],
      "item": "minecraft:wheat",
      "multiplier": 2.0
    }
    ```
  * `insanelib:loot_purger` — removes or damages items based on distance from world spawn; useful for progressive loot difficulty
    ```json
    {
      "type": "insanelib:loot_purger",
      "conditions": [...],
      "end_range": 5000,
      "start_range": 0,
      "multiplier_at_start": 0.0,
      "apply_to_damageable": false,
      "blacklisted_items_tag": "insanelib:loot_purger_blacklist",
      "blacklisted_entity_type_tag": "insanelib:loot_purger_entity_blacklist"
    }
    ```
    Items are progressively purged the closer to spawn the loot generates. At `start_range` the survival chance is `multiplier_at_start`, at `end_range` it is 1. `apply_to_damageable` damages items proportionally instead of removing them.
  * `insanelib:disenchant` — removes enchantments from all items; enchanted books become plain books
    ```json
    {
      "type": "insanelib:disenchant",
      "conditions": [...],
      "blacklisted_items_tag": "insanelib:disenchant_blacklist"
    }
    ```
* Added loot functions:
  * `insanelib:enchant_randomly_weightless` — enchants an item with a given number of random enchantments, each chosen with equal probability (no weight). Supports books.
    ```json
    {
      "function": "insanelib:enchant_randomly_weightless",
      "conditions": [...],
      "count": 2,
      "max_lvl": false,
      "treasure": false
    }
    ```
    `count`: number of enchantments to apply (supports number providers). `max_lvl`: always apply the maximum level. `treasure`: allow treasure enchantments (tag `minecraft:treasure`).
  * `insanelib:enchant_with_treasure` — applies a single random treasure enchantment to the item. Supports books.
    ```json
    {
      "function": "insanelib:enchant_with_treasure",
      "conditions": [...],
      "ignore_curses": false,
      "ignore_treasures": false
    }
    ```
    `ignore_curses`: exclude curse enchantments (tag `minecraft:curse`). `ignore_treasures`: exclude non-curse treasure enchantments.
* Added loot conditions:
  * `insanelib:block_tag_match` — passes if the broken block is in the given tag (returns true if no block state is in context)
    ```json
    { "condition": "insanelib:block_tag_match", "block_tag": "minecraft:logs" }
    ```
  * `insanelib:killer_has_advancement` — passes if the killing player has completed the given advancement
    ```json
    { "condition": "insanelib:killer_has_advancement", "advancement": "minecraft:story/mine_diamond" }
    ```
  * `insanelib:non_player_arised_drop` — passes if the drop was not caused by a player, explosion, or tool (i.e. natural mob death without a player killer)
    ```json
    { "condition": "insanelib:non_player_arised_drop" }
    ```

# 2.4.7.1-beta
## Technical
* Fixed self() methods not being @Unique

# 2.4.7.0-beta
## Technical
* Added `ItemComponentsReloadListener.PROGRAMMATIC_PROVIDERS` to change stacks programmatically

# 2.4.6.2-beta
## Technical
* Attached sources in the .jar

# 2.4.6.1-beta
## Technical
* Changed MCUtils.computeFoodFormula variables
  * Removed effectiveness
  * Renamed hunger to nutrition

# 2.4.6.0-beta
## Technical
* Added back `InsaneLib.ONE_DECIMAL_FORMATTER`

# 2.4.5.0-beta
## Technical
* Added `MCUtils.computeFoodFormula` using EvalEx
* Added back all the events

# 2.4.4.0-beta
## Technical
* Added back `feature_enabled` neoforge condition and loot condition

# 2.4.3.0-beta
## Technical
* Added back `PlayerUseItemMovSpeedEvent` and `PlayerSprintEvent`

# 2.4.2.0-beta
* Moved Attack Speed Based Invincibility feature to ISO

# 2.4.1.1-beta
## Technical
* Fixed concurrent modification exception

# 2.4.1.0-beta
## Technical
* Ported back `ClientUtils`

# 2.4.0.1-beta
* Attack invincibility frames are now reduced by 10%
* Fixed invincibility frames not shown on entities if damage was not from an entity or from non-weapon

# 2.4.0.0-beta
* Added insanelib:knockback_multiplier data component
  * 0~1 that can reduce the knockback of an item

# 2.3.1.1-beta
* Fixed invincibility feature erroring when installed server side only

# 2.3.1.0-beta
* Added config option to limit invincibility frames to only faster attack speeds

# 2.3.0.0-beta
* Added Attack Speed Based Invincibility feature
  * Makes attack change invincibility frames based off attack speed, so faster attacks will give less invincibility frames and vice versa
  * If installed on the client, they will actually see the red invincibility frames correctly based off attack speed

# 2.2.2.0-beta
* Added back `/insanelib` command, but, more importantly, added a new subcommand get_data_components
  * With `/insanelib get_data_components <item>` you can get a list of all the data components that are currently applied to the item

# 2.2.1.0-beta
* Renamed Item Definitions to Item components (the data pack folder has also changed to `item_components`) 
  * Added `remove_components`, a list of components to remove from the item
  * Added `priority`, integer value. When multiple json target the same item, components are merged — higher priority wins per component type.
* A higher-priority remove overrides a lower-priority set, and vice versa.

# 2.2.0.0-beta
* Added Item Definitions feature
  * Use data packs to change items' data components  
  E.g. in `data/<namespace>/item_definitions/strong_diamond_sword.json` will make diamond swords have 50 Attack Damage and 2000 durability
  ```json
  {
      "item": "minecraft:diamond_sword",
      "components": {
          "minecraft:max_damage": 2000,
          "minecraft:attribute_modifiers": {
              "modifiers": [
                  {
                      "type": "minecraft:generic.attack_damage",
                      "id": "minecraft:attack_damage",
                      "amount": 50.0,
                      "operation": "add_value",
                      "slot": "mainhand"
                  }
              ]
          }
      }
  }
  ```

# 2.1.3.0-beta
* Ported Push Resistance attribute from Enhanced AI

# 2.1.2.5-beta
* Crash fix when saving a parsed effect instance

# 2.1.2.4-beta
* Fix Network messages again

# 2.1.2.3-beta
* Fix Network messages being sent to clients with no mod installed

# 2.1.2.2-beta
* Fix ObjTag resolving unknown registry entries as fallback objects

# 2.1.2.1-beta
* Fixed missing `ObjTagValue.LIST_TYPE`

# 2.1.2.0-beta
* Added back `IdTagValue` as `ObjTagValue`
* Added `ObjTag#asHolder`

# 2.1.1.0-beta
* Added `MCUtils.createPotionStackFromEffectInstances`

# 2.1.0.1-beta
* Fixed startup crash

# 2.1.0.0-beta
* Modules now require Resource Location as identifier
* You can now use `ILModConfig` to prevent having to create a config class each mod
  * In the mod class
    ```java
      public static ILModConfig CONFIG;
    
      public YourMod(IEventBus modEventBus, ModContainer modContainer) {
        CONFIG = new ILModConfig(MOD_ID, ModConfig.Type.COMMON, modEventBus,
                Modules::init, InsaneLib.class.getClassLoader());
        modContainer.registerConfig(ModConfig.Type.COMMON, CONFIG.spec, MOD_ID + "/common.toml");
      }
    ```
  * You can also create a single module mod via `ILModConfig` constructor
    ```java
      public static ILModConfig CONFIG;
    
      public YourMod(IEventBus modEventBus, ModContainer modContainer) {
        CONFIG = new ILModConfig(location("main"), "Main", ModConfig.Type.COMMON, modEventBus,
                Modules::init, InsaneLib.class.getClassLoader());
        modContainer.registerConfig(ModConfig.Type.COMMON, CONFIG.spec, MOD_ID + "/common.toml");
      }
    ```
    Features in single module mods can now omit the module parameter in `@LoadFeature`
* Ported more MCUtils functions

# 2.0.4.0-beta
* Ported Better Falling Blocks

# 2.0.3.0-alpha
* Added Fix Swimmers Swimming Attribute, making Drowned and Fishes use the neoforge swimming speed attribute instead of the vanilla movement speed

# 2.0.2.1-alpha
* Player attributes are now empty by default

# 2.0.2.0-alpha
* Added Player Attributes feature
  * Directly from Insane's Survival Overhaul, change players attributes with a json in the config folder.
  * By default, it will slightly reduce movement speed and block reach
* Added back JsonFeature (needed for Player Attributes)

# 2.0.1.0-alpha
* Added back MessageCreeperDataSync 
  * Creeper data is now automatically synced client-side (if the mod is installed on the client).

# 2.0.0.5-alpha
* Fixed "Fix Air Speed.Sprinting Jump Slowdown" being ignored

# 2.0.0.4-alpha
Port to 1.21.1

This version contains most of the player features (missing Better Falling Blocks) + everything needed to make MPR work.

* Removed `spawn_type` tag, neoforge already does that with `neoforge:spawn_type` NBT Tag
* Follow range fix now applies to any entity that uses vanilla NearestAttackableTargetGoal and will update everytime the mob tries to find a new target instead of only on spawn
* Enhanced Fix Air Speed (aka Fix Jump Movement Factor)