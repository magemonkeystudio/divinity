package com.promcteam.divinity.stats.items.attributes;

import com.promcteam.codex.util.ItemUT;
import com.promcteam.codex.util.NumberUT;
import com.promcteam.codex.util.StringUT;
import com.promcteam.codex.util.constants.JStrings;
import com.promcteam.divinity.Divinity;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.promcteam.divinity.config.EngineCfg;
import com.promcteam.divinity.modules.list.gems.GemManager;
import com.promcteam.divinity.modules.list.gems.GemManager.Gem;
import com.promcteam.divinity.modules.list.refine.RefineManager;
import com.promcteam.divinity.stats.bonus.BonusCalculator;
import com.promcteam.divinity.stats.bonus.BonusMap;
import com.promcteam.divinity.stats.bonus.StatBonus;
import com.promcteam.divinity.stats.items.ItemStats;
import com.promcteam.divinity.stats.items.ItemTags;
import com.promcteam.divinity.stats.items.api.DuplicableItemLoreStat;
import com.promcteam.divinity.stats.items.api.DynamicStat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;

public class DefenseAttribute extends DuplicableItemLoreStat<StatBonus> implements DynamicStat<StatBonus> {

    private int         priority;
    private Set<String> blockDamageType;
    private double      protectionFactor;

    public DefenseAttribute(
            @NotNull String id,
            @NotNull String name,
            @NotNull String format,
            int priority,
            @NotNull Set<String> blockDamageType,
            double protectionFactor
    ) {
        super(id, name, format, "%DEFENSE_" + id + "%", ItemTags.TAG_ITEM_DEFENSE, StatBonus.DATA_TYPE);
        this.priority = priority;
        this.blockDamageType = blockDamageType;
        this.protectionFactor = protectionFactor;

        ItemStats.registerDynamicStat(this);

        // Legacy keys
        this.keys.add(NamespacedKey.fromString("prorpgitems:qrpg_item_defense_" + this.getId()));
        this.keys.add(NamespacedKey.fromString("quantumrpg:qrpg_item_defense_" + this.getId()));
    }

    @Override
    @NotNull
    public Class<StatBonus> getParameterClass() {
        return StatBonus.class;
    }

    public int getPriority() {
        return this.priority;
    }

    public boolean isBlockable(@NotNull DamageAttribute dmg) {
        return this.blockDamageType.contains(dmg.getId())
                || this.blockDamageType.contains(JStrings.MASK_ANY);
    }

    public double getProtectionFactor() {
        return protectionFactor;
    }

    public double getTotal(@NotNull ItemStack item, @Nullable Player player) {
        return BonusCalculator.SIMPLE_FULL.apply(0D, get(item, player));
    }

    @NotNull
    public List<BiFunction<Boolean, Double, Double>> get(@NotNull ItemStack item, @Nullable Player player) {
        List<BiFunction<Boolean, Double, Double>> bonuses = new ArrayList<>();
        double                                    base    = 0;
        double                                    percent = 0;
        boolean                                   has     = false;

        // Get from old format
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            PersistentDataContainer container = meta.getPersistentDataContainer();
            for (NamespacedKey key : this.keys) {
                if (container.has(key, PersistentDataType.DOUBLE)) {
                    Double value = container.get(key, PersistentDataType.DOUBLE);
                    if (value != null) {
                        base += value;
                        has = true;
                        break;
                    }
                }
            }
        }

        for (StatBonus bonus : this.getAllRaw(item)) {
            if (!bonus.meetsRequirement(player)) continue;
            double[] value = bonus.getValue();
            if (value.length == 1 && bonus.isPercent()) {
                percent += value[0];
            } else {
                base += value[0];
                has = true;
            }
        }

        // Add default item armor value for default defense type, if no custom defense applied
        if (base == 0 && this.isDefault() && ItemStats.getDefenses().stream()
                .filter(defenseAttribute -> !defenseAttribute.isDefault())
                .noneMatch(defenseAttribute -> ItemStats.hasDefense(item, player, defenseAttribute))) {
            base += DefenseAttribute.getVanillaArmor(item);
        }

        {
            double finalBase = base;
            bonuses.add((isPercent, input) -> isPercent ? input : input + finalBase);
            double finalPercent = percent;
            bonuses.add((isPercent, input) -> isPercent ? input + finalPercent : input);
        }

        // Support for Refine Module
        RefineManager refine = Divinity.getInstance().getModuleCache().getRefineManager();
        if (refine != null && has) {
            bonuses.add(refine.getRefinedBonus(item, this));
        }

        // Support for filled socket Gems.
        GemManager gems = Divinity.getInstance().getModuleCache().getGemManager();
        if (gems != null) {
            for (Entry<Gem, Integer> e : gems.getItemSockets(item)) {
                BonusMap bMap = e.getKey().getBonusMap(e.getValue());
                if (bMap == null) continue;

                bonuses.add(bMap.getBonus(this));
            }
        }

        return bonuses;
    }

    public static double getVanillaArmor(@NotNull ItemStack item) {
        return Divinity.getInstance().getPMS().getDefaultArmor(item);
    }

    public static double getVanillaToughness(@NotNull ItemStack item) {
        return Divinity.getInstance().getPMS().getDefaultToughness(item);
    }

    public boolean isDefault() {
        DefenseAttribute def = ItemStats.getDefenseByDefault();
        return def != null && def.getId().equalsIgnoreCase(this.getId());
    }

    @Override
    @NotNull
    public String formatValue(@NotNull ItemStack item, @NotNull StatBonus statBonus) {
        return NumberUT.format(statBonus.getValue()[0]) + (statBonus.isPercent() ? "%" : "");
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
        int amount = this.getAmount(item);
        if (amount == 0) return item;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        List<String> lore = meta.getLore();
        if (lore == null) return item;

        for (int i = 0; i < amount; i++) {
            int    loreIndex = -1;
            String metaId    = "";
            for (NamespacedKey key : this.keys) {
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
