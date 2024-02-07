package su.nightexpress.quantumrpg.stats.items.attributes;

import mc.promcteam.engine.utils.DataUT;
import mc.promcteam.engine.utils.NumberUT;
import mc.promcteam.engine.utils.actions.ActionManipulator;
import mc.promcteam.engine.utils.constants.JStrings;
import mc.promcteam.engine.utils.random.Rnd;
import org.bukkit.block.Biome;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.config.EngineCfg;
import su.nightexpress.quantumrpg.modules.list.gems.GemManager;
import su.nightexpress.quantumrpg.modules.list.gems.GemManager.Gem;
import su.nightexpress.quantumrpg.modules.list.refine.RefineManager;
import su.nightexpress.quantumrpg.stats.bonus.BonusCalculator;
import su.nightexpress.quantumrpg.stats.bonus.BonusMap;
import su.nightexpress.quantumrpg.stats.items.ItemStats;
import su.nightexpress.quantumrpg.stats.items.ItemTags;
import su.nightexpress.quantumrpg.stats.items.api.ItemLoreStat;
import su.nightexpress.quantumrpg.utils.ItemUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;

public class DamageAttribute extends ItemLoreStat<double[]> {

    private int                 priority;
    private ActionManipulator   actionEngine;
    private Set<String>         attachedDamageCauses;
    private Map<String, Double> biomeModifier;
    private Map<String, Double> entityTypeModifier;
    private Map<String, Double> mythicFactionModifier;
    private DefenseAttribute    defenseAttached;

    public DamageAttribute(
            @NotNull String id,
            @NotNull String name,
            @NotNull String format,
            int priority,
            @NotNull ActionManipulator actionEngine,
            @NotNull Set<String> attachedDamageCauses,
            @NotNull Map<String, Double> biome,
            @NotNull Map<String, Double> entityTypeModifier,
            @NotNull Map<String, Double> mythicFactionModifier
    ) {
        super(id, name, format, "%DAMAGE_" + id + "%", ItemTags.TAG_ITEM_DAMAGE, DataUT.DOUBLE_ARRAY);
        this.priority = priority;
        this.actionEngine = actionEngine;
        this.attachedDamageCauses = attachedDamageCauses;
        this.biomeModifier = biome;
        this.entityTypeModifier = entityTypeModifier;
        this.mythicFactionModifier = mythicFactionModifier;
        this.defenseAttached = null;
    }

    public boolean isDefault() {
        return this.equals(ItemStats.getDamageByDefault());
    }

    public int getPriority() {
        return this.priority;
    }

    public boolean isAttached(@NotNull DamageCause cause) {
        return this.attachedDamageCauses.contains(cause.name())
                || this.attachedDamageCauses.contains(JStrings.MASK_ANY);
    }

    public boolean isAttached(@NotNull DefenseAttribute def) {
        return this.defenseAttached != null && this.defenseAttached.getId().equalsIgnoreCase(def.getId());
    }

    public void setAttachedDefense(@Nullable DefenseAttribute defense) {
        this.defenseAttached = defense;
    }

    @Nullable
    public DefenseAttribute getAttachedDefense() {
        return this.defenseAttached;
    }

    public static double getVanillaDamage(@NotNull ItemStack item) {
        return QuantumRPG.getInstance().getPMS().getDefaultDamage(item);
    }

    public double getMinOrMax(@NotNull ItemStack item, int i) {
        double   value = 0D;
        double[] arr   = this.getRaw(item);
        if (arr != null) {
            value = arr[i];
        }

        if (value == 0D && this.isDefault()) {
            value = DamageAttribute.getVanillaDamage(item);
        }

        return value;
    }

    public double get(@NotNull ItemStack item) {
        double  min = 0D;
        double  max = 0D;
        boolean has = false;

        double[] arr = this.getRaw(item);
        // Item has attached damage type
        if (arr != null) {
            min = arr[0];
            max = arr[1];
            has = true;
        } else {
            if (ItemUtils.isWeapon(item)) {
                if (this.isDefault() && !ItemStats.hasDamage(item)) {
                    min = max = DamageAttribute.getVanillaDamage(item);
                    has = true;
                }
            }
        }

        List<BiFunction<Boolean, Double, Double>> bonuses = new ArrayList<>();

        // Support for Refined attributes.
        RefineManager refineManager = QuantumRPG.getInstance().getModuleCache().getRefineManager();
        if (refineManager != null && has) {
            bonuses.add(refineManager.getRefinedBonus(item, this));
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

        // Multiply values by additional percent bonus.
        min = BonusCalculator.CALC_FULL.apply(min, bonuses);
        max = BonusCalculator.CALC_FULL.apply(max, bonuses);

        if (max == 0D) max = min; // Fix for single damage value. Useless for 5.0?

        return Rnd.getDouble(min, max);
    }

    @NotNull
    public ActionManipulator getHitActions() {
        return this.actionEngine;
    }

    @NotNull
    public Map<String, Double> getBiomeDamageModifiers() {
        return this.biomeModifier;
    }

    public double getDamageModifierByBiome(@NotNull Biome b) {
        return this.biomeModifier.getOrDefault(b.name(), 1D);
    }

    public double getDamageModifierByEntityType(@NotNull Entity e) {
        return this.entityTypeModifier.getOrDefault(e.getType().name(), 1D);
    }

    public double getDamageModifierByMythicFaction(@Nullable String faction) {
        if (faction == null) return 1D;
        return this.mythicFactionModifier.getOrDefault(faction.toLowerCase(), 1D);
    }

    @Override
    @NotNull
    public String formatValue(@NotNull ItemStack item, double[] values) {
        double min    = values[0];
        double max    = values[1];
        String format = (min == max) ? EngineCfg.LORE_STYLE_DAMAGE_FORMAT_SINGLE : EngineCfg.LORE_STYLE_DAMAGE_FORMAT_RANGE;

        return format
                .replace("%value%", NumberUT.format(min))
                .replace("%max%", NumberUT.format(max))
                .replace("%min%", NumberUT.format(min));
    }
}
