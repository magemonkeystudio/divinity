package studio.magemonkey.divinity.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import studio.magemonkey.divinity.manager.damage.DamageCalculator;
import studio.magemonkey.divinity.manager.damage.DamageCalculator.Modifiers;
import studio.magemonkey.divinity.manager.damage.formula.CombatExpression;
import studio.magemonkey.divinity.manager.damage.formula.CombatPreset;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CombatCfgTest {

    private static final double DELTA = 1e-9;

    @TempDir
    File folder;

    private final List<String> info = new ArrayList<>();
    private final List<String> warn = new ArrayList<>();

    @AfterEach
    void reset() {
        CombatCfg.resetToDefaults();
    }

    private void load(Boolean oldKey) {
        CombatCfg.load(folder, oldKey, info::add, warn::add);
    }

    private File file() {
        return new File(folder, CombatCfg.FILE_NAME);
    }

    private String read() throws IOException {
        return new String(Files.readAllBytes(file().toPath()), StandardCharsets.UTF_8);
    }

    private void write(String text) throws IOException {
        Files.write(file().toPath(), text.getBytes(StandardCharsets.UTF_8));
    }

    /** 20 damage against 30 defense at protection factor 0.8, toughness 0, nothing else. Default 4, legacy 15.2. */
    private static double hit() {
        Modifiers m = new Modifiers(1, 1, 1, true, 1, 1, 0, 1, 1, 0, false);
        return new DamageCalculator(m).calculate(20, 1, 1, 30D, 0.8).getDamage();
    }

    // ------------------------------------------------------------------ //
    // Before anything is loaded
    // ------------------------------------------------------------------ //

    @Test
    void beforeLoading_theDefaultPresetIsInForce() {
        assertEquals(CombatPreset.DEFAULT, CombatCfg.preset());
        assertEquals(4D, hit(), DELTA);
    }

    // ------------------------------------------------------------------ //
    // Migration
    // ------------------------------------------------------------------ //

    @Test
    void migration_oldKeyTrue_writesTheLegacyPreset() throws IOException {
        load(true);

        assertTrue(read().contains("\npreset: legacy\n"));
        assertEquals(CombatPreset.LEGACY, CombatCfg.preset());
        assertEquals(15.2D, hit(), DELTA);
        assertEquals(1, info.size(), info.toString());
        assertTrue(info.get(0).contains("legacy-combat: true") && info.get(0).contains("legacy"), info.get(0));
        assertTrue(warn.isEmpty(), warn.toString());
    }

    @Test
    void migration_oldKeyFalse_writesTheDefaultPreset() throws IOException {
        load(false);

        assertTrue(read().contains("\npreset: default\n"));
        assertEquals(CombatPreset.DEFAULT, CombatCfg.preset());
        assertEquals(4D, hit(), DELTA);
        assertEquals(1, info.size(), info.toString());
        assertTrue(info.get(0).contains("legacy-combat: false"), info.get(0));
    }

    @Test
    void migration_noOldKey_writesTheDefaultFileWithoutAMessage() throws IOException {
        load(null);

        assertTrue(file().isFile());
        assertEquals(CombatPreset.DEFAULT, CombatCfg.preset());
        assertTrue(info.isEmpty(), info.toString());
        assertTrue(warn.isEmpty(), warn.toString());
    }

    @Test
    void migration_writtenFileIsTheShippedOneApartFromThePresetLine() throws IOException {
        load(false);
        String defaults = read();
        file().delete();
        load(true);
        assertEquals(defaults.replace("\npreset: default\n", "\npreset: legacy\n"), read());
    }

    @Test
    void migration_neverOverwritesAnExistingFile() throws IOException {
        String mine = "preset: default\nexpressions:\n  defense-value: \"defense * 0.5\"\n# my own notes\n";
        write(mine);

        load(true); // the old key says legacy, the file says default: the file wins

        assertEquals(mine, read());
        assertEquals(CombatPreset.DEFAULT, CombatCfg.preset());
        assertTrue(info.isEmpty(), "nothing was migrated, so nothing is reported: " + info);
        // and its override is in force: 1 - max(3, 15 - 4 * 20 / 8 = 5) * 0.8 * 0.05 = 0.84
        assertEquals(20 * (1 - 5 * 0.8 * 0.05), hit(), DELTA);
    }

    @Test
    void migration_isIdempotent() throws IOException {
        load(true);
        String first = read();

        load(true);
        load(false); // even if the old key changed in engine.yml afterwards

        assertEquals(first, read());
        assertEquals(CombatPreset.LEGACY, CombatCfg.preset());
        assertEquals(1, info.size(), "reported once, on the run that migrated: " + info);
    }

    @Test
    void oldKeyIsIgnoredOnceTheFileExists() throws IOException {
        load(true);
        write(read().replace("\npreset: legacy\n", "\npreset: default\n")); // the admin switches presets by hand

        load(true); // engine.yml still says legacy-combat: true

        assertEquals(CombatPreset.DEFAULT, CombatCfg.preset());
        assertEquals(4D, hit(), DELTA);
    }

    @Test
    void migration_cannotWriteTheFile_stillHonoursTheOldKeyForThisRun() throws IOException {
        File notAFolder = new File(folder, "plain-file");
        assertTrue(notAFolder.createNewFile());

        CombatCfg.load(new File(notAFolder, "inside"), true, info::add, warn::add);

        assertEquals(CombatPreset.LEGACY, CombatCfg.preset());
        assertEquals(1, warn.size(), warn.toString());
        assertTrue(warn.get(0).contains("could not be written"), warn.get(0));
    }

    // ------------------------------------------------------------------ //
    // Reload
    // ------------------------------------------------------------------ //

    @Test
    void reload_picksUpChangesToThePresetAndToExpressions() throws IOException {
        load(false);
        assertEquals(4D, hit(), DELTA);

        write("preset: legacy\n");
        load(false);
        assertEquals(15.2D, hit(), DELTA);

        write("preset: legacy\nexpressions:\n  defense-mitigation: \"damage - 1\"\n");
        load(false);
        assertEquals(19D, hit(), DELTA);

        write("preset: default\n");
        load(false);
        assertEquals(4D, hit(), DELTA);
    }

    @Test
    void reload_ofABrokenFile_neverLeavesCombatWithoutFormulas() throws IOException {
        load(true);
        write("preset: [unclosed\n  - : :\n");

        load(true);

        assertEquals(CombatPreset.DEFAULT, CombatCfg.preset());
        assertEquals(4D, hit(), DELTA);
        assertEquals(1, warn.size(), warn.toString());
        assertTrue(warn.get(0).contains("could not be read"), warn.get(0));
        assertTrue(read().contains("[unclosed"), "the broken file is left for the admin to fix");
    }

    // ------------------------------------------------------------------ //
    // Problems in the file
    // ------------------------------------------------------------------ //

    @Test
    void brokenFormula_isReportedOnceAndOnlyThatOneFallsBack() throws IOException {
        write("preset: legacy\nexpressions:\n"
                + "  defense-value: \"defense *\"\n"
                + "  direct-damage: \"damage * 0.5\"\n");

        load(null);

        assertEquals(1, warn.size(), warn.toString());
        assertTrue(warn.get(0).contains("'defense-value'") && warn.get(0).contains("legacy"), warn.get(0));
        // legacy mitigation with the preset's defense value: 1 - 30 * 0.8 * 0.01, and half the damage is direct
        Modifiers m = new Modifiers(1, 1, 1, true, 1, 1, 0, 1, 1, 0, false);
        assertEquals(10 * 0.76 + 10, new DamageCalculator(m).calculate(20, 1, 1, 30D, 0.8).getDamage(), DELTA);
    }

    @Test
    void unknownPreset_isReportedAndTheDefaultIsUsed() throws IOException {
        write("preset: legcay\n");
        load(null);

        assertEquals(CombatPreset.DEFAULT, CombatCfg.preset());
        assertEquals(1, warn.size());
        assertTrue(warn.get(0).contains("legcay"), warn.get(0));
    }

    @Test
    void unknownExpressionKey_isReportedAndIgnored() throws IOException {
        write("expressions:\n  defence-value: \"defense * 0.5\"\n");
        load(null);

        assertEquals(1, warn.size());
        assertTrue(warn.get(0).contains("defence-value"), warn.get(0));
        assertEquals(4D, hit(), DELTA);
    }

    @Test
    void emptyFile_meansTheDefaultPresetWithoutComplaint() throws IOException {
        write("");
        load(null);

        assertEquals(CombatPreset.DEFAULT, CombatCfg.preset());
        assertTrue(warn.isEmpty(), warn.toString());
    }

    @Test
    void shippedFile_loadsCleanlyAndChangesNothing() {
        load(null);

        assertTrue(warn.isEmpty(), warn.toString());
        for (CombatExpression expression : CombatExpression.values()) {
            assertFalse(CombatCfg.formulas(false).isOverridden(expression), expression.key());
        }
    }

    // ------------------------------------------------------------------ //
    // The deprecated switch
    // ------------------------------------------------------------------ //

    @Test
    @SuppressWarnings("deprecation")
    void forcingLegacy_usesTheLegacyPresetWhateverTheFileSays() throws IOException {
        load(false);
        Modifiers legacy = new Modifiers(1, 1, 1, true, 1, 1, 0, 1, 1, 0, true);

        assertEquals(15.2D, new DamageCalculator(legacy).calculate(20, 1, 1, 30D, 0.8).getDamage(), DELTA);
        assertEquals(4D, hit(), DELTA);
        assertFalse(EngineCfg.LEGACY_COMBAT, "nothing sets the deprecated switch");
    }

    @Test
    void forcingLegacy_keepsOverridesFromTheFile() throws IOException {
        write("expressions:\n  defense-value: \"defense * 0.5\"\n");
        load(null);
        Modifiers legacy = new Modifiers(1, 1, 1, true, 1, 1, 0, 1, 1, 0, true);

        // legacy: 1 - 15 * 0.8 * 0.01 = 0.88
        assertEquals(20 * 0.88, new DamageCalculator(legacy).calculate(20, 1, 1, 30D, 0.8).getDamage(), DELTA);
    }
}
