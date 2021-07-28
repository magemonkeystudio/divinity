package su.nightexpress.quantumrpg.modules;

import java.util.List;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.modules.cmds.MExecutor;

public abstract class QModuleSocket extends QModuleRate {
  protected SocketSettings ss;
  
  public QModuleSocket(QuantumRPG plugin, boolean enabled, MExecutor exec) {
    super(plugin, enabled, exec);
  }
  
  protected abstract void setupSettings();
  
  public abstract List<String> getFilledSocketKeys(ItemStack paramItemStack);
  
  public final boolean hasFreeSlot(ItemStack item) {
    if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore())
      return false; 
    ItemMeta meta = item.getItemMeta();
    return meta.getLore().contains(this.ss.getEmptySlot());
  }
  
  public final int getEmptySlotIndex(ItemStack item) {
    if (!hasFreeSlot(item))
      return -1; 
    ItemMeta meta = item.getItemMeta();
    return meta.getLore().indexOf(this.ss.getEmptySlot());
  }
  
  public SocketSettings getSettings() {
    return this.ss;
  }
  
  public void unload() {
    super.unload();
    this.ss = null;
  }
}
