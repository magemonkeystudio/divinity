package su.nightexpress.quantumrpg.modules.refine;

import java.util.LinkedHashMap;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.quantumrpg.config.Lang;
import su.nightexpress.quantumrpg.gui.ContentType;
import su.nightexpress.quantumrpg.gui.GUIItem;
import su.nightexpress.quantumrpg.gui.SocketGUI;
import su.nightexpress.quantumrpg.modules.QModule;
import su.nightexpress.quantumrpg.modules.QModuleRate;
import su.nightexpress.quantumrpg.utils.Utils;

public class RefineGUI extends SocketGUI {
  public RefineGUI(QModuleRate m, String title, int size, LinkedHashMap<String, GUIItem> items, int item_slot, int source_slot, int result_slot) {
    super((QModule)m, title, size, items, item_slot, source_slot, result_slot);
  }
  
  public boolean click(Player p, ItemStack item, ContentType type, int slot, InventoryClickEvent e) {
    Inventory inv = e.getInventory();
    RefineManager m = (RefineManager)this.m;
    if (type == ContentType.ACCEPT) {
      ItemStack target = new ItemStack(inv.getItem(this.item_slot));
      ItemStack gem = new ItemStack(inv.getItem(this.source_slot));
      ItemStack result = new ItemStack(inv.getItem(this.result_slot));
      inv.setItem(this.item_slot, null);
      inv.setItem(this.source_slot, null);
      int chance = m.getSocketRate(gem);
      if (chance < Utils.randInt(0, 100)) {
        Utils.addItem(p, m.downgradeItem(target));
        m.out((Entity)p, Lang.Refine_Enchanting_Failure.toMsg());
        p.closeInventory();
        m.playEffects(p, false);
        return false;
      } 
      Utils.addItem(p, result);
      m.out((Entity)p, Lang.Refine_Enchanting_Success.toMsg());
      p.closeInventory();
      m.playEffects(p, true);
      return false;
    } 
    if (type == ContentType.EXIT) {
      m.out((Entity)p, Lang.Refine_Enchanting_Cancel.toMsg());
      p.closeInventory();
    } 
    return false;
  }
}
