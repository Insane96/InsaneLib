# ObjTag Branch - Change Description

This document extensively describes all the changes made in the `ObjTag` branch compared to `1.20.1`.

---

## New file: `ObjTag.java`
**Path:** `src/main/java/insane96mcp/insanelib/data/ObjTag.java` (197 lines)

This is the central class of the branch. `ObjTag<T>` is a generic structure that represents a reference to either a **registry object** (e.g. a block, an item, an entity type) or a **tag** (a group of objects defined via datapack).

### Class structure
- **Fields:** `obj` (the direct object, nullable), `tag` (the TagKey, nullable), `registry` (the Minecraft registry it belongs to).
- **Private constructors:** two constructors, one for direct object and one for tag. Creation is done through static factory methods.

### Factory methods
- `objOf(T obj, Registry<T>)` — creates an ObjTag from a direct object.
- `tagOf(TagKey<T>, Registry<T>)` — creates an ObjTag from a tag.
- `of(String id, ResourceKey<Registry<T>>)` — parses from string: if it starts with `#` it's interpreted as a tag, otherwise as an object. E.g. `"minecraft:stone"` → object, `"#minecraft:cherry_logs"` → tag.
- `objOf(ResourceLocation, ResourceKey<Registry<T>>)` and `tagOf(ResourceLocation, ResourceKey<Registry<T>>)` — variants that resolve the registry from `BuiltInRegistries`.

### `matches(T obj)` method
Checks whether an object matches the ObjTag:
- If `this.obj` is not null, directly compares with `equals`.
- If `this.tag` is not null, looks up the tag in the registry and checks whether the object is contained in the tag via `HolderSet.Named`.
- If the tag is not found in the registry, logs a debug message and returns `false`.

### JSON Serialization/Deserialization
- `serialize()` — converts the ObjTag to a `JsonPrimitive`: the registry id for objects, `#id` for tags.
- `serializeList(List<ObjTag<T>>)` — serializes a list of ObjTag into a `JsonArray`.
- `deserialize(JsonElement, ResourceKey<Registry<T>>)` — deserializes a JSON element into an ObjTag.
- `deserializeList(JsonElement, ResourceKey<Registry<T>>)` — deserializes a JSON array into a list of ObjTag.
- `deserializeRegistryObject(JsonElement, ResourceKey<Registry<T>>)` — deserializes a registry object directly (without wrapping in ObjTag).

### Inner class `Serializer`
Implements `JsonSerializer<ObjTag<?>>` and `JsonDeserializer<ObjTag<?>>` for the `@JsonAdapter` annotation. Deserialization via annotation throws an exception, forcing explicit use of `ObjTag.deserialize()` (which requires the registry type).

### Inner class `RegistryMappings`
Maps Java classes to the corresponding Minecraft `ResourceKey<Registry<?>>`:
- `Block.class` → `Registries.BLOCK`
- `Item.class` → `Registries.ITEM`
- `EntityType.class` → `Registries.ENTITY_TYPE`
- `Enchantment.class` → `Registries.ENCHANTMENT`
- `Fluid.class` → `Registries.FLUID`

The `getRegistryKey(Class<T>)` method returns the registry key associated with a class.

### Inner class `AdapterFactory<T>`
Implements `TypeAdapterFactory` for Gson. Enables automatic serialization/deserialization of `ObjTag<T>` when registered in a `GsonBuilder`. The adapter internally uses `ObjTag.serialize()` and `ObjTag.deserialize()` with the `registryKey` provided to the constructor.

---

## Renamed: `LogHelper.java` → `ILLogger.java`
**Path:** `src/main/java/insane96mcp/insanelib/util/ILLogger.java`

### Changes
- The class was renamed from `LogHelper` to `ILLogger`.
- The `error`, `warn`, and `info` methods no longer use `String.format(s, args)` but instead pass arguments directly to the SLF4J logger (`InsaneLib.LOGGER.error(s, args)`). This is more efficient because SLF4J avoids string formatting if the log level is not active, and natively supports `{}` placeholders.
- Added the `debug(String s, Object... args)` method which did not exist before, used by the new `ObjTag.matches()`.

---

## Modified: `JsonFeature.java`
**Path:** `src/main/java/insane96mcp/insanelib/base/JsonFeature.java`

### Fields
- `JSON_CONFIGS` was made `private` (previously `public final`), improving encapsulation.

### New method `addJsonConfig(JsonConfig<?>)`
Public method to add JSON configurations to the internal list. Replaces direct access to the list.

### Refactor of inner class `JsonConfig<T>`
The inner class `JsonConfig<T>` was significantly restructured:

**New field:**
- `clazz` (`Class<?>`, nullable) — registry object class, used to automatically register the `ObjTag` `AdapterFactory` in Gson.

**Simplified constructors:**
- Previously there were 4 constructors with different combinations of parameters (`onLoad`, `syncToClient`, `syncType`).
- Now there is a **single constructor** with the essential parameters (`fileName`, `list`, `defaultList`, `listType`).
- Optional parameters are set via **builder methods** with fluent API:
  - `syncToClient(ResourceLocation syncType)` — enables client synchronization.
  - `onLoad(BiConsumer<List<T>, Boolean>)` — sets the load callback.
  - `withRegistryFor(Class<?> innerType)` — specifies the registry class for ObjTag serialization.

**New private method `getGson()`:**
- Centralizes Gson instance creation.
- If `clazz` is set, it automatically registers `ObjTag.AdapterFactory` with the corresponding registry key via `ObjTag.RegistryMappings.getRegistryKey()`.
- Used in both `loadAndReadFile` and `syncData`, replacing inline Gson creation.

### Reference updates
- All references to `LogHelper` were updated to `ILLogger`.
- Added `ObjTag` import.

---

## Modified: `BaseFeature.java`
**Path:** `src/main/java/insane96mcp/insanelib/module/base/BaseFeature.java`

### Inheritance change
- The class now extends `JsonFeature` instead of `Feature`, gaining the ability to handle JSON configurations.

### New fields
- `TEST_BLOCKS_DEFAULT` — default list of `ObjTag<Block>` containing `minecraft:stone`, `minecraft:dirt`, and the tag `#minecraft:cherry_logs`. Serves as an example/test of the ObjTag system.
- `testBlocks` — mutable list of `ObjTag<Block>` populated from the JSON file.
- `BLOCK_LIST_TYPE` — Gson `Type` for `ArrayList<ObjTag<Block>>`, required for generic deserialization.

### Constructor
- Added registration of a `JsonConfig` via `addJsonConfig()`:
  - File: `test_blocks.json`
  - List: `testBlocks`
  - Default: `TEST_BLOCKS_DEFAULT`
  - Uses `.withRegistryFor(Block.class)` to enable ObjTag serialization.

### New method `getModConfigFolder()`
- Required implementation of `JsonFeature.getModConfigFolder()`, returns `InsaneLib.CONFIG_FOLDER` (`"config/insanelib"`).

### New event handler `onSpawn(LivingHurtEvent)`
- Registered with `EventPriority.LOWEST`.
- When an entity is hit by a player in the Overworld, it checks whether the block below the hit entity is in the `testBlocks` list.
- If there is a match, it logs an informational message. This is clearly test/debug code to verify the ObjTag system works correctly.

### New imports
- `ObjTag`, `ILLogger`, `TypeToken`, `Registries`, `Player`, `Level`, `Block`, `LivingHurtEvent`, `EventPriority`.

---

## Modified: `InsaneLib.java`
**Path:** `src/main/java/insane96mcp/insanelib/InsaneLib.java`

### New field
- `CONFIG_FOLDER` — constant `"config/insanelib"`, used by `BaseFeature.getModConfigFolder()` to define the mod's JSON configuration folder.

---

## Modified: `Feature.java`
**Path:** `src/main/java/insane96mcp/insanelib/base/Feature.java`

### Reference updates
- Import changed from `LogHelper` to `ILLogger`.
- Two calls to `LogHelper.warn()` updated to `ILLogger.warn()` in the `setEnabledConfig()` and `setConfigOption()` methods.

---

## Modified: `Module.java`
**Path:** `src/main/java/insane96mcp/insanelib/base/Module.java`

### Reference updates
- Import changed from `LogHelper` to `ILLogger`.
- Four calls updated: one `warn` and three `info` in the feature annotation scanning method.

---

## Modified: `IdTagMatcher.java`
**Path:** `src/main/java/insane96mcp/insanelib/data/IdTagMatcher.java`

### Reference updates
- Import changed from `LogHelper` to `ILLogger`.
- Four calls to `LogHelper.warn()` updated to `ILLogger.warn()` in the `parseLine()` method.

---

## Modified: `JsonFeatureDataReloadListener.java`
**Path:** `src/main/java/insane96mcp/insanelib/data/JsonFeatureDataReloadListener.java`

### Reference updates
- Import changed from `LogHelper` to `ILLogger`.
- One call to `LogHelper.info()` updated to `ILLogger.info()` in the `apply()` method.

---

## Modified: `MCUtils.java`
**Path:** `src/main/java/insane96mcp/insanelib/util/MCUtils.java`

### Reference updates
- Five calls to `LogHelper.warn()` updated to `ILLogger.warn()` in the `parseEffectInstance()` method.

---

## Summary

| Type | File | Description |
|------|------|-------------|
| **New** | `ObjTag.java` | Generic type-safe system for registry object/tag references |
| **Renamed** | `LogHelper.java` → `ILLogger.java` | Rename + SLF4J logging improvement + added `debug()` |
| **Refactor** | `JsonFeature.java` | Encapsulation, builder pattern for JsonConfig, ObjTag integration |
| **Modified** | `BaseFeature.java` | Extends JsonFeature, ObjTag test with blocks |
| **Modified** | `InsaneLib.java` | Added CONFIG_FOLDER constant |
| **Updated** | `Feature.java`, `Module.java`, `IdTagMatcher.java`, `JsonFeatureDataReloadListener.java`, `MCUtils.java` | Renamed LogHelper → ILLogger |