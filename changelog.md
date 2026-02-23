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
  * You can now create a single module mod via `ILModConfig` constructor
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