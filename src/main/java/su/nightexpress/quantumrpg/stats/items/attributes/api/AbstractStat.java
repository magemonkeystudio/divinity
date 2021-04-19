package su.nightexpress.quantumrpg.stats.items.attributes.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.BiFunction;

import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nightexpress.quantumrpg.modules.list.gems.GemManager;
import su.nightexpress.quantumrpg.modules.list.gems.GemManager.Gem;
import su.nightexpress.quantumrpg.modules.list.refine.RefineManager;
import su.nightexpress.quantumrpg.stats.bonus.BonusCalculator;
import su.nightexpress.quantumrpg.stats.bonus.BonusMap;
import su.nightexpress.quantumrpg.stats.items.ItemTags;
import su.nightexpress.quantumrpg.stats.items.api.ItemLoreStat;
import su.nightexpress.quantumrpg.stats.items.attributes.stats.SimpleStat;
import su.nightexpress.quantumrpg.utils.ItemUtils;

public abstract class AbstractStat<Z> extends ItemLoreStat<Z> {

	protected Type statType;
	private double cap;
	
	public AbstractStat(
			@NotNull Type statType,
			@NotNull String name,
			@NotNull String format,
			double cap,
			@NotNull PersistentDataType<?,Z> dataType
			) {
		super(
			statType.name(), 
			name, 
			format, 
			"%ITEM_STAT_" + statType.name() + "%", 
			ItemTags.TAG_ITEM_STAT, 
			dataType
		);
		this.statType = statType;
		this.cap = cap;
	}
	
    public static enum ItemType {
		ARMOR,
		WEAPON,
		BOTH,
		;
	}
    
    public static enum Type {

    	DIRECT_DAMAGE(ItemType.WEAPON, true, false, true),
    	AOE_DAMAGE(ItemType.WEAPON, true, false, true),
    	PVP_DAMAGE(ItemType.WEAPON, true, true, true),
    	PVE_DAMAGE(ItemType.WEAPON, true, true, true),
    	DODGE_RATE(ItemType.ARMOR, true, true, true),
    	ACCURACY_RATE(ItemType.WEAPON, true, true, true),
    	BLOCK_RATE(ItemType.BOTH, true, true, true),
    	BLOCK_DAMAGE(ItemType.ARMOR, true, true, true),
    	LOOT_RATE(ItemType.BOTH, true, true, true),
    	BURN_RATE(ItemType.WEAPON, true, true, true),
    	PVP_DEFENSE(ItemType.ARMOR, true, false, true),
    	PVE_DEFENSE(ItemType.ARMOR, true, true, true),
    	CRITICAL_RATE(ItemType.WEAPON, true, true, true),
    	CRITICAL_DAMAGE(ItemType.WEAPON, false, false, true),
    	DURABILITY(ItemType.BOTH, false, true, false),
    	MOVEMENT_SPEED(ItemType.ARMOR, true, true, true),
    	PENETRATION(ItemType.WEAPON, true, true, true),
    	ATTACK_SPEED(ItemType.BOTH, true, true, true),
    	VAMPIRISM(ItemType.WEAPON, true, true, true),
    	MAX_HEALTH(ItemType.BOTH, false, true, true),
    	BLEED_RATE(ItemType.WEAPON, true, true, true),
    	DISARM_RATE(ItemType.WEAPON, true, true, true),
    	SALE_PRICE(ItemType.BOTH, true, true, false),
    	THORNMAIL(ItemType.ARMOR, true, false, true),
    	HEALTH_REGEN(ItemType.BOTH, true, true, true),
    	MANA_REGEN(ItemType.BOTH, true, true, true),
    	;
    	
    	private ItemType type;
    	private boolean perc;
    	private boolean canNegate;
    	private boolean isGlobal;
    	
    	private Type(@NotNull ItemType type, boolean perc, boolean nega, boolean isGlobal) {
    		this.type = type;
    		this.perc = perc;
    		this.canNegate = nega;
    		this.isGlobal = isGlobal;
    	}
    	
    	@NotNull
    	public ItemType getItemType() {
    		return this.type;
    	}
    	
    	public boolean isPercent() {
    		return this.perc;
    	}
    	
    	public boolean canBeNegative() {
    		return this.canNegate;
    	}
    	
    	/**
    	 * Defines is this stat is entity-global (true) or per-item based (false).
    	 * @return
    	 */
    	public boolean isGlobal() {
			return isGlobal;
		}
    	
    	@Nullable
        public static Type getByName(@NotNull String s) {
        	try {
        		return valueOf(s.toUpperCase());
        	}
        	catch (IllegalArgumentException ex) {
        		return null;
        	}
        }
    }
    
    public static double getDefaultAttackSpeed(@NotNull ItemStack item) {
    	return plugin.getPMS().getDefaultSpeed(item);
    }
    
    @NotNull
    public AbstractStat.Type getType() {
    	return this.statType;
    }
    
    public double getCapability() {
    	return this.cap;
    }
    
    public void setCapability(double cap) {
    	this.cap = cap;
    }
    
    @NotNull
	public ItemType getItemType() {
		return this.statType.getItemType();
    }
	
	public boolean isPercent() {
		return this.statType.isPercent();
	}
	
	public boolean canBeNegative() {
		return this.statType.canBeNegative();
	}
	
	@Deprecated
	public boolean isMainItem(@NotNull ItemStack item) {
		if (this.isPercent() || this.getItemType() == ItemType.BOTH) return true;
		
		if (this.getItemType() == ItemType.ARMOR && ItemUtils.isArmor(item)) return true;
		if (this.getItemType() == ItemType.WEAPON && ItemUtils.isWeapon(item)) return true;
		
		return false;
	}
	
	@Nullable
	public Type getDependStat() {
		switch (this.statType) {
			case BLOCK_RATE: {
				return Type.BLOCK_DAMAGE;
			}
			case BLOCK_DAMAGE: {
				return Type.BLOCK_RATE;
			}
			case CRITICAL_RATE: {
				return Type.CRITICAL_DAMAGE;
			}
			case CRITICAL_DAMAGE: {
				return Type.CRITICAL_RATE;
			}
			default: {
				return null;
			}
		}
	}
	
	public double get(@NotNull ItemStack item) {
		double value = 0D;
		boolean has = false;
		
		List<BiFunction<Boolean, Double, Double>> bonuses = new ArrayList<>();
		
		if (this instanceof SimpleStat) {
			SimpleStat reg = (SimpleStat) this;
			Double d = reg.getRaw(item);
			if (d != null) {
				value = d;
				has = true;
			}
			
			// Support for Gems adding values.
			GemManager gems = plugin.getModuleCache().getGemManager();
			if (gems != null) {
				for (Entry<Gem, Integer> e : gems.getItemSockets(item)) {
					BonusMap bMap = e.getKey().getBonusMap(e.getValue());
					if (bMap == null) continue;
					
					bonuses.add(bMap.getBonus(reg));
				}
			}
		}
		else if (this instanceof DoubleStat) {
			DoubleStat reg = (DoubleStat) this;
			double[] arr = reg.getRaw(item);
			if (arr != null) {
				value = arr[0];
				has = true;
			}
		}
		
		// Support for Refined attributes.
		RefineManager refine = plugin.getModuleCache().getRefineManager();
		if (refine != null && has) {
			bonuses.add(refine.getRefinedBonus(item, this));
		}
		
		// Multiply value by additional percent bonus.
		value = BonusCalculator.CALC_FULL.apply(value, bonuses);
		
		return this.fineValue(value);
	}
	
	public double fineValue(double value) {
		if (this.statType == Type.DURABILITY) return value;
		
		if (this.getCapability() >= 0 && value > this.getCapability()) {
			value = this.getCapability();
		}
		if (value < 0 && !this.canBeNegative()) {
			value = 0;
		}
		return value;
	}
}
