package su.nightexpress.quantumrpg.modules;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.gui.SocketGUI;
import su.nightexpress.quantumrpg.modules.cmds.MExecutor;
import su.nightexpress.quantumrpg.nbt.NBTItem;
import su.nightexpress.quantumrpg.utils.Utils;

public abstract class QModuleRate extends QModuleLevel {
  protected SocketGUI gui;
  
  public QModuleRate(QuantumRPG plugin, boolean enabled, MExecutor exec) {
    super(plugin, enabled, exec);
  }
  
  public abstract ItemStack extractSocket(ItemStack paramItemStack, int paramInt);
  
  public abstract ItemStack insertSocket(ItemStack paramItemStack1, ItemStack paramItemStack2);
  
  public final void startSocketing(Player p, ItemStack target, ItemStack src) {
    p.getInventory().removeItem(new ItemStack[] { target });
    if (src.getAmount() > 1) {
      ItemStack gem2 = new ItemStack(src);
      gem2.setAmount(src.getAmount() - 1);
      src.setAmount(1);
      Utils.addItem(p, gem2);
    } 
    ItemStack result = insertSocket(new ItemStack(target), new ItemStack(src));
    this.gui.openSocketing(p, target, src, result);
  }
  
  public int getSocketRate(ItemStack item) {
    if (item == null)
      return -1; 
    NBTItem nbt = new NBTItem(item);
    if (nbt.hasKey("E_ITEM_RATE").booleanValue())
      return nbt.getInteger("E_ITEM_RATE").intValue(); 
    return -1;
  }
  
  public void unload() {
    super.unload();
    if (this.gui != null) {
      this.gui.shutdown();
      this.gui = null;
    } 
  }
}
