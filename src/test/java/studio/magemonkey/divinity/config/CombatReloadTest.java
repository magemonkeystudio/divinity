package studio.magemonkey.divinity.config;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import studio.magemonkey.divinity.manager.damage.formula.CombatExpression;
import studio.magemonkey.divinity.manager.damage.formula.CombatPreset;
import studio.magemonkey.divinity.testutil.MockedTest;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Goes through the plugin's real reload ({@code Divinity#reload}, which is what a config reload runs), not through
 * {@link CombatCfg#load} directly. The reload reads {@code engine.yml} first and {@code combat.yml} after it, which
 * the migration test depends on.
 */
class CombatReloadTest extends MockedTest {

    private File   combatFile;
    private File   engineFile;
    private String originalEngine;

    @BeforeAll
    void findFiles() throws IOException {
        combatFile = new File(plugin.getDataFolder(), CombatCfg.FILE_NAME);
        engineFile = new File(plugin.getDataFolder(), "engine.yml");
        assertTrue(engineFile.isFile(), "engine.yml is written when the plugin starts");
        originalEngine = read(engineFile);
    }

    @AfterEach
    void restore() throws IOException {
        write(engineFile, originalEngine);
        Files.deleteIfExists(combatFile.toPath());
        plugin.reload();
    }

    @AfterAll
    void leaveDefaults() {
        CombatCfg.resetToDefaults();
    }

    private static String read(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    }

    private static void write(File file, String text) throws IOException {
        Files.write(file.toPath(), text.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void startingThePlugin_wroteTheShippedFile() throws IOException {
        // The plugin was loaded by MockedTest; with no old key in engine.yml that is the default preset
        assertTrue(combatFile.isFile());
        assertTrue(read(combatFile).contains("\npreset: default\n"));
        assertEquals(CombatPreset.DEFAULT, CombatCfg.preset());
    }

    @Test
    void reload_picksUpEditsToCombatYml() throws IOException {
        write(combatFile, "preset: legacy\n");
        plugin.reload();
        assertEquals(CombatPreset.LEGACY, CombatCfg.preset());
        assertFalse(CombatCfg.formulas(false).isOverridden(CombatExpression.DEFENSE_VALUE));

        write(combatFile, "preset: default\nexpressions:\n  defense-value: \"defense * 0.5\"\n");
        plugin.reload();
        assertEquals(CombatPreset.DEFAULT, CombatCfg.preset());
        assertTrue(CombatCfg.formulas(false).isOverridden(CombatExpression.DEFENSE_VALUE));
        // 1 - max(3, 15 - 4 * 20 / 8 = 5) * 0.8 * 0.05 = 0.84 of 20
        assertEquals(20 * (1 - 5 * 0.8 * 0.05), CombatCfg.formulas(false)
                .eval(CombatExpression.DEFENSE_MITIGATION, 20D, CombatCfg.formulas(false)
                        .eval(CombatExpression.DEFENSE_VALUE, 30D, 1D, 1D), 0.8D, 0D), 1e-9);
    }

    @Test
    void reload_readsTheOldKeyFromEngineYmlBeforeMigrating() throws IOException {
        // An old server: legacy-combat is on in engine.yml and there is no combat.yml yet
        assertTrue(originalEngine.contains("\ncombat:\n"), "engine.yml has a combat section");
        write(engineFile, originalEngine.replace("\ncombat:\n", "\ncombat:\n  legacy-combat: true\n"));
        Files.delete(combatFile.toPath());

        plugin.reload();

        assertTrue(read(combatFile).contains("\npreset: legacy\n"), "migrated from the key just read");
        assertEquals(CombatPreset.LEGACY, CombatCfg.preset());
    }

    @Test
    void reload_ofAnExistingFile_ignoresTheOldKeyAndKeepsTheFile() throws IOException {
        String mine = "preset: default\n# kept\n";
        write(combatFile, mine);
        write(engineFile, originalEngine.replace("\ncombat:\n", "\ncombat:\n  legacy-combat: true\n"));

        plugin.reload();

        assertEquals(mine, read(combatFile));
        assertEquals(CombatPreset.DEFAULT, CombatCfg.preset());
    }
}
