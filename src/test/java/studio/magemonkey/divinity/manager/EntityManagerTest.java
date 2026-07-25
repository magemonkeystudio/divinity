package studio.magemonkey.divinity.manager;

import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import studio.magemonkey.divinity.config.EngineCfg;
import studio.magemonkey.divinity.testutil.MockedTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntityManagerTest extends MockedTest {
    private PlayerMock player;

    @BeforeEach
    void setup() {
        player = genPlayer("Travja");
    }

    @AfterEach
    void resetFlag() {
        EngineCfg.LEGACY_VANILLA_ENTITY_STATS = false;
    }

    @Test
    void entityStatsHandlingAppliesDuplicatorFixerByDefault() {
        EngineCfg.LEGACY_VANILLA_ENTITY_STATS = false;

        server.getPluginManager().callEvent(new PlayerToggleSprintEvent(player, true));

        assertTrue(EntityManager.isPacketDuplicatorFixed(player),
                "Duplicator fixer metadata should be applied when legacy vanilla entity stats is disabled");
    }

    @Test
    void legacyVanillaEntityStatsSkipsDuplicatorFixer() {
        EngineCfg.LEGACY_VANILLA_ENTITY_STATS = true;

        server.getPluginManager().callEvent(new PlayerToggleSprintEvent(player, true));

        assertFalse(EntityManager.isPacketDuplicatorFixed(player),
                "Duplicator fixer metadata should not be applied when legacy vanilla entity stats is enabled");
    }
}
