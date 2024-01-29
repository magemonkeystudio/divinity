package su.nightexpress.quantumrpg.stats.bonus;

import mc.promcteam.engine.config.api.JYML;
import mc.promcteam.engine.utils.NumberUT;
import mc.promcteam.engine.utils.StringUT;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.config.EngineCfg;
import su.nightexpress.quantumrpg.hooks.EHook;
import su.nightexpress.quantumrpg.hooks.external.SkillAPIHK;
import su.nightexpress.quantumrpg.stats.items.ItemStats;
import su.nightexpress.quantumrpg.stats.items.api.ItemLoreStat;
import su.nightexpress.quantumrpg.stats.items.attributes.*;
import su.nightexpress.quantumrpg.stats.items.attributes.api.AbstractStat;
import su.nightexpress.quantumrpg.stats.items.attributes.stats.SimpleStat;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class BonusMap {

    private final Map<ItemLoreStat<?>, BiFunction<Boolean, Double, Double>> bonus;

    public BonusMap() {
        this.bonus = new HashMap<>();
    }

    public BonusMap(@NotNull BonusMap from) {
        this();
        BonusMap.this.bonus.putAll(from.bonus);
    }

    public void clear() {
        this.bonus.clear();
    }

    public boolean isEmpty() {
        return this.bonus.isEmpty();
    }

    @NotNull
    public Map<ItemLoreStat<?>, BiFunction<Boolean, Double, Double>> getBonuses() {
        return this.bonus;
    }

    @NotNull
    public Map<SimpleStat, BiFunction<Boolean, Double, Double>> getStatBonuses() {
        return this.bonus.entrySet().stream()
                .filter(entry -> entry.getKey() instanceof SimpleStat)
                .collect(Collectors.toMap(key -> (SimpleStat) key.getKey(), Map.Entry::getValue, (has, add) -> has));
    }

    @NotNull
    public Map<DamageAttribute, BiFunction<Boolean, Double, Double>> getDamageBonuses() {
        return this.bonus.entrySet().stream()
                .filter(entry -> entry.getKey() instanceof DamageAttribute)
                .collect(Collectors.toMap(key -> (DamageAttribute) key.getKey(), Map.Entry::getValue, (has, add) -> has));
    }

    @NotNull
    public Map<DefenseAttribute, BiFunction<Boolean, Double, Double>> getDefenseBonuses() {
        return this.bonus.entrySet().stream()
                .filter(entry -> entry.getKey() instanceof DefenseAttribute)
                .collect(Collectors.toMap(key -> (DefenseAttribute) key.getKey(), Map.Entry::getValue, (has, add) -> has));
    }

    @NotNull
    public Map<SkillAPIAttribute, BiFunction<Boolean, Double, Double>> getSkillAPIAttributeBonuses() {
        return this.bonus.entrySet().stream()
                .filter(entry -> entry.getKey() instanceof SkillAPIAttribute)
                .collect(Collectors.toMap(key -> (SkillAPIAttribute) key.getKey(), Map.Entry::getValue, (has, add) -> has));
    }

    @NotNull
    public Map<AmmoAttribute, BiFunction<Boolean, Double, Double>> getAmmoBonuses() {
        return this.bonus.entrySet().stream()
                .filter(entry -> entry.getKey() instanceof AmmoAttribute)
                .collect(Collectors.toMap(key -> (AmmoAttribute) key.getKey(), Map.Entry::getValue, (has, add) -> has));
    }

    @NotNull
    public Map<HandAttribute, BiFunction<Boolean, Double, Double>> getHandBonuses() {
        return this.bonus.entrySet().stream()
                .filter(entry -> entry.getKey() instanceof HandAttribute)
                .collect(Collectors.toMap(key -> (HandAttribute) key.getKey(), Map.Entry::getValue, (has, add) -> has));
    }

    public BiFunction<Boolean, Double, Double> getBonus(@NotNull ItemLoreStat<?> stat) {
        return this.bonus.getOrDefault(stat, (b, v) -> v);
    }

    public void loadStats(@NotNull JYML cfg, @NotNull String path) {
        for (String id : cfg.getSection(path)) {
            AbstractStat.Type dt = AbstractStat.Type.getByName(id);
            if (dt == null) continue;

            AbstractStat<?> mainStat = ItemStats.getStat(dt);
            if (!(mainStat instanceof SimpleStat)) continue;

            SimpleStat stat = (SimpleStat) mainStat;

            String sVal = cfg.getString(path + "." + id);
            if (sVal == null) continue;

            boolean perc = sVal.contains("%");
            double  val  = StringUT.getDouble(sVal.replace("%", ""), 0, true);

            BiFunction<Boolean, Double, Double> func = (isBonus, apply) -> perc == isBonus ? apply + val : apply;
            this.bonus.put(stat, func);
        }
    }

    public void loadDamages(@NotNull JYML cfg, @NotNull String path) {
        for (String id : cfg.getSection(path)) {
            DamageAttribute dt = ItemStats.getDamageById(id);
            if (dt == null) continue;

            String sVal = cfg.getString(path + "." + id);
            if (sVal == null) continue;

            boolean perc = sVal.contains("%");
            double  val  = StringUT.getDouble(sVal.replace("%", ""), 0, true);
            //if (val == 0) continue;

            BiFunction<Boolean, Double, Double> func = (bonus, apply) -> perc == bonus ? apply + val : apply;
            this.bonus.put(dt, func);
        }
    }

    public void loadDefenses(@NotNull JYML cfg, @NotNull String path) {
        for (String id : cfg.getSection(path)) {
            DefenseAttribute dt = ItemStats.getDefenseById(id);
            if (dt == null) continue;

            String sVal = cfg.getString(path + "." + id);
            if (sVal == null) continue;

            boolean perc = sVal.contains("%");
            double  val  = StringUT.getDouble(sVal.replace("%", ""), 0, true);
            //if (val == 0) continue;

            BiFunction<Boolean, Double, Double> func = (bonus, apply) -> perc == bonus ? apply + val : apply;
            this.bonus.put(dt, func);
        }
    }

    public void loadSkillAPIAttributes(@NotNull JYML cfg, @NotNull String path) {
        for (String id : cfg.getSection(path)) {
            SkillAPIHK skillAPIHK = (SkillAPIHK) QuantumRPG.getInstance().getHook(EHook.SKILL_API);
            if (skillAPIHK == null) continue;
            Collection<SkillAPIAttribute> attributes = skillAPIHK.getAttributes();

            SkillAPIAttribute stat = attributes.stream()
                    .filter(attribute -> attribute.getId().equalsIgnoreCase(id))
                    .findAny().orElse(null);
            if (stat == null) continue;

            String sVal = cfg.getString(path + "." + id);
            if (sVal == null) continue;

            boolean perc = sVal.contains("%");
            double  val  = StringUT.getDouble(sVal.replace("%", ""), 0, true);

            BiFunction<Boolean, Double, Double> func = (isBonus, apply) -> perc == isBonus ? apply + val : apply;
            this.bonus.put(stat, func);
        }
    }

    public void loadAmmo(@NotNull JYML cfg, @NotNull String path) {
        for (String id : cfg.getSection(path)) {
            AmmoAttribute stat;
            try {
                stat = ItemStats.getAmmo(AmmoAttribute.Type.valueOf(id.toUpperCase()));
                if (stat == null) continue;
            } catch (IllegalArgumentException e) {
                continue;
            }

            String sVal = cfg.getString(path + "." + id);
            if (sVal == null) continue;

            boolean perc = sVal.contains("%");
            double  val  = StringUT.getDouble(sVal.replace("%", ""), 0, true);

            BiFunction<Boolean, Double, Double> func = (isBonus, apply) -> perc == isBonus ? apply + val : apply;
            this.bonus.put(stat, func);
        }
    }

    public void loadHands(@NotNull JYML cfg, @NotNull String path) {
        for (String id : cfg.getSection(path)) {
            HandAttribute stat;
            try {
                stat = ItemStats.getHand(HandAttribute.Type.valueOf(id.toUpperCase()));
                if (stat == null) continue;
            } catch (IllegalArgumentException e) {
                continue;
            }

            String sVal = cfg.getString(path + "." + id);
            if (sVal == null) continue;

            boolean perc = sVal.contains("%");
            double  val  = StringUT.getDouble(sVal.replace("%", ""), 0, true);

            BiFunction<Boolean, Double, Double> func = (isBonus, apply) -> perc == isBonus ? apply + val : apply;
            this.bonus.put(stat, func);
        }
    }

    @NotNull
    public String replacePlaceholders(@NotNull String str) {
        for (Map.Entry<ItemLoreStat<?>, BiFunction<Boolean, Double, Double>> e : this.bonus.entrySet()) {
            double valRaw   = e.getValue().apply(false, 0D);
            double valBonus = e.getValue().apply(true, 0D);

            double valTotal  = valRaw != 0D ? valRaw : valBonus;
            String valFormat = NumberUT.format(valTotal);
            String valStr    = valBonus != 0D ? valFormat += EngineCfg.LORE_CHAR_PERCENT : valFormat;

            str = str.replace(e.getKey().getPlaceholder(), valStr);
        }
        return str;
    }
}
