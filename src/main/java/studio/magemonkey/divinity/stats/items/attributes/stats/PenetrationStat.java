package studio.magemonkey.divinity.stats.items.attributes.stats;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.magemonkey.codex.util.ItemUT;
import studio.magemonkey.codex.util.NumberUT;
import studio.magemonkey.codex.util.StringUT;
import studio.magemonkey.divinity.config.EngineCfg;
import studio.magemonkey.divinity.stats.bonus.BonusCalculator;
import studio.magemonkey.divinity.stats.bonus.StatBonus;
import studio.magemonkey.divinity.stats.items.ItemStats;
import studio.magemonkey.divinity.stats.items.ItemTags;
import studio.magemonkey.divinity.stats.items.api.DuplicableItemLoreStat;
import studio.magemonkey.divinity.stats.items.api.DynamicStat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * A configurable penetration stat read from penetration.yml.
 *
 * <ul>
 *   <li>{@code percent-pen: true}  — reduces the victim's effective defense by a percentage.
 *       Works with LEGACY, CUSTOM and FACTOR defense formulas.</li>
 *   <li>{@code percent-pen: false} — reduces the victim's effective defense by a flat value.
 *       Only applied under the CUSTOM defense formula; ignored for LEGACY/FACTOR.</li>
 * </ul>
 *
 * Each stat declares {@code hooks} — a list of damage-type IDs (matching damage.yml keys)
 * that this penetration applies to.
 */
public class PenetrationStat extends DuplicableItemLoreStat<StatBonus> implements DynamicStat<StatBonus> {

    @Getter private final String      penId;
    @Getter private final Set<String> hooks;       // damage-type IDs this pen applies to
    @Getter private final boolean     percentPen;  // true = %, false = flat
    @Getter private final double      capacity;

    public PenetrationStat(
            @NotNull String penId,
            @NotNull String name,
            @NotNull String format,
            @NotNull Set<String> hooks,
            boolean percentPen,
            double capacity
    ) {
        super(
                "penetration_" + penId.toLowerCase(),
                name,
                format,
                "%PENETRATION_" + penId.toUpperCase() + "%",
                ItemTags.TAG_ITEM_PENETRATION,
                StatBonus.DATA_TYPE
        );
        this.penId      = penId.toLowerCase();
        this.hooks      = hooks;
        this.percentPen = percentPen;
        this.capacity   = capacity;

        ItemStats.registerPenetration(this);
        ItemStats.registerDynamicStat(this);
    }

    @Override
    @NotNull
    public Class<StatBonus> getParameterClass() {
        return StatBonus.class;
    }

    /** Returns true if this penetration applies to the given damage type. */
    public boolean isApplicableTo(@NotNull String damageTypeId) {
        return this.hooks.contains(damageTypeId.toLowerCase());
    }

    public double getTotal(@NotNull ItemStack item, @Nullable Player player) {
        return BonusCalculator.SIMPLE_FULL.apply(0D, get(item, player));
    }

    @NotNull
    public List<BiFunction<Boolean, Double, Double>> get(@NotNull ItemStack item, @Nullable Player player) {
        List<BiFunction<Boolean, Double, Double>> bonuses = new ArrayList<>();
        double base    = 0;
        double percent = 0;

        for (StatBonus bonus : this.getAllRaw(item)) {
            if (!bonus.meetsRequirement(player)) continue;
            double[] value = bonus.getValue();
            if (value.length == 1 && bonus.isPercent()) {
                percent += value[0];
            } else {
                base += value[0];
            }
        }

        final double fb = base;
        final double fp = percent;
        bonuses.add((isPercent, input) -> isPercent ? input : input + fb);
        bonuses.add((isPercent, input) -> isPercent ? input + fp : input);

        return bonuses;
    }

    @Override
    @NotNull
    public String getFormat(@Nullable Player p, @NotNull ItemStack item, @NotNull StatBonus value) {
        StatBonus.Condition<?> condition = value.getCondition();
        return StringUT.colorFix(super.getFormat(item, value)
                .replace("%condition%", condition == null || !EngineCfg.LORE_STYLE_REQ_USER_DYN_UPDATE
                        ? ""
                        : condition.getFormat(p, item)));
    }

    @Override
    @NotNull
    public ItemStack updateItem(@Nullable Player p, @NotNull ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        int amount = this.getAmount(item);
        if (amount == 0) return item;
        List<String> lore = meta.getLore();
        if (lore == null) return item;

        for (int i = 0; i < amount; i++) {
            int    loreIndex = -1;
            String metaId    = "";
            for (org.bukkit.NamespacedKey key : this.keys) {
                metaId = key.getKey() + i;
                loreIndex = ItemUT.getLoreIndex(item, metaId);
                if (loreIndex >= 0) break;
            }
            if (loreIndex < 0) continue;

            @Nullable StatBonus arr = this.getRaw(item, i);
            if (arr == null) continue;
            String formatNew = this.getFormat(p, item, arr);
            lore.set(loreIndex, formatNew);
            meta.setLore(lore);
            item.setItemMeta(meta);
            ItemUT.addLoreTag(item, metaId, formatNew);
        }

        return item;
    }

    @Override
    @NotNull
    public String formatValue(@NotNull ItemStack item, @NotNull StatBonus statBonus) {
        String sVal = NumberUT.format(statBonus.getValue()[0]);
        if (statBonus.isPercent()) {
            sVal += EngineCfg.LORE_CHAR_PERCENT;
        }
        return sVal;
    }
}
