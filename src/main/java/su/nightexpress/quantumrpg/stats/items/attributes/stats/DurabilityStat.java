package su.nightexpress.quantumrpg.stats.items.attributes.stats;

import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import mc.promcteam.engine.utils.random.Rnd;
import su.nightexpress.quantumrpg.api.event.RPGItemDamageEvent;
import su.nightexpress.quantumrpg.config.EngineCfg;
import su.nightexpress.quantumrpg.stats.items.ItemStats;
import su.nightexpress.quantumrpg.stats.items.attributes.api.DoubleStat;

public class DurabilityStat extends DoubleStat {

	public DurabilityStat(
			@NotNull Type statType, 
			@NotNull String name,
			@NotNull String format, 
			double cap) {
		super(statType, name, format, cap);
	}

	public boolean isUnbreakable(@NotNull ItemStack item) {
		double[] arr = this.getRaw(item);
		if (arr != null) {
			return arr[1] == -1;
		}
		else {
			return false;
		}
	}
	
	public boolean isDamaged(@NotNull ItemStack item) {
		if (!ItemStats.hasStat(item, this.statType)) {
			return false;
		}
		if (this.isUnbreakable(item)) {
			return false;
		}
		double[] durability = this.getRaw(item);
		return durability != null && durability[0] < durability[1];
	}
	
	public boolean isBroken(@NotNull ItemStack item) {
		return ItemStats.hasStat(item, this.statType) && this.get(item) == 0
				&& !EngineCfg.ATTRIBUTES_DURABILITY_BREAK_ITEMS;
	}
	
	public boolean reduceDurability(
			@NotNull LivingEntity li, @NotNull ItemStack item, int amount) {
		
		if (!(li instanceof Player) && !EngineCfg.ATTRIBUTES_DURABILITY_REDUCE_FOR_MOBS) return false;
		if (!ItemStats.hasStat(item, this.statType) || this.isUnbreakable(item)) return false;
		
		ItemMeta meta = item.getItemMeta();
		
		// Vanilla unbreaking formula
		if (meta != null && meta.hasEnchant(Enchantment.DURABILITY)) {
			double lvl = meta.getEnchantLevel(Enchantment.DURABILITY);
			double chance = (100D / (lvl + 1D));
			if (Rnd.get(true) < chance) {
				return false;
			}
		}
		
		// Stop if item is already broken
		double current = this.get(item);
		if (current == 0) return false;
		
		double[] durability = this.getRaw(item);
		if (durability == null) return false;
		
		// Custom item damage event
		RPGItemDamageEvent eve = new RPGItemDamageEvent(item, li);
		plugin.getPluginManager().callEvent(eve);
		if (eve.isCancelled()) return false;
		
		double max = durability[1];
		double lose = current - Math.min(amount, current);
	
		if (lose <= 0) {
			if (EngineCfg.ATTRIBUTES_DURABILITY_BREAK_ITEMS) {
				item.setAmount(0);
				li.getWorld().playSound(li.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.8f, 0.8f);
				return false;
			}
		}
		
		return this.add(item, new double[] {lose, max}, -1);
	}
	
	@Override
	@NotNull
	public String formatValue(@NotNull ItemStack item, double[] values) {
		return EngineCfg.getDurabilityFormat((int) values[0], (int) values[1]);
	}
}
