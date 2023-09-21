# Changelog

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