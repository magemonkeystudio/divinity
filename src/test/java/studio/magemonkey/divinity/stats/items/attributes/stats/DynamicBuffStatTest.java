package studio.magemonkey.divinity.stats.items.attributes.stats;

import org.junit.jupiter.api.Test;
import studio.magemonkey.divinity.stats.items.ItemStats;
import studio.magemonkey.divinity.testutil.MockedTest;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DynamicBuffStatTest extends MockedTest {

    @Test
    void isApplicableTo_matchesConfiguredHookCaseInsensitively() {
        DynamicBuffStat buff = new DynamicBuffStat(
                DynamicBuffStat.BuffTarget.DAMAGE,
                "test_physical_buff",
                "Physical Buff %",
                "&3▸ %name%: &f%value%%condition%",
                Set.of("physical"),
                -1D
        );

        assertTrue(buff.isApplicableTo("physical"), "Should match the exact configured hook");
        assertTrue(buff.isApplicableTo("PHYSICAL"), "Hook matching should be case-insensitive");
        assertFalse(buff.isApplicableTo("magical"), "Should not match a damage type outside its hooks");
    }

    @Test
    void constructor_lowercasesBuffId() {
        DynamicBuffStat buff = new DynamicBuffStat(
                DynamicBuffStat.BuffTarget.DEFENSE,
                "TEST_MixedCase_Id",
                "Mixed Case Buff",
                "&9▸ %name%: &f%value%%condition%",
                Set.of("physical"),
                -1D
        );

        assertEquals("test_mixedcase_id", buff.getBuffId());
    }

    @Test
    void buffTargetIsPreserved() {
        DynamicBuffStat damageBuff = new DynamicBuffStat(
                DynamicBuffStat.BuffTarget.DAMAGE,
                "test_damage_target",
                "Damage Target Buff",
                "&3▸ %name%: &f%value%%condition%",
                Set.of("physical"),
                -1D
        );
        DynamicBuffStat defenseBuff = new DynamicBuffStat(
                DynamicBuffStat.BuffTarget.DEFENSE,
                "test_defense_target",
                "Defense Target Buff",
                "&9▸ %name%: &f%value%%condition%",
                Set.of("physical"),
                -1D
        );

        assertEquals(DynamicBuffStat.BuffTarget.DAMAGE, damageBuff.getBuffTarget());
        assertEquals(DynamicBuffStat.BuffTarget.DEFENSE, defenseBuff.getBuffTarget());
    }

    @Test
    void constructor_registersItselfUnderItsBuffTargetSpecificRegistry() {
        DynamicBuffStat damageBuff = new DynamicBuffStat(
                DynamicBuffStat.BuffTarget.DAMAGE,
                "test_damage_registry_buff",
                "Damage Registry Buff",
                "&3▸ %name%: &f%value%%condition%",
                Set.of("physical"),
                -1D
        );
        DynamicBuffStat defenseBuff = new DynamicBuffStat(
                DynamicBuffStat.BuffTarget.DEFENSE,
                "test_defense_registry_buff",
                "Defense Registry Buff",
                "&9▸ %name%: &f%value%%condition%",
                Set.of("physical"),
                -1D
        );

        assertSame(damageBuff, ItemStats.getDamageBuff(damageBuff.getBuffId()),
                "A DAMAGE-target buff must be looked up via getDamageBuff");
        assertNull(ItemStats.getDefenseBuff(damageBuff.getBuffId()),
                "A DAMAGE-target buff must not leak into the defense-buff registry");

        assertSame(defenseBuff, ItemStats.getDefenseBuff(defenseBuff.getBuffId()),
                "A DEFENSE-target buff must be looked up via getDefenseBuff");
        assertNull(ItemStats.getDamageBuff(defenseBuff.getBuffId()),
                "A DEFENSE-target buff must not leak into the damage-buff registry");
    }
}
