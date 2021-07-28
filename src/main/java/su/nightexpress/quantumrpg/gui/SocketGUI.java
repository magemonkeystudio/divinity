package su.nightexpress.quantumrpg.gui;

import java.util.LinkedHashMap;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.quantumrpg.modules.QModule;
import su.nightexpress.quantumrpg.utils.Utils;

public class SocketGUI extends GUI {
  protected QModule m;
  
  protected int item_slot;
  
  protected int source_slot;
  
  protected int result_slot;
  
  public SocketGUI(QModule m, String title, int size, LinkedHashMap<String, GUIItem> items, int item_slot, int source_slot, int result_slot) {
    super(m.pl(), title, size, items);
    this.m = m;
    this.item_slot = item_slot;
    this.source_slot = source_slot;
    this.result_slot = result_slot;
  }
  
  public int getItemSlot() {
    return this.item_slot;
  }
  
  public int getSourceSlot() {
    return this.source_slot;
  }
  
  public int getResultSlot() {
    return this.result_slot;
  }
  
  public void openSocketing(Player p, ItemStack target, ItemStack src, ItemStack result) {
    p.openInventory(build(new Object[] { target, src, result }));
  }
  
  public Inventory build(Object... val) {
    ItemStack item = (ItemStack)val[0];
    ItemStack gem = (ItemStack)val[1];
    ItemStack result = (ItemStack)val[2];
    Inventory inv = getInventory();
    for (GUIItem gi : this.items.values()) {
      byte b;
      int i;
      int[] arrayOfInt;
      for (i = (arrayOfInt = gi.getSlots()).length, b = 0; b < i; ) {
        int j = arrayOfInt[b];
        inv.setItem(j, gi.getItem());
        b++;
      } 
    } 
    inv.setItem(this.item_slot, item);
    inv.setItem(this.source_slot, gem);
    inv.setItem(this.result_slot, result);
    return inv;
  }
  
  public boolean onClose(Player p, InventoryCloseEvent e) {
    Inventory inv = e.getInventory();
    ItemStack item = inv.getItem(getItemSlot());
    ItemStack src = inv.getItem(getSourceSlot());
    if (item != null)
      Utils.addItem(p, item); 
    if (src != null)
      Utils.addItem(p, src); 
    return false;
  }
}
