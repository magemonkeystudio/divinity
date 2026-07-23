package studio.magemonkey.divinity.stats.items.attributes.stats;

import org.junit.jupiter.api.Test;
import studio.magemonkey.divinity.testutil.MockedTest;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
}
