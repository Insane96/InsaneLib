# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

InsaneLib is a Minecraft Forge library mod for version 1.20.1 (Minecraft 1.20.1, Forge 47.3.33) used by Insane96MCP's mods (Progressive Bosses, Insane's Survival Overhaul, Mobs Properties Randomness, etc.). It provides a modular configuration system, custom events, mixins, data-driven features, and utility classes.

## Build Commands

```bash
# Build the mod (creates both slim and jarJar versions)
./gradlew build

# Run Minecraft client for testing
./gradlew runClient

# Run Minecraft server for testing
./gradlew runServer

# Run data generation
./gradlew runData

# Clean build artifacts
./gradlew clean

# Create JAR without dependencies (slim)
./gradlew jar

# Create JAR with bundled dependencies
./gradlew jarJar

# Generate IDE project files
./gradlew genIntellijRuns
./gradlew genEclipseRuns
./gradlew genVSCodeRuns
```

## Architecture

### Module & Feature System

The codebase uses a hierarchical **Module → Feature** architecture:

- **Modules** (`insane96mcp.insanelib.base.Module`): Top-level organizational units that group related features. Modules are created via `Module.Builder` and registered in `Modules.init()`.
- **Features** (`insane96mcp.insanelib.base.Feature`): Specific functionality within a module. Features are discovered via the `@LoadFeature` annotation and automatically loaded through classpath scanning.
- **JsonFeatures** (`insane96mcp.insanelib.base.JsonFeature`): Extension of Feature that supports JSON-based configuration files for data-driven behavior.

**Key Flow:**
1. Modules are created in `Modules.init()` (called from `Config.CommonConfig` constructor)
2. Features are discovered via `@LoadFeature` annotation during `Module.loadFeatures()`
3. Each Feature can define config options via `@Config` annotated fields
4. Configuration is automatically loaded/synced via ForgeConfigSpec

**Example Feature Declaration:**
```java
@LoadFeature(module = "insanelib:base", name = "My Feature", description = "Does something", enabledByDefault = true)
public class MyFeature extends Feature {
    @Config(name = "My Setting", description = "A config value", min = 0.0, max = 100.0)
    public static Double mySetting = 50.0;
}
```

### Configuration System

- **Standard Configs**: Use `@Config` annotation on static fields within Features. Supports `Double`, `Integer`, `List<String>`, `Enum`, `MinMax`, `Difficulty`, `Blacklist`, `IdTagMatcher`.
- **JSON Configs**: JsonFeature subclasses can define `JsonConfig<T>` objects that load/reload JSON files from `config/insanelib/<module>/<feature>/` directories.
- **Config Syncing**: JSON configs can be synced to clients via `JsonConfigSyncMessage`.

### Data Matchers

**IdTagMatcher** (`insane96mcp.insanelib.data.IdTagMatcher`): Core utility for matching registry entries or tags.
- Format: `modid:entry` (ID) or `#modid:tag` (TAG)
- Supports dimension filtering: `#modid:tag,modid:dimension`
- Methods: `matchesBlock()`, `matchesItem()`, `matchesEntity()`, `matchesBiome()`, `matchesEnchantment()`, etc.
- Use `IdTagMatcher.parseLine()` to parse string representation

### Custom Events

InsaneLib provides custom Forge events in `insane96mcp.insanelib.event.*`:
- `AddEatEffectEvent`: Fired when food effects are applied
- `BlockBurntEvent`: Fired when a block is destroyed by fire
- `CakeEatEvent`: Fired when cake is eaten
- `FallingBlockLandEvent`: Fired when a falling block lands
- `HurtItemStackEvent`: Fired when an item takes damage
- `PlayerExhaustionEvent`: Fired when player exhaustion changes
- `PlayerSprintEvent`: Fired when player sprint state changes
- `PlayerUseItemSpeedModifierEvent`: Allows modifying item use speed

Events are fired via `ILEventFactory` or directly via mixins.

### Mixins

Mixins are located in `src/main/java/insane96mcp/insanelib/mixin/`. The mixin config is at `src/main/resources/mixins.insanelib.json`.

**Current Mixins:**
- Server-side: `CakeBlockMixin`, `FallingBlockEntityMixin`, `FireBlockMixin`, `ItemStackMixin`, `LivingEntityMixin`, `PlayerMixin`, `ServerLevelAccessor`
- Client-side: `client.LocalPlayerMixin`
- Accessor: `IntArrayTagAccessor`

The mod uses MixinExtras (version 0.4.1) for enhanced mixin capabilities.

### Network System

Network messages are in `insane96mcp.insanelib.network.message.*`:
- `EntityModNBTDataSync`: Syncs custom NBT data attached to entities
- `JsonConfigSyncMessage`: Syncs JSON configs to clients
- `MessageCreeperDataSync`: Legacy creeper data sync

Network handler is initialized in `InsaneLib.preInit()`.

### Utility Classes

- **MCUtils** (`util.MCUtils`): General Minecraft utilities for entity/player operations, attribute modifiers
- **TagUtils** (`util.TagUtils`): Tag lookup utilities
- **MathHelper** (`util.MathHelper`): Math operations
- **LogHelper** (`util.LogHelper`): Logging utilities
- **ConfigUtils** (`util.ConfigUtils`): Config parsing helpers
- **FileUtils** (`util.FileUtils`): File I/O operations
- **ILGsonHelper** (`util.json.ILGsonHelper`): Extended Gson helpers with validators
- **ModNBTData** (`util.ModNBTData`): Attach custom NBT to entities

### Data System

- **JsonFeatureDataReloadListener** (`data.JsonFeatureDataReloadListener`): Handles reloading JSON configs on datapack sync
- **InjectLootTableModifier** (`data.lootmodifier.InjectLootTableModifier`): Global loot modifier for injecting loot tables
- **SerializableAttributeModifier** (`data.SerializableAttributeModifier`): JSON-serializable attribute modifiers

### Package Structure

```
insane96mcp.insanelib/
├── ai/                    # Custom AI goals (e.g., ILNearestAttackableTargetGoal)
├── base/                  # Core Module/Feature system
│   ├── config/           # Config data types (MinMax, Difficulty, Blacklist)
│   ├── Module.java       # Module management
│   ├── Feature.java      # Base feature class
│   ├── JsonFeature.java  # JSON-config feature
│   └── LoadFeature.java  # Annotation for auto-loading features
├── data/                  # Data matchers and reload listeners
├── event/                 # Custom Forge events
├── exception/             # Custom exceptions
├── item/                  # Custom item tiers
├── mixin/                 # Mixin classes
├── module/                # Concrete modules
│   └── base/             # Base module features (TimeStopNoPlayerOnline, FixesFeature, TagsFeature, BetterFallingBlocks)
├── network/               # Network message handlers
├── setup/                 # Mod setup (Config, ILGlobalLootModifiers, ILStrings)
├── util/                  # Utility classes
│   ├── json/             # JSON utilities and validators
│   └── weightedrandom/   # Weighted random selection
└── world/                 # World-related utilities (scheduled tasks, effects)
```

## Development Notes

- Java 17 is required
- Uses official Minecraft mappings
- Access transformers are in `src/main/resources/META-INF/accesstransformer.cfg`
- Mixin refmap: `mixins.insanelib.refmap.json`
- All features are automatically discovered via `@LoadFeature` annotation - no manual registration needed
- When adding a new feature, annotate with `@LoadFeature(module = "insanelib:base")` and it will be auto-loaded
- The main branch for PRs is `1.18.x`, but development branches match Minecraft versions (e.g., `1.20.1`)