package su.nightexpress.quantumrpg.modules.list.itemgenerator.generators;

import mc.promcteam.engine.config.api.JYML;
import mc.promcteam.engine.utils.NumberUT;
import mc.promcteam.engine.utils.StringUT;
import mc.promcteam.engine.utils.random.Rnd;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.ItemGeneratorManager.GeneratorItem;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.api.AbstractAttributeGenerator;
import su.nightexpress.quantumrpg.stats.bonus.BonusCalculator;
import su.nightexpress.quantumrpg.stats.items.ItemStats;
import su.nightexpress.quantumrpg.stats.items.api.ItemLoreStat;
import su.nightexpress.quantumrpg.stats.items.attributes.DamageAttribute;
import su.nightexpress.quantumrpg.stats.items.attributes.DefenseAttribute;
import su.nightexpress.quantumrpg.stats.items.attributes.SocketAttribute;
import su.nightexpress.quantumrpg.stats.items.attributes.api.AbstractStat;
import su.nightexpress.quantumrpg.stats.items.attributes.api.DoubleStat;
import su.nightexpress.quantumrpg.stats.items.attributes.stats.SimpleStat;
import su.nightexpress.quantumrpg.utils.ItemUtils;
import su.nightexpress.quantumrpg.utils.LoreUT;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class AttributeGenerator<A extends ItemLoreStat<?>> extends AbstractAttributeGenerator {

    protected Map<A, double[]> attributes;

    public AttributeGenerator(
            @NotNull QuantumRPG plugin,
            @NotNull GeneratorItem generatorItem,
            @NotNull String path,
            @NotNull Collection<A> attributesAll,
            @NotNull String placeholder
    ) {
        super(plugin, generatorItem, placeholder);

        JYML cfg = this.generatorItem.getConfig();

        this.minAmount = cfg.getInt(path + "minimum");
        this.maxAmount = cfg.getInt(path + "maximum");
        this.loreFormat = StringUT.color(cfg.getStringList(path + "lore-format"));
        this.attributes = new HashMap<>();

        attributesAll.forEach(att -> {
            String  path2    = path + "list." + att.getId() + ".";
            boolean isSocket = att instanceof SocketAttribute;

            cfg.addMissing(path2 + "chance", 0D);
            if (!isSocket) {
                cfg.addMissing(path2 + "scale-by-level", 1D);
                cfg.addMissing(path2 + "min", "0");
                cfg.addMissing(path2 + "max", "0");
            }

            if (!this.loreFormat.contains(att.getPlaceholder())) {
                this.loreFormat.add(att.getPlaceholder());
                cfg.set(path + "lore-format", this.loreFormat);
            }

            double chance = cfg.getDouble(path2 + "chance");
            if (chance <= 0) return;

            double m1    = cfg.getDouble(path2 + "min", 0D);
            double m2    = cfg.getDouble(path2 + "max", 0D);
            double scale = cfg.getDouble(path2 + "scale-by-level", 1D);
            this.attributes.put(att, new double[]{chance, m1, m2, scale});
        });
    }

    @Override
    public void generate(@NotNull ItemStack item, int itemLevel) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        List<String> lore = meta.getLore();
        if (lore == null) return;

        // Check if attribute type is Socket
        // This is used to allow max. value to be unlimited regardless of the map amount.
        boolean isSocket = false;
        boolean isValid  = true;

        Optional<A> opt = this.getAttributes().keySet().stream().findFirst();
        if (opt.isPresent()) {
            A check = opt.get();
            if (check instanceof DefenseAttribute && !ItemUtils.isArmor(item)) {
                isValid = false;
            } else if (check instanceof DamageAttribute && ItemUtils.isArmor(item)) {
                isValid = false;
            }
            isSocket = check instanceof SocketAttribute;
        }

        Map<A, double[]> stats = this.getAttributes();

        int generatorPos = lore.indexOf(this.placeholder);
        int min          = this.getMinAmount();
        int max          = isSocket ? this.getMaxAmount() : Math.min(stats.size(), this.getMaxAmount());

        // Check if Tier can have item stats
        // and remove them from lore if not
        if (generatorPos < 0 || max == 0 || stats.isEmpty() || !isValid) {
            LoreUT.replacePlaceholder(item, this.placeholder, null);
            return;
        }
        // Min: -1, Max: 5
        // Roll: <=5
        //
        // Min: -1, Max: -1
        // Roll: Unlimited
        //
        // Min: 3, Max: -1
        // Roll: >3
        //
        // Min: 2, Max: 5
        // Roll: (2, 5);
        boolean isMaxUnlimited = (max < 0);
        boolean isMinUnlimited = (min < 0);
        int     maxSize        = stats.size();
        int     rollMax        = isMaxUnlimited ? maxSize : max;
        int     rollMin        = (isMinUnlimited || (isMinUnlimited && isMaxUnlimited)) ? Rnd.get(rollMax + 1) : min;
        int     roll           = Rnd.get((isMaxUnlimited ? rollMax : rollMin), rollMax);
        // Get random amount of stats
        // in range of min and max
		
		/*System.out.println("-------------------------------------");
		System.out.println(this.placeholder);
		System.out.println("isMaxUnlimited: " + isMaxUnlimited);
		System.out.println("isMinUnlimited: " + isMinUnlimited);
		System.out.println("Max Stats Size: " + maxSize);
		System.out.println("Roll Min: " + rollMin);
		System.out.println("Roll Max: " + rollMax);
		System.out.println("Roll Total: " + roll);*/

        // If get stats number is 0
        // Remove all stat placeholders
        if (roll <= 0) {
            LoreUT.replacePlaceholder(item, this.placeholder, null);
            return;
        }

        // Create a map with a chances for each stat
        Map<A, Double> mapChance = new HashMap<>();
        stats.forEach((stat, values) -> mapChance.put(stat, values[0]));

        boolean noStats = true;
        for (int count = 0; count < roll; count++) {
            //System.out.println("Count: " + (count+1) + "/" + roll + "/" + rollMin);
            //System.out.println("Chance Map Size: " + mapChance.size());

            // If min. stats are not added yet, DEFINITELY picked one of them from a map by a chance.
            // If min. stats are ADDED, picks random stat and check it chance manually.
            @Nullable A stat;
            if (count < rollMin) { // Let's roll only 100% until we have our minimum or all the 100s are used.
                Map<A, Double> filtered = mapChance.keySet().stream()
                        .filter(a -> mapChance.get(a) >= 100D).collect(Collectors.toMap(a -> a, mapChance::get, (a1, b) -> b));
                stat = filtered.isEmpty() ? Rnd.getRandomItem(mapChance) : Rnd.getRandomItem(filtered);
            } else
                stat = Rnd.get(new ArrayList<>(mapChance.keySet()));

            if (stat == null) continue;
            //System.out.println("Stat: " + stat.getName());

            // If depended stat is not persist in config,
            // then we can't add it because we don't know it values.
            double[] values = stats.get(stat);
            if (values == null) continue;

            // Minimal stats are added, so we can process chances
            if (count >= rollMin) {
                //System.out.println("Check for chance 1");
                // Check for a chance to apply on item manually.
                if (Rnd.get(true) >= values[0]) { // isMaxUnlimited &&
                    //System.out.println("Check for chance 2");
                    // Do not remove if socket map. Sockets are duplicable stats.
                    if (!isSocket) mapChance.remove(stat);
                    continue;
                }
            }

            // A bit hacky, but this will prevent placeholder from left in the lore
            // if no stats were added.
            if (noStats) {
                for (String format : this.getLoreFormat()) {
                    generatorPos = LoreUT.addToLore(lore, generatorPos, format);
                }
                lore.remove(this.placeholder);
                meta.setLore(lore);
                item.setItemMeta(meta);
                noStats = false;
            }

            if (stat instanceof SocketAttribute) {
                SocketAttribute socketAtt = (SocketAttribute) stat;
                int             sPos      = -1; // Negative value = auto-find placeholder.
                // This needs to apply socket after the existent one.
                // Returns slot before the same socket in lore.
                if (!stat.hasPlaceholder(item)) {
                    sPos = socketAtt.getLoreIndex(item, 0);
                }
                socketAtt.add(item, socketAtt.getDefaultValue(), sPos);
                // Continue to avoid socket remove from the map.
                // We don't want to remove it, as item may have more than 1 of this.
                continue;
            } else {
                if (stat.hasPlaceholder(item)) {
                    BiFunction<Boolean, Double, Double> vMod   = generatorItem.getMaterialModifier(item, stat);
                    double                              vScale = generatorItem.getScaleOfLevel(values[3], itemLevel);
                    double                              vMin   = BonusCalculator.CALC_FULL.apply(values[1], Arrays.asList(vMod)) * vScale;//(values[1]) * vScale * (1D + vMod[1] / 100D);
                    double                              vMax   = BonusCalculator.CALC_FULL.apply(values[2], Arrays.asList(vMod)) * vScale;//(values[2]) * vScale * (1D + vMod[1] / 100D);

                    // Skip zero stats and decrease the counter to allow other stats
                    // to be generated instead of these ones.
                    if (vMin == 0 && vMax == 0) {
                        count--;
                    } else {
                        if (stat instanceof DamageAttribute) {
                            DamageAttribute dmgAtt = (DamageAttribute) stat;
                            double          rndV1  = NumberUT.round(Rnd.getDouble(vMin, vMax));
                            double          rndV2  = NumberUT.round(Rnd.getDouble(vMin, vMax));

                            double vFinMin = Math.min(rndV1, rndV2);
                            double vFinMax = Math.max(rndV1, rndV2);
                            dmgAtt.add(item, new double[]{vFinMin, vFinMax}, -1);
                        } else {
                            double vFin = NumberUT.round(Rnd.getDouble(vMin, vMax));
                            if (stat instanceof DefenseAttribute) {
                                DefenseAttribute defAtt = (DefenseAttribute) stat;
                                defAtt.add(item, vFin, -1);
                            } else if (stat instanceof SimpleStat) {
                                SimpleStat rStat = (SimpleStat) stat;
                                rStat.add(item, vFin, -1);

                                // Add depending stats.
                                AbstractStat.Type depend = rStat.getDependStat();
                                if (depend != null && rStat.isMainItem(item)) {
                                    @SuppressWarnings("unchecked")
                                    A dStat = (A) ItemStats.getStat(depend);
                                    if (dStat != null && !dStat.isApplied(item)) {
                                        mapChance.put(dStat, 100D);
                                        // Make depend stat to be 100% rolled.
                                        count--;
                                    }
                                }
                            } else if (stat instanceof DoubleStat) {
                                DoubleStat rStat = (DoubleStat) stat;
                                rStat.add(item, new double[]{vFin, vFin}, -1);
                            }
                        }
                    }
                }
            }
            mapChance.remove(stat);
        }
        if (noStats) {
            LoreUT.replacePlaceholder(item, this.placeholder, null);
        }
    }

    @NotNull
    public Map<A, double[]> getAttributes() {
        return attributes;
    }
}
