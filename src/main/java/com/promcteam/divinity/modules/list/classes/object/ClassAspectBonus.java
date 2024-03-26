package com.promcteam.divinity.modules.list.classes.object;

import com.promcteam.divinity.stats.bonus.BonusMap;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ClassAspectBonus {

    private int                             maxValue;
    private Map<ClassAttributeType, Double> pointAtt;
    private BonusMap                        bonusMap;

    public ClassAspectBonus(
            int maxValue,
            Map<ClassAttributeType, Double> pointAtt,
            BonusMap bonusMap
    ) {
        this.setMaxValue(maxValue);
        this.pointAtt = pointAtt;
        this.bonusMap = bonusMap;
    }

    public int getMaxValue() {
        return this.maxValue;
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
    }

    public double getPerPointAttribute(@NotNull ClassAttributeType type) {
        return this.pointAtt.getOrDefault(type, 0D);
    }

    @NotNull
    public BonusMap getBonusMap() {
        return this.bonusMap;
    }
}
