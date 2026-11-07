package studio.magemonkey.divinity.manager.stats;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatsDescriptionWrapTest {

    @Test
    void wrapsDescriptionIntoShortLoreLines() {
        String description = "Scales eligible Divinity custom mob-drop chances, not drop quantity. "
                + "+50% turns a 20% chance into 30%.";
        List<String> lines = StatsCatalog.Row.wrapDescription(description, 30);

        assertTrue(lines.size() > 2);
        assertTrue(lines.stream().allMatch(line -> line.length() <= 30));
        assertEquals(description, String.join(" ", lines));
    }
}
