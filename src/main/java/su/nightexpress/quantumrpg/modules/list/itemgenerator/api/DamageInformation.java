package su.nightexpress.quantumrpg.modules.list.itemgenerator.api;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DamageInformation {

    private double chance, min, max, scaleByLevel;
    private boolean flatRange, round;

}
