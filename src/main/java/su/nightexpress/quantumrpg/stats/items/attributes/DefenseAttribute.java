package su.nightexpress.quantumrpg.stats.items.attributes;

import mc.promcteam.engine.utils.NumberUT;
import mc.promcteam.engine.utils.constants.JStrings;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.modules.list.gems.GemManager;
import su.nightexpress.quantumrpg.modules.list.gems.GemManager.Gem;
import su.nightexpress.quantumrpg.modules.list.refine.RefineManager;
import su.nightexpress.quantumrpg.stats.bonus.BonusCalculator;
import su.nightexpress.quantumrpg.stats.bonus.BonusMap;
import su.nightexpress.quantumrpg.stats.items.ItemStats;
import su.nightexpress.quantumrpg.stats.items.ItemTags;
import su.nightexpress.quantumrpg.stats.items.api.DuplicableItemLoreStat;
import su.nightexpress.quantumrpg.stats.items.attributes.api.StatBonus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;

public class DefenseAttribute extends DuplicableItemLoreStat<StatBonus> {

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
        double  base    = 0;
        double  percent = 0;
        boolean has     = false;

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
            if (!bonus.meetsRequirements(player)) continue;
            double[] value = bonus.getValue();
            if (value.length == 1) {
                if (bonus.isPercent()) percent += value[0];
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
        RefineManager refine = QuantumRPG.getInstance().getModuleCache().getRefineManager();
        if (refine != null && has) {
            bonuses.add(refine.getRefinedBonus(item, this));
        }

        // Support for filled socket Gems.
        GemManager gems = QuantumRPG.getInstance().getModuleCache().getGemManager();
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
        return QuantumRPG.getInstance().getPMS().getDefaultArmor(item);
    }

    public static double getVanillaToughness(@NotNull ItemStack item) {
        return QuantumRPG.getInstance().getPMS().getDefaultToughness(item);
    }

    public boolean isDefault() {
        DefenseAttribute def = ItemStats.getDefenseByDefault();
        return def != null && def.getId().equalsIgnoreCase(this.getId());
    }

    @Override
    @NotNull
    public String formatValue(@NotNull ItemStack item, @NotNull StatBonus statBonus) {
        return NumberUT.format(statBonus.getValue()[0])+(statBonus.isPercent() ? "%" : "");
    }
}
