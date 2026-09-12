package studio.magemonkey.divinity.modules.list.classes.api;

import org.bukkit.configuration.InvalidConfigurationException;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import studio.magemonkey.codex.config.api.JYML;
import studio.magemonkey.divinity.testutil.MockedTest;

import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression tests for RPGClass's per-class permission check. Unlike every other permission
 * node in the plugin, this one is keyed by a user-defined class ID, so plugin.yml can't
 * statically alias divinity.classes.class.<id> to quantumrpg.classes.class.<id> (the ID space
 * is unbounded). RPGClass.hasPermission() instead does a two-way runtime check.
 */
class RPGClassPermissionTest extends MockedTest {

    private RPGClass newPermissionRequiredClass(String id) throws IOException, InvalidConfigurationException {
        java.io.File file = Files.createTempDirectory("rpgclass-test").resolve(id + ".yml").toFile();
        JYML cfg = new JYML(file);
        cfg.set("name", id);
        cfg.set("permission-required", true);
        cfg.set("leveling.max-level", 0);
        return new RPGClass(plugin, cfg);
    }

    @Test
    void divinityGrant_satisfiesPermissionCheck() throws Exception {
        RPGClass rpgClass = newPermissionRequiredClass("mage");
        PlayerMock player = genPlayer("class-test-divinity", false);
        player.addAttachment(plugin).setPermission("divinity.classes.class.mage", true);

        assertTrue(rpgClass.hasPermission(player));
    }

    @Test
    void legacyQuantumrpgGrant_satisfiesPermissionCheck() throws Exception {
        RPGClass rpgClass = newPermissionRequiredClass("warrior");
        PlayerMock player = genPlayer("class-test-legacy", false);
        player.addAttachment(plugin).setPermission("quantumrpg.classes.class.warrior", true);

        assertTrue(rpgClass.hasPermission(player));
    }

    @Test
    void noGrant_failsPermissionCheck() throws Exception {
        RPGClass rpgClass = newPermissionRequiredClass("rogue");
        PlayerMock player = genPlayer("class-test-none", false);

        assertFalse(rpgClass.hasPermission(player));
    }
}
