package su.nightexpress.quantumrpg.modules;

import org.bukkit.inventory.ItemStack;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.modules.cmds.MExecutor;
import su.nightexpress.quantumrpg.utils.NBTUtils;

public abstract class QModuleLevel extends QModuleDrop {
  public QModuleLevel(QuantumRPG plugin, boolean enabled, MExecutor exec) {
    super(plugin, enabled, exec);
  }
  
  public int getLevel(ItemStack item) {
    return NBTUtils.getItemLevel(item);
  }
  
  public boolean isInLevelRange(ItemStack target, ItemStack item) {
    if (!isItemOfThisModule(item))
      return false; 
    int lvl2 = getLevel(item);
    String id = getItemId(item);
    LeveledItem li = getItemById(id, LeveledItem.class);
    if (li == null)
      return false; 
    if (!li.hasLevelRequirements())
      return true; 
    int min = li.getMinLevelRequirement(lvl2);
    int max = li.getMaxLevelRequirement(lvl2);
    int lvl = getLevel(target);
    if (lvl == -1)
      return false; 
    if (min == max)
      return (lvl >= min); 
    return (lvl >= min && lvl <= max);
  }
  
  public void unload() {
    super.unload();
  }
}
