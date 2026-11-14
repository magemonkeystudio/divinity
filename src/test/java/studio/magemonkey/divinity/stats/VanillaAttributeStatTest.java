package studio.magemonkey.divinity.stats;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class VanillaAttributeStatTest {

    /** Configured in percent: the vanilla attribute is a 0..1-ish fraction or multiplier. */
    private static final Set<VanillaAttributeStat> PERCENT_STATS = EnumSet.of(
            VanillaAttributeStat.WATER_MOVEMENT_EFFICIENCY,
            VanillaAttributeStat.MOVEMENT_EFFICIENCY,
            VanillaAttributeStat.SNEAKING_SPEED,
            VanillaAttributeStat.BLOCK_BREAK_SPEED,
            VanillaAttributeStat.EXPLOSION_KNOCKBACK_RESISTANCE,
            VanillaAttributeStat.FALL_DAMAGE_MULTIPLIER,
            VanillaAttributeStat.SUBMERGED_MINING_SPEED);

    @Test
    void percentStatsAreScaledAndRawStatsAreNot() {
        for (VanillaAttributeStat stat : VanillaAttributeStat.values()) {
            double expected = PERCENT_STATS.contains(stat) ? 0.5 : 50;
            assertEquals(expected, stat.toAttributeAmount(50), 1e-9, stat.name());
        }
    }

    @Test
    void rawStatsKeepTheirUnits() {
        // The original bug: +5 block reach was applied as +0.05
        assertEquals(5, VanillaAttributeStat.BLOCK_INTERACTION_RANGE.toAttributeAmount(5), 1e-9);
        assertEquals(0.5, VanillaAttributeStat.STEP_HEIGHT.toAttributeAmount(0.5), 1e-9);
    }

    @Test
    void modifierIdsAreStableAndUnique() {
        // Changing these would orphan modifiers already applied to entities
        assertEquals(UUID.fromString("d141e000-5ca1-4000-0000-000000000001"),
                VanillaAttributeStat.SCALE.getModifierId());
        assertEquals(UUID.fromString("d141e000-5ca1-4000-0000-00000000000a"),
                VanillaAttributeStat.FLYING_SPEED.getModifierId());
        assertEquals(UUID.fromString("d141e000-5ca1-4000-0000-000000000012"),
                VanillaAttributeStat.SUBMERGED_MINING_SPEED.getModifierId());

        Set<UUID> ids = new HashSet<>();
        for (VanillaAttributeStat stat : VanillaAttributeStat.values()) {
            assertTrue(ids.add(stat.getModifierId()), "Duplicate modifier id for " + stat);
            assertEquals("divinity." + stat.name().toLowerCase(), stat.getModifierName());
        }
    }

    @Test
    void bundledConfigDefinesEveryStat() throws Exception {
        YamlConfiguration cfg;
        try (InputStream in = getClass().getClassLoader()
                .getResourceAsStream("item_stats/stats/general_stats.yml")) {
            assertNotNull(in, "bundled general_stats.yml");
            cfg = YamlConfiguration.loadConfiguration(new InputStreamReader(in, StandardCharsets.UTF_8));
        }

        for (VanillaAttributeStat stat : VanillaAttributeStat.values()) {
            ConfigurationSection section = cfg.getConfigurationSection(stat.getStatType().name());
            assertNotNull(section, "Missing default section for " + stat);
            assertTrue(section.getBoolean("enabled"), stat + " should ship enabled");
            assertTrue(section.contains("capacity"), stat + " should ship a capacity");
        }
    }
}
