# Copilot instructions for Divinity

## Build and test

- `mvn test` — run the full test suite.
- `mvn package` — build the plugin jar.
- `mvn -Dtest=studio.magemonkey.divinity.modules.list.itemgenerator.ItemGeneratorManagerTest test` — run one test class.
- `mvn -Dtest=studio.magemonkey.divinity.modules.list.itemgenerator.ItemGeneratorManagerTest#<methodName> test` — run one test method.

## Architecture

- `studio.magemonkey.divinity.Divinity` is the plugin entrypoint and lifecycle coordinator. It extends `CodexDataPlugin<Divinity, DivinityUser>` and owns startup/shutdown, config loading, hook registration, custom actions, data setup, and module bootstrap.
- Startup is ordered: `onLoad()` hooks Mimic, `enable()` sets up custom actions and PMS, checks the Codex core version, loads profile settings, then initializes managers, the module cache, worth handling, and the `DivinityProvider` damage/item registration.
- Runtime features are split into managers under `manager/` and feature modules under `modules/list/`. `ModuleCache.initialize()` registers the active modules in a fixed order; `ListenerManager` registers listeners conditionally based on loaded stats, requirements, and server version.
- Config is data-driven. `src/main/resources/config.yml` defines tiers, item groups, and item sub-types; `config/Config.java` reads those and then extracts item-stat templates from resource YAML files under `item_stats/`.
- Hooking is two-layered: `Divinity.registerHooks()` handles the main hook registration and version-specific MythicMobs selection, while `HookListener` adds dynamic hook behavior after startup.
- The plugin keeps compatibility metadata in `plugin.yml` and `paper-plugin.yml`, including legacy names like ProRPGItems/QuantumRPG and the Bukkit/Paper entrypoint.

## Conventions

- Prefer the existing lifecycle pattern: managers implement `Loadable` and expose `setup()`/`shutdown()`, listeners register/unregister themselves, and shutdown code nulls optional managers after cleanup.
- Use the existing package structure when adding code: commands live beside their manager, module-specific code stays under `modules/list/<module>/`, and shared runtime helpers stay under `utils/`, `stats/`, or `hooks/`.
- Keep configuration access consistent with the rest of the codebase: load/extract YAML through `JYML`, colorize text with `StringUT.color(...)`, and preserve default values when adding new config keys.
- Optional integrations are common. Check for presence/version before wiring hooks or listeners, and preserve the null-safe handling used for optional managers like profiles and some listeners.
- Tests use `MockBukkit` through `src/test/java/studio/magemonkey/divinity/testutil/MockedTest.java`; the harness depends on `CODEX_VERSION` and `DIVINITY_VERSION` system properties from Surefire and loads compiled classes into a mock plugin jar.
- Legacy third-party jars in `libs/` are still wired through Maven `systemPath` dependencies; do not remove or rename them without updating `pom.xml`.
