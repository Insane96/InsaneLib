InsaneLib's Changelog

## 1.9.0
* Port to 1.19.4

## 1.8.1
* Added `ConfigOption#getConfigPath`
* Added `ILItemTier`
* Added `Feature#getConfigOption` (and renamed `Feature#setConfig` to `Feature#setConfigOption`)
* Fixed nested Blacklist and MinMax configs not popping the path correctly

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