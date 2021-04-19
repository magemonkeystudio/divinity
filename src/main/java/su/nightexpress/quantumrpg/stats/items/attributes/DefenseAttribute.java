package su.nightexpress.quantumrpg.stats.items.attributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;

import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.utils.NumberUT;
import su.nexmedia.engine.utils.constants.JStrings;
import su.nightexpress.quantumrpg.modules.list.gems.GemManager;
import su.nightexpress.quantumrpg.modules.list.gems.GemManager.Gem;
import su.nightexpress.quantumrpg.modules.list.refine.RefineManager;
import su.nightexpress.quantumrpg.stats.bonus.BonusCalculator;
import su.nightexpress.quantumrpg.stats.bonus.BonusMap;
import su.nightexpress.quantumrpg.stats.items.ItemStats;
import su.nightexpress.quantumrpg.stats.items.ItemTags;
import su.nightexpress.quantumrpg.stats.items.api.ItemLoreStat;

public class DefenseAttribute extends ItemLoreStat<Double> {

	private int priority;
	private Set<String> blockDamageType;
	private double protectionFactor;
	
	public DefenseAttribute(
			@NotNull String id,
			@NotNull String name,
			@NotNull String format,
			int priority,
			@NotNull Set<String> blockDamageType,
			double protectionFactor
			) {
		super(id, name, format, "%DEFENSE_" + id + "%", ItemTags.TAG_ITEM_DEFENSE, PersistentDataType.DOUBLE);
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
	
	public double get(@NotNull ItemStack item) {
		double value = 0D;
		boolean has = false;
		
		List<BiFunction<Boolean, Double, Double>> bonuses = new ArrayList<>();
		
		Double rawValue = this.getRaw(item);
		if (rawValue != null) {
			value = rawValue.doubleValue();
			has = true;
		}
		
		// Support for Refine Module
		RefineManager refine = plugin.getModuleCache().getRefineManager();
		if (refine != null && has) {
			bonuses.add(refine.getRefinedBonus(item, this));
		}
		
		// Support for filled socket Gems.
		GemManager gems = plugin.getModuleCache().getGemManager();
		if (gems != null) {
			for (Entry<Gem, Integer> e : gems.getItemSockets(item)) {
				BonusMap bMap = e.getKey().getBonusMap(e.getValue());
				if (bMap == null) continue;
					
				bonuses.add(bMap.getBonus(this));
			}
		}
		
		// Multiply value by additional percent bonus.
		value = BonusCalculator.CALC_FULL.apply(value, bonuses);
		
		// Return default item armor value
		// for default defense type, if no custom defense applied
		if (value == 0D && this.isDefault()) {
			return DefenseAttribute.getVanillaArmor(item);
		}
		
		return value;
	}
	
	public static double getVanillaArmor(@NotNull ItemStack item) {
    	return plugin.getPMS().getDefaultArmor(item);
	}
	
    public static double getVanillaToughness(@NotNull ItemStack item) {
    	return plugin.getPMS().getDefaultToughness(item);
    }
	
	public boolean isDefault() {
		DefenseAttribute def = ItemStats.getDefenseByDefault();
		return def != null && def.getId().equalsIgnoreCase(this.getId());
	}

	@Override
	@NotNull
	public String formatValue(@NotNull ItemStack item, Double values) {
		return NumberUT.format(values.doubleValue());
	}
}
