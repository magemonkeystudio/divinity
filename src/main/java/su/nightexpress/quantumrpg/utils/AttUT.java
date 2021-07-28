package su.nightexpress.quantumrpg.utils;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.api.ItemAPI;
import su.nightexpress.quantumrpg.modules.EModule;
import su.nightexpress.quantumrpg.modules.gems.GemManager;
import su.nightexpress.quantumrpg.modules.sets.SetManager;
import su.nightexpress.quantumrpg.stats.ItemStat;
import su.nightexpress.quantumrpg.types.BonusType;

public class AttUT {
  private static QuantumRPG plugin = QuantumRPG.instance;
  
  public static double getItemStatValue(ItemStack item, ItemStat type, Player p) {
    GemManager gems = null;
    SetManager sets = null;
    if (EModule.GEMS.isEnabled())
      gems = (GemManager)plugin.getModule(GemManager.class); 
    if (p != null && EModule.SETS.isEnabled())
      sets = (SetManager)plugin.getModule(SetManager.class); 
    double raw = ItemAPI.getStatOnItem(item, type);
    double value = 0.0D;
    double bonus = 0.0D;
    if (gems != null) {
      value += gems.getItemGemStat(item, type, false);
      bonus += gems.getItemGemStat(item, type, true);
    } 
    if (sets != null) {
      SetManager.ItemSet set = sets.getItemSet(item);
      if (set != null) {
        value += sets.getSetBonus((LivingEntity)p, set, BonusType.ITEM_STAT, type.name(), false);
        bonus += sets.getSetBonus((LivingEntity)p, set, BonusType.ITEM_STAT, type.name(), true);
      } 
    } 
    if (type.isMainItem(item)) {
      value += raw;
    } else {
      bonus += raw;
    } 
    if (bonus != 0.0D)
      value *= 1.0D + bonus / 100.0D; 
    return value;
  }
}
