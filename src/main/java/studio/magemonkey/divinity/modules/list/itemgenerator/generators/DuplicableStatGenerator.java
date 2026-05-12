package studio.magemonkey.divinity.modules.list.itemgenerator.generators;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.config.api.JYML;
import studio.magemonkey.codex.util.NumberUT;
import studio.magemonkey.codex.util.StringUT;
import studio.magemonkey.codex.util.random.Rnd;
import studio.magemonkey.divinity.Divinity;
import studio.magemonkey.divinity.modules.list.itemgenerator.ItemGeneratorManager;
import studio.magemonkey.divinity.modules.list.itemgenerator.api.AbstractAttributeGenerator;
import studio.magemonkey.divinity.modules.list.itemgenerator.api.DamageInformation;
import studio.magemonkey.divinity.stats.bonus.BonusCalculator;
import studio.magemonkey.divinity.stats.bonus.StatBonus;
import studio.magemonkey.divinity.stats.items.api.DuplicableItemLoreStat;
import studio.magemonkey.divinity.stats.items.api.ItemLoreStat;
import studio.magemonkey.divinity.utils.LoreUT;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Generator for DuplicableItemLoreStat subtypes (DynamicBuffStat, PenetrationStat).
 * Each stat independently rolls against its own chance — no global min/max pool.
 */
public class DuplicableStatGenerator<T extends DuplicableItemLoreStat<StatBonus>> extends AbstractAttributeGenerator {

    private final Map<T, DamageInformation> attributes;

    public DuplicableStatGenerator(
            @NotNull Divinity plugin,
            @NotNull ItemGeneratorManager.GeneratorItem generatorItem,
            @NotNull String basePath,
            @NotNull String listSection,
            @NotNull Collection<T> attributesAll,
            @NotNull Function<T, String> idExtractor,
            @NotNull String placeholder
    ) {
        super(plugin, generatorItem, placeholder);

        JYML cfg = generatorItem.getConfig();

        String loreFormatKey = basePath + listSection + ".lore-format";
        this.loreFormat = StringUT.color(cfg.getStringList(loreFormatKey));
        this.attributes = new LinkedHashMap<>();

        for (T att : attributesAll) {
            String path2 = basePath + listSection + "." + idExtractor.apply(att) + ".";

            cfg.addMissing(path2 + "chance", 0D);
            cfg.addMissing(path2 + "scale-by-level", 1D);
            cfg.addMissing(path2 + "min", 0D);
            cfg.addMissing(path2 + "max", 0D);
            cfg.addMissing(path2 + "flat-range", false);
            cfg.addMissing(path2 + "round", false);

            if (!this.loreFormat.contains(att.getPlaceholder())) {
                this.loreFormat.add(att.getPlaceholder());
                cfg.set(loreFormatKey, this.loreFormat);
            }

            double  chance      = cfg.getDouble(path2 + "chance", 0D);
            double  m1          = cfg.getDouble(path2 + "min", 0D);
            double  m2          = cfg.getDouble(path2 + "max", 0D);
            if (m1 > m2) { double t = m1; m1 = m2; m2 = t; }
            double  scale       = cfg.getDouble(path2 + "scale-by-level", 1D);
            boolean flatRange   = cfg.getBoolean(path2 + "flat-range", false);
            boolean roundValues = cfg.getBoolean(path2 + "round", false);

            this.attributes.put(att, new DamageInformation(chance, m1, m2, scale, flatRange, roundValues));
        }
    }

    @Override
    public void generate(@NotNull ItemStack item, int itemLevel) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        List<String> lore = meta.getLore();
        if (lore == null) return;

        int generatorPos = lore.indexOf(this.placeholder);
        if (generatorPos < 0) return;

        // Roll each stat independently against its own chance
        List<T> toApply = new ArrayList<>();
        for (Map.Entry<T, DamageInformation> entry : this.attributes.entrySet()) {
            DamageInformation info = entry.getValue();
            if (info.getChance() <= 0) continue;
            if (Rnd.get(true) < info.getChance()) {
                toApply.add(entry.getKey());
            }
        }

        if (toApply.isEmpty()) {
            LoreUT.replacePlaceholder(item, this.placeholder, null);
            return;
        }

        // Insert lore-format (stat placeholders) and remove the generator marker
        for (String format : this.getLoreFormat()) {
            generatorPos = LoreUT.addToLore(lore, generatorPos, format);
        }
        lore.remove(this.placeholder);
        meta.setLore(lore);
        item.setItemMeta(meta);

        // Generate and write values for each rolled stat
        for (T stat : toApply) {
            if (!stat.hasPlaceholder(item)) continue;

            DamageInformation info = this.attributes.get(stat);
            if (info == null) continue;

            BiFunction<Boolean, Double, Double> vMod =
                    generatorItem.getMaterialModifiers(item, (ItemLoreStat<?>) stat);

            double vScale = generatorItem.getScaleOfLevel(info.getScaleByLevel(), itemLevel);
            double vMin   = BonusCalculator.SIMPLE_FULL.apply(info.getMin(), Arrays.asList(vMod)) * vScale;
            double vMax   = BonusCalculator.SIMPLE_FULL.apply(info.getMax(), Arrays.asList(vMod)) * vScale;
            double vFin   = NumberUT.round(Rnd.getDouble(vMin, vMax));
            if (info.isRound()) {
                vFin = Math.round(vFin);
            }

            if (vFin != 0) {
                stat.add(item, new StatBonus(new double[]{vFin}, false, null), -1);
            }

            for (StatBonus bonus : generatorItem.getClassBonuses((ItemLoreStat<?>) stat)) {
                stat.add(item, bonus, -1);
            }
            for (StatBonus bonus : generatorItem.getRarityBonuses((ItemLoreStat<?>) stat)) {
                stat.add(item, bonus, -1);
            }
            for (StatBonus bonus : generatorItem.getMaterialBonuses(item, (ItemLoreStat<?>) stat)) {
                stat.add(item, bonus, -1);
            }
        }
    }
}
