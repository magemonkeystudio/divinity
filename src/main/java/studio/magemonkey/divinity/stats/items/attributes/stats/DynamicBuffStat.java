package studio.magemonkey.divinity.stats.items.attributes.stats;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
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

import java.util.*;
import java.util.function.BiFunction;

public class DynamicBuffStat extends DuplicableItemLoreStat<StatBonus> implements DynamicStat<StatBonus> {

    public enum BuffTarget { DAMAGE, DEFENSE }

    @Getter
    private final BuffTarget buffTarget;
    @Getter
    private final String     buffId;
    @Getter
    private final Set<String> hooks;
    @Getter
    private final double     capacity;

    public DynamicBuffStat(
            @NotNull BuffTarget buffTarget,
            @NotNull String buffId,
            @NotNull String name,
            @NotNull String format,
            @NotNull Set<String> hooks,
            double capacity
    ) {
        super(
                buffTarget.name().toLowerCase() + "_buff_" + buffId.toLowerCase(),
                name,
                format,
                "%" + buffTarget.name() + "_BUFF_" + buffId + "%",
                buffTarget == BuffTarget.DAMAGE
                        ? ItemTags.TAG_ITEM_DAMAGE_BUFF
                        : ItemTags.TAG_ITEM_DEFENSE_BUFF,
                StatBonus.DATA_TYPE
        );
        this.buffTarget = buffTarget;
        this.buffId = buffId.toLowerCase();
        this.hooks = hooks;
        this.capacity = capacity;

        if (buffTarget == BuffTarget.DAMAGE) {
            ItemStats.registerDamageBuff(this);
        } else {
            ItemStats.registerDefenseBuff(this);
        }
        ItemStats.registerDynamicStat(this);
    }

    @Override
    @NotNull
    public Class<StatBonus> getParameterClass() {
        return StatBonus.class;
    }

    public boolean isApplicableTo(@NotNull String typeId) {
        return this.hooks.contains(typeId.toLowerCase());
    }

    public double getTotal(@NotNull ItemStack item, @Nullable Player player) {
        return BonusCalculator.SIMPLE_ADDITIVE.apply(0D, get(item, player));
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

        {
            double finalBase = base;
            bonuses.add((isPercent, input) -> isPercent ? input : input + finalBase);
            double finalPercent = percent;
            bonuses.add((isPercent, input) -> isPercent ? input + finalPercent : input);
        }

        return bonuses;
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
}
