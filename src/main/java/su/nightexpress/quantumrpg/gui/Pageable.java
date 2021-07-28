package su.nightexpress.quantumrpg.gui;

import java.util.LinkedHashMap;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.quantumrpg.QuantumRPG;

public class Pageable extends GUI {
  protected int pages;
  
  public Pageable(QuantumRPG plugin, String title, int size, LinkedHashMap<String, GUIItem> items, int pages) {
    super(plugin, title, size, items);
    this.pages = pages;
  }
  
  public int getPages() {
    return this.pages;
  }
  
  public boolean click(Player p, ItemStack item, ContentType type, int slot, InventoryClickEvent e) {
    if (!super.click(p, item, type, slot, e))
      return false; 
    if (type == ContentType.NEXT) {
      int page = GUIUtils.getPage(item);
      open(p, page + 1);
      return false;
    } 
    if (type == ContentType.BACK) {
      int page = GUIUtils.getPage(item);
      open(p, page - 1);
      return false;
    } 
    return true;
  }
  
  public void open(Player p, int page) {
    Inventory inv = build(new Object[] { Integer.valueOf(page) });
    p.openInventory(inv);
  }
  
  protected Inventory build(Object... o) {
    int page = ((Integer)o[0]).intValue();
    Inventory inv = getInventory();
    for (GUIItem gi : getContent().values()) {
      ItemStack item = gi.getItem().clone();
      if (gi.getType() == ContentType.NEXT)
        if (this.pages > 1 && page < this.pages) {
          item = GUIUtils.setPage(item, page + 1);
        } else {
          continue;
        }  
      if (gi.getType() == ContentType.BACK)
        if (page > 1) {
          item = GUIUtils.setPage(item, page - 1);
        } else {
          continue;
        }  
      byte b;
      int i, arrayOfInt[];
      for (i = (arrayOfInt = gi.getSlots()).length, b = 0; b < i; ) {
        int slot = arrayOfInt[b];
        inv.setItem(slot, item);
        b++;
      } 
    } 
    return inv;
  }
}
