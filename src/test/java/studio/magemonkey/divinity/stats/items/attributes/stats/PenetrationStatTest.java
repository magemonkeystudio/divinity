package studio.magemonkey.divinity.stats.items.attributes.stats;

import org.junit.jupiter.api.Test;
import studio.magemonkey.divinity.testutil.MockedTest;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PenetrationStatTest extends MockedTest {

    @Test
    void isApplicableTo_matchesConfiguredHookCaseInsensitively() {
        PenetrationStat pen = new PenetrationStat(
                "test_physical_pen",
                "Physical Penetration",
                "&c▸ %name%: &f%value%%condition%",
                Set.of("physical"),
                false,
                -1D
        );

        assertTrue(pen.isApplicableTo("physical"), "Should match the exact configured hook");
        assertTrue(pen.isApplicableTo("PHYSICAL"), "Hook matching should be case-insensitive");
        assertFalse(pen.isApplicableTo("magical"), "Should not match a damage type outside its hooks");
    }

    @Test
    void constructor_lowercasesPenId() {
        PenetrationStat pen = new PenetrationStat(
                "TEST_MixedCase_Pen",
                "Mixed Case Penetration",
                "&c▸ %name%: &f%value%%condition%",
                Set.of("physical"),
                false,
                -1D
        );

        assertEquals("test_mixedcase_pen", pen.getPenId());
    }

    @Test
    void percentPenFlagIsPreserved() {
        PenetrationStat flatPen = new PenetrationStat(
                "test_flat_pen",
                "Flat Penetration",
                "&c▸ %name%: &f%value%%condition%",
                Set.of("physical"),
                false,
                -1D
        );
        PenetrationStat percentPen = new PenetrationStat(
                "test_percent_pen",
                "Percent Penetration",
                "&c▸ %name%: &f%value%%condition%",
                Set.of("physical"),
                true,
                -1D
        );

        assertFalse(flatPen.isPercentPen());
        assertTrue(percentPen.isPercentPen());
    }
}
