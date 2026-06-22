# Changelog

## 1.23.4.5
* Backported ModNBTData optimizations

## 1.23.4.4
* Optimized ModNBTData path parsing to avoid regex overhead on every NBT access 

## 1.23.4.3
* Scaffolding is no longer affected by Better Falling Blocks

## 1.23.4.2
* Pointed dripstones are no longer affected by Better Falling Blocks

## 1.23.4.1
* Fixed falling blocks not stacking when pass through (e.g. Leaves with Passable Foliage)

## 1.23.4.0
* Added `insanelib:tags.sky_light` and `insanelib:tags.block_light` NBT so commands can know the light level an entity is standing in

## 1.23.3
* Added `insanelib:blacklisted_better_falling_blocks` to disable certain blocks from using the feature
  * Defaults to the dragon egg so you can pick it up

## 1.23.2
* Fixed falling blocks getting stuck on non-full blocks

## 1.23.1
* Various fixes for Better Falling Block
  * Disabling insta-break blocks now works
  * Falling blocks in another non-insta-break block will move to the side instead of getting stuck

## 1.23.0
* Added `/insanelib` command to change player's play time

## 1.22.1
* Fix follow range now only affects vanilla entities by default
  * Use entity type tag `insanelib:fix_follow_range` to add other mobs

## 1.22.0
* Moved `ISOFallingBlockEntity` from Insane's Survival Overhaul to here
  * It's not a new entity, it's a feature that alters vanilla Falling Blocks

## 1.21.21
* Added MCUtils.removeModifier
* Applying max health modifiers with MCUtils.applyModifier no longer fully heals the entity

## 1.21.20
* Fixed client integrated packs not loading with correct type

## 1.21.19
* Added empty constructor to JsonFeature

## 1.21.18
* Enhanced config options for 'Time stop no player online'

## 1.21.17
* Fixed Integrated Client Pack not loading
* Fixed log errors when Time Control and/or Serene Seasons are not present

## 1.21.16
* Scheduled tasks are now cleared when leaving the world

## 1.21.15
* Prevent time ticking when no player online is now disabled by default
* Split weather stop when no player online and added infos about disabling game rules

## 1.21.14
* Time stop with no player online enhanced
  * Added support for weather, Serene Seasons and Time Control

## 1.21.13
* Added loot condition `insanelib:feature_enabled`
  * Same as recipe condition
* Condition now errors if the feature doesn't exist

## 1.21.12
* Fixed backwards compatibility for JsonFeatures

## 1.21.11
* `Module#getFeature` is now case-insensitive

## 1.21.10
* Added a new `insanelib:feature_enabled` condition for recipes and advancements
* Features no longer require the 3 parameter constructor. `Feature#init` can be overridden instead if needed
* Added `Module#getFeature` by name
* Cleaned up Feature and Module creation

## 1.21.9
* Added missing `ModNBTData#putPersisted` and `containsPersisted`

## 1.21.8
* Added int arrays to `ModNBTData`
* Added `ModNBTData#remove`
* Added `ModNBTData` methods to save and retrieve player's persisted data

## 1.21.7
* Fixed yet again Features data tags

## 1.21.6
* Fixed Time stop with no player online data pack
  * Not working
  * Erroring due to Time Control not installed

## 1.21.5
* Again, crash with invalid characters in feature data key

## 1.21.4
* Fixed crash with Features with spaces

## 1.21.3
* Added `EntityModNBTDataSync` to sync NBT data to players
* Network is now fully server sided
* Added missing long type for `ModNBTData`

## 1.21.2
* Ported `IntegratedPack` from Insane Survival Overhaul
* Stolen `handleMissingMappings` from Mantle
* Added a new integrated data pack that also stops weather, season and Time Control's time from ticking

## 1.21.1
* Fixed getList and renamed modDataContains to contains

## 1.21.0
* Added a new NBT Tag utils method to store and retrieve NBT data from entities and stacks
  * The data is saved in mod's id compound and feature compound  
    E.g. `ForgeData:{insanelib: {tags: {spawn_type: 12b, experience_multiplier: 1.5}}}`   
    (was `ForgeData:{"insanelib:spawn_type": 12b, "insanelib:experience_multiplier": 1.5}`}`)

## 1.20.4
* Fixed 'Prevent Time Ticking If No Players Online' not applying on server start

## 1.20.3
* Added 'Prevent Time Ticking If No Players Online'
  * This makes it so the game time won't advance if there are no players online
    This fixes servers having huge amount of days accumulated even if no players are online

## 1.20.2
* Changed spawn type tag to be any type from `MobSpawnType`
  * It's now a byte `insanelib:spawn_type`
  ```java 
  public enum MobSpawnType {
    NATURAL,
    CHUNK_GENERATION,
    SPAWNER,
    STRUCTURE,
    BREEDING,
    MOB_SUMMONED,
    JOCKEY,
    EVENT,
    CONVERSION,
    REINFORCEMENT,
    TRIGGERED,
    BUCKET,
    SPAWN_EGG,
    COMMAND,
    DISPENSER,
    PATROL
  }
  ```
* Removed `ILStrings` and moved tags to `TagsFeature`

## 1.20.1
* Finalized running server side only

## 1.20.0
* Area effect cloud has been moved to a standalone mod
  * So the mod can now run server side only. Mostly for Mobs Properties Randomness
* Mod config name has been changed from "insanelib-common.toml" to "insanelib.toml"

## 1.19.0
* `@Label` is now deprecated, name and descriptions have been added to `@Config` and `@LoadFeature`
  * `@LoadFeature#name`, if omitted, fill be taken from the class name, with "Feature" removed
* Omitted config / feature names now accept $ as a .
  * So experience$dropped will be parsed in the config as "Experience" main config options and "Dropped" sub-config option
* Fixed `ONE_DECIMAL_FORMATTER` using machine locale

## 1.18.3
* Updated Forge

## 1.18.2
* Config options can now omit the `@Label` annotation
  * name is capitalized from the field name
  * description is blank by default
  * If `@Label` is present, name can be omitted and will same as if the annotation was omitted

## 1.18.1
* Fixed `MinMax` `getRandBetween` and `getIntRandBetween` excluding the maximum

## 1.18.0
* Added `PlayerUseItemSpeedModifierEvent`

## 1.17.0
* Added `Feature#postReadConfig`

## 1.16.1
* Added `SerializableAttributeModifier` constructor without slot
* Added `MCUtils.getFoodEffectiveness`, `MCUtils.getFoodSaturationRestored`, `MCUtils.syncedRandom`

## 1.16.0
* Added Json Helpers

## 1.15.1
* Added getOrCreatePersistedData

## 1.15.0
* Added a new parameter to features which will prevent them from loading if the mods specified are not present

## 1.14.0
* Added `SerializableAttributeModifier`

## 1.13.5
* Added `ClientUtils` from ITR

## 1.13.4
* Revert 'Features are no longer enabled on config reload if they can't be disabled'

## 1.13.3
* Removed IEnchantmentTooltip
* Features are no longer enabled on config reload if they can't be disabled

## 1.13.2
* Overridden `IdTagMatcher`, `IdTagRange` and `IdTagValue` `.toString()`
* Added `InsaneLib.ONE_DECIMAL_FORMATTER`

## 1.13.1
* Added `Feature#get` to get a feature from a class
* Replaced `Feature#enable` and `Feature#disable` with `Feature#setEnabled`

## 1.13.0
* Added `IEnchantmentTooltip`

## 1.12.1
* Forgot `SyncType` constructor

## 1.12.0
* Added `JsonFeature` that can write/read and sync jsons
* Added `matchesBlock(BlockState)` and `matchesItem(ItemStack)`

## 1.11.1
* Fixed some `IdTagRange` methods missing

## 1.11.0
* Added many new Events
  * `PlayerSprintEvent`: can be canceled to prevent the player from sprinting
  * `BlockBurntEvent`: triggered when a block burns
  * `PlayerExhaustionEvent`: can be used to change the amount of exhaustion given to the player
  * `CakeEatEvent`: triggered when a cake is eaten
  * `FallingBlockLandEvent`: triggered when a falling block lands
  * `HurtItemStackEvent`: Can be used to change the amount of damage an `ItemStack` takes
  * `AddEatEffectEvent`: can be used to cancel applying on eat effects from foods
* Added `IdTagValue` that contains IdTagMatcher and a value
* Added `IdTagRange` that contains IdTagMatcher and a min and max
* Added `TwinIdTagMatcher` that contains IdTagMatcher and a min and max

## 1.10.2
* Added `InjectLootTableModifier` ("type": "insanelib:inject_loot_table")

## 1.10.1
* Fixed missing 1.9.1 and 1.9.2 changes

## 1.10.0
* Port to 1.20

## 1.9.2
* Added a way to enable/disable features via code (`Feature#disable`, `Feature#enable`)

## 1.9.1
* Added Difficulty Config
  * Holds easy normal and hard values

## 1.9.0
* Port to 1.19.4

## 1.8.1
* Added `ConfigOption#getConfigPath`
* Added `ILItemTier`
* Added `ILMobEffect`
* Added `Feature#getConfigOption` (and renamed `Feature#setConfig` to `Feature#setConfigOption`)
* Fixed nested Blacklist and MinMax configs not popping the path correctly
* Fixed `WeightedRandom` still using `Random` instead of `RandomSource
* Fixed 'Fix Follow Range' not working
* Fixed `@Config` annotation having the wrong min value by default

## 1.8.0
* Bump version for Minecraft 1.19.3. 1.8.x+ is now 1.19.3 and 1.7.x it's 1.19.2

## 1.7.4
* Added Creeper Data Sync network message

## 1.7.3
* Updated to 1.19.3

## 1.7.2
* Added a way to set config values
* IdTagMatcher can now be deserialized as a simple string

## 1.7.1
* Allow different config types in modules
* Added more constructors to `Blacklist`

## 1.7.0
* Overhauled Modules, Features and Config Options registration

## 1.6.2
* Fixed `MathHelper.getAmountWithDecimalChance` not using RandomSource

## 1.6.1
* Fixed getAllItems/Blocks/Entities/Fluids returning air when the entry was not in the registry

## 1.6.0
* Ported to 1.19.2