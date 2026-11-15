package studio.magemonkey.divinity.config;

import com.google.common.annotations.VisibleForTesting;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.magemonkey.divinity.Divinity;
import studio.magemonkey.divinity.manager.damage.formula.CombatExpression;
import studio.magemonkey.divinity.manager.damage.formula.CombatFormulas;
import studio.magemonkey.divinity.manager.damage.formula.CombatPreset;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Loads {@code combat.yml}, the damage formulas, and keeps the result for the damage calculator.
 * <p>
 * Loading never throws and never overwrites an existing {@code combat.yml}. When there is none, one is written:
 * from the shipped file, with the preset that matches the old {@code combat.legacy-combat} key of
 * {@code engine.yml}. That key is read for this one purpose and is ignored afterwards.
 */
public final class CombatCfg {

    public static final String FILE_NAME = "combat.yml";

    private static final String PRESET_KEY      = "preset";
    private static final String EXPRESSIONS_KEY = "expressions";

    /** What is in force. Replaced as a whole, so a hit always sees one consistent set. */
    private static volatile State state = State.defaults();

    private CombatCfg() {
    }

    /** Loads (and if needed creates) {@code combat.yml} in the plugin's data folder. Call again to reload. */
    public static void load(@NotNull Divinity plugin) {
        load(plugin.getDataFolder(),
                EngineCfg.LEGACY_COMBAT_OLD_KEY,
                plugin.getLogger()::info,
                plugin.getLogger()::warning);
    }

    /**
     * @param folder       the folder that holds (or will hold) {@code combat.yml}
     * @param oldLegacyKey the value of {@code combat.legacy-combat} in {@code engine.yml}, or null if it is not there
     * @param info         receives the one line that reports a migration
     * @param warn         receives every problem, each once
     */
    public static void load(@NotNull File folder,
                            @Nullable Boolean oldLegacyKey,
                            @NotNull Consumer<String> info,
                            @NotNull Consumer<String> warn) {
        File file = new File(folder, FILE_NAME);

        CombatPreset migrated = null;
        if (!file.exists()) {
            migrated = migrate(file, oldLegacyKey, info, warn);
        }

        CombatPreset        preset    = migrated != null ? migrated : CombatPreset.DEFAULT;
        Map<String, Object> overrides = new HashMap<>();

        if (file.exists()) {
            YamlConfiguration yaml = new YamlConfiguration();
            try {
                yaml.load(file);
                preset = readPreset(yaml, warn);
                overrides = readOverrides(yaml, warn);
            } catch (IOException | InvalidConfigurationException | RuntimeException e) {
                warn.accept(FILE_NAME + " could not be read (" + e.getMessage() + "). Using the "
                        + CombatPreset.DEFAULT.key() + " preset. The file is left as it is.");
                preset = CombatPreset.DEFAULT;
                overrides.clear();
            }
        }

        // Only the preset in use reports its problems; the other one is built for the deprecated override below
        CombatFormulas[] byPreset = new CombatFormulas[CombatPreset.values().length];
        for (CombatPreset each : CombatPreset.values()) {
            byPreset[each.ordinal()] = CombatFormulas.build(each, overrides, each == preset ? warn : null);
        }
        state = new State(preset, byPreset);
    }

    /**
     * The formulas the damage calculator uses.
     *
     * @param forceLegacy selects the legacy preset whatever {@code combat.yml} says. Nothing in the plugin sets
     *                    this; it exists for callers of the old {@code EngineCfg.LEGACY_COMBAT} switch.
     */
    @NotNull
    public static CombatFormulas formulas(boolean forceLegacy) {
        State current = state;
        return current.byPreset[(forceLegacy ? CombatPreset.LEGACY : current.preset).ordinal()];
    }

    /** The preset {@code combat.yml} selects. */
    @NotNull
    public static CombatPreset preset() {
        return state.preset;
    }

    /** Back to the shipped default preset with no overrides, as before anything was loaded. */
    @VisibleForTesting
    public static void resetToDefaults() {
        state = State.defaults();
    }

    /**
     * Writes {@code combat.yml} for a server that has none.
     *
     * @return the preset the old key selects, or null if nothing was migrated (no old key, and no file written)
     */
    @Nullable
    private static CombatPreset migrate(@NotNull File file,
                                        @Nullable Boolean oldLegacyKey,
                                        @NotNull Consumer<String> info,
                                        @NotNull Consumer<String> warn) {
        CombatPreset preset = Boolean.TRUE.equals(oldLegacyKey) ? CombatPreset.LEGACY : CombatPreset.DEFAULT;

        String shipped = readShipped();
        if (shipped == null) {
            warn.accept(FILE_NAME + " is missing from the plugin jar, so none was written.");
            return preset;
        }
        String text = shipped.replaceFirst("(?m)^preset: default\\s*$", "preset: " + preset.key());

        try {
            File parent = file.getParentFile();
            if (parent != null) parent.mkdirs();
            // CREATE_NEW: an existing file is never overwritten, even if one appears after the check above
            Files.write(file.toPath(), text.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE_NEW);
        } catch (FileAlreadyExistsException e) {
            return null;
        } catch (IOException e) {
            warn.accept(FILE_NAME + " could not be written (" + e.getMessage() + "). Using the "
                    + preset.key() + " preset for this run.");
            return preset;
        }

        if (oldLegacyKey != null) {
            info.accept("Moved 'combat.legacy-combat: " + oldLegacyKey + "' from engine.yml to " + FILE_NAME
                    + " (preset: " + preset.key() + "). The old key is now ignored.");
        }
        return preset;
    }

    @Nullable
    private static String readShipped() {
        try (InputStream in = CombatCfg.class.getResourceAsStream("/" + FILE_NAME)) {
            if (in == null) return null;
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }

    @NotNull
    private static CombatPreset readPreset(@NotNull YamlConfiguration yaml, @NotNull Consumer<String> warn) {
        Object value = yaml.get(PRESET_KEY);
        if (value == null) return CombatPreset.DEFAULT;

        CombatPreset preset = CombatPreset.byKey(String.valueOf(value));
        if (preset == null) {
            warn.accept(FILE_NAME + ": unknown preset '" + value + "' (use 'default' or 'legacy'). Using '"
                    + CombatPreset.DEFAULT.key() + "'.");
            return CombatPreset.DEFAULT;
        }
        return preset;
    }

    @NotNull
    private static Map<String, Object> readOverrides(@NotNull YamlConfiguration yaml,
                                                     @NotNull Consumer<String> warn) {
        Map<String, Object>  overrides = new HashMap<>();
        ConfigurationSection section   = yaml.getConfigurationSection(EXPRESSIONS_KEY);
        if (section == null) return overrides;

        for (String key : section.getKeys(false)) {
            if (CombatExpression.byKey(key) == null) {
                warn.accept(FILE_NAME + ": '" + key + "' is not a formula and is ignored.");
                continue;
            }
            overrides.put(key, section.get(key));
        }
        return overrides;
    }

    private static final class State {
        final CombatPreset     preset;
        final CombatFormulas[] byPreset;

        State(@NotNull CombatPreset preset, @NotNull CombatFormulas[] byPreset) {
            this.preset = preset;
            this.byPreset = byPreset;
        }

        static State defaults() {
            CombatFormulas[] all = new CombatFormulas[CombatPreset.values().length];
            for (CombatPreset each : CombatPreset.values()) {
                all[each.ordinal()] = CombatFormulas.of(each);
            }
            return new State(CombatPreset.DEFAULT, all);
        }
    }
}
