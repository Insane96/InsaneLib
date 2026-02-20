# Changelog

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