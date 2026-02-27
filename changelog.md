## 2.4.0.0-beta
* Added insanelib:knockback_multiplier data component
  * 0~1 that can reduce the knockback of an item

## 2.3.1.1-beta
* Fixed invincibility feature erroring when installed server side only

## 2.3.1.0-beta
* Added config option to limit invincibility frames to only faster attack speeds

## 2.3.0.0-beta
* Added Attack Speed Based Invincibility feature
  * Makes attack change invincibility frames based off attack speed, so faster attacks will give less invincibility frames and vice versa
  * If installed on the client, they will actually see the red invincibility frames correctly based off attack speed

## 2.2.2.0-beta
* Added back `/insanelib` command, but, more importantly, added a new subcommand get_data_components
  * With `/insanelib get_data_components <item>` you can get a list of all the data components that are currently applied to the item

## 2.2.1.0-beta
* Renamed Item Definitions to Item components (the data pack folder has also changed to `item_components`) 
  * Added `remove_components`, a list of components to remove from the item
  * Added `priority`, integer value. When multiple json target the same item, components are merged — higher priority wins per component type.
* A higher-priority remove overrides a lower-priority set, and vice versa.

## 2.2.0.0-beta
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

## 2.1.3.0-beta
* Ported Push Resistance attribute from Enhanced AI

## 2.1.2.5-beta
* Crash fix when saving a parsed effect instance

## 2.1.2.4-beta
* Fix Network messages again

## 2.1.2.3-beta
* Fix Network messages being sent to clients with no mod installed

## 2.1.2.2-beta
* Fix ObjTag resolving unknown registry entries as fallback objects

## 2.1.2.1-beta
* Fixed missing `ObjTagValue.LIST_TYPE`

## 2.1.2.0-beta
* Added back `IdTagValue` as `ObjTagValue`
* Added `ObjTag#asHolder`

## 2.1.1.0-beta
* Added `MCUtils.createPotionStackFromEffectInstances`

## 2.1.0.1-beta
* Fixed startup crash

## 2.1.0.0-beta
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

## 2.0.4.0-beta
* Ported Better Falling Blocks

## 2.0.3.0-alpha
* Added Fix Swimmers Swimming Attribute, making Drowned and Fishes use the neoforge swimming speed attribute instead of the vanilla movement speed

## 2.0.2.1-alpha
* Player attributes are now empty by default

## 2.0.2.0-alpha
* Added Player Attributes feature
  * Directly from Insane's Survival Overhaul, change players attributes with a json in the config folder.
  * By default, it will slightly reduce movement speed and block reach
* Added back JsonFeature (needed for Player Attributes)

## 2.0.1.0-alpha
* Added back MessageCreeperDataSync 
  * Creeper data is now automatically synced client-side (if the mod is installed on the client).

## 2.0.0.5-alpha
* Fixed "Fix Air Speed.Sprinting Jump Slowdown" being ignored

## 2.0.0.4-alpha
Port to 1.21.1

This version contains most of the player features (missing Better Falling Blocks) + everything needed to make MPR work.

* Removed `spawn_type` tag, neoforge already does that with `neoforge:spawn_type` NBT Tag
* Follow range fix now applies to any entity that uses vanilla NearestAttackableTargetGoal and will update everytime the mob tries to find a new target instead of only on spawn
* Enhanced Fix Air Speed (aka Fix Jump Movement Factor)