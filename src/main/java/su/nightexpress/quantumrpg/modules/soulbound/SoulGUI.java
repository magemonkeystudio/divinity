package su.nightexpress.quantumrpg.modules.soulbound;

import java.util.LinkedHashMap;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.quantumrpg.config.Lang;
import su.nightexpress.quantumrpg.gui.ContentType;
import su.nightexpress.quantumrpg.gui.GUIItem;
import su.nightexpress.quantumrpg.gui.SocketGUI;
import su.nightexpress.quantumrpg.modules.QModule;
import su.nightexpress.quantumrpg.utils.Utils;

public class SoulGUI extends SocketGUI {
  public SoulGUI(QModule m, String title, int size, LinkedHashMap<String, GUIItem> items, int item_slot, int source_slot, int result_slot) {
    super(m, title, size, items, item_slot, source_slot, result_slot);
  }
  
  public boolean click(Player p, ItemStack item, ContentType type, int slot, InventoryClickEvent e) {
    Inventory inv = e.getInventory();
    if (type == ContentType.ACCEPT) {
      ItemStack result = new ItemStack(inv.getItem(getResultSlot()));
      inv.setItem(getItemSlot(), null);
      p.getInventory().addItem(new ItemStack[] { result });
      this.m.out((Entity)p, Lang.Restrictions_SoulAccept.toMsg());
      p.closeInventory();
      return false;
    } 
    if (type == ContentType.EXIT) {
      this.m.out((Entity)p, Lang.Restrictions_SoulDecline.toMsg());
      p.closeInventory();
    } 
    return false;
  }
  
  public boolean onClose(Player p, InventoryCloseEvent e) {
    Inventory inv = e.getInventory();
    ItemStack item = inv.getItem(getItemSlot());
    if (item != null)
      Utils.addItem(p, item); 
    return false;
  }
}
