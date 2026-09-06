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