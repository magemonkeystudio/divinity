package su.nightexpress.quantumrpg.modules.resolve;

import java.util.LinkedHashMap;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.config.Lang;
import su.nightexpress.quantumrpg.gui.ContentType;
import su.nightexpress.quantumrpg.gui.GUI;
import su.nightexpress.quantumrpg.gui.GUIItem;
import su.nightexpress.quantumrpg.utils.Utils;

public class ResolveGUI extends GUI {
  protected ResolveManager m;
  
  protected int item_slot;
  
  protected int source_slot;
  
  protected int[] result_slots;
  
  public ResolveGUI(ResolveManager m, String title, int size, LinkedHashMap<String, GUIItem> items, int item_slot, int source_slot, int[] result_slots) {
    super(m.pl(), title, size, items);
    this.m = m;
    this.item_slot = item_slot;
    this.source_slot = source_slot;
    this.result_slots = result_slots;
  }
  
  public int getItemSlot() {
    return this.item_slot;
  }
  
  public int getSourceSlot() {
    return this.source_slot;
  }
  
  public int[] getResultSlots() {
    return this.result_slots;
  }
  
  public void openResolveGUI(Player p, ItemStack item, ItemStack source) {
    if (item == null) {
      item = new ItemStack(Material.AIR);
    } else {
      p.getInventory().removeItem(new ItemStack[] { item });
    } 
    if (source == null) {
      source = new ItemStack(Material.HOPPER);
    } else if (source.getAmount() > 1) {
      ItemStack gem2 = new ItemStack(source);
      gem2.setAmount(source.getAmount() - 1);
      source.setAmount(1);
      Utils.addItem(p, gem2);
    } 
    p.openInventory(build(new Object[] { item, source }));
  }
  
  public Inventory build(Object... val) {
    ItemStack target = (ItemStack)val[0];
    ItemStack src = (ItemStack)val[1];
    Inventory inv = getInventory();
    for (GUIItem gi : this.items.values()) {
      byte b;
      int j;
      int[] arrayOfInt;
      for (j = (arrayOfInt = gi.getSlots()).length, b = 0; b < j; ) {
        int k = arrayOfInt[b];
        ItemStack item = gi.getItem();
        inv.setItem(k, item);
        b++;
      } 
    } 
    inv.setItem(getItemSlot(), target);
    inv.setItem(getSourceSlot(), src);
    List<ResolveManager.SourceItem> list = this.m.getSource(target);
    for (int i = 0; i < this.result_slots.length && 
      list.size() > i; i++) {
      ItemStack pp = ((ResolveManager.SourceItem)list.get(i)).getPreview();
      inv.setItem(this.result_slots[i], pp);
    } 
    return inv;
  }
  
  public boolean click(Player p, ItemStack item, ContentType type, int slot, InventoryClickEvent e) {
    Inventory inv = e.getInventory();
    ItemStack target = getItem(inv, getItemSlot());
    ItemStack gem = getItem(inv, getSourceSlot());
    if (slot >= inv.getSize() && target.getType() == Material.AIR) {
      if (!this.m.canResolve(item)) {
        this.m.out((Entity)p, Lang.Resolve_Invalid.toMsg().replace("%item%", Utils.getItemName(item)));
        return false;
      } 
      inv.setItem(getSourceSlot(), null);
      this.m.openResolveGUI(p, item, gem);
      p.getInventory().removeItem(new ItemStack[] { item });
      return false;
    } 
    if (slot < inv.getSize() && slot == getItemSlot() && target.getType() != Material.AIR) {
      inv.setItem(getSourceSlot(), null);
      this.m.openResolveGUI(p, (ItemStack)null, gem);
      return false;
    } 
    if (type == ContentType.ACCEPT) {
      if (!this.m.canResolve(target))
        return false; 
      inv.setItem(getItemSlot(), null);
      inv.setItem(getSourceSlot(), null);
      List<ResolveManager.SourceItem> list = this.m.getSource(target);
      for (int j = 0; j < target.getAmount(); j++) {
        for (ResolveManager.SourceItem si : list) {
          if (Utils.getRandDouble(0.0D, 100.0D) > si.getChance())
            continue; 
          ItemStack i = si.getItem();
          if (i != null && i.getType() != Material.AIR)
            Utils.addItem(p, i); 
          for (String cmd : si.getCommands())
            ((QuantumRPG)this.plugin).getServer().dispatchCommand((CommandSender)((QuantumRPG)this.plugin).getServer().getConsoleSender(), cmd.replace("%p", p.getName())); 
        } 
      } 
      this.m.out((Entity)p, Lang.Resolve_Done.toMsg().replace("%item%", Utils.getItemName(target)));
      p.closeInventory();
      return false;
    } 
    if (type == ContentType.EXIT) {
      this.m.out((Entity)p, Lang.Resolve_Cancel.toMsg());
      p.closeInventory();
    } 
    return false;
  }
  
  public boolean onClose(Player p, InventoryCloseEvent e) {
    Inventory inv = e.getInventory();
    ItemStack item = inv.getItem(getItemSlot());
    ItemStack src = inv.getItem(getSourceSlot());
    if (item != null)
      Utils.addItem(p, item); 
    if (this.m.isItemOfThisModule(src))
      Utils.addItem(p, src); 
    return false;
  }
}
