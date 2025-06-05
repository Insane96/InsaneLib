# Changelog

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