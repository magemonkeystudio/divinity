package su.nightexpress.quantumrpg.modules.sell;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import su.nightexpress.quantumrpg.config.Lang;
import su.nightexpress.quantumrpg.gui.ContentType;
import su.nightexpress.quantumrpg.gui.GUI;
import su.nightexpress.quantumrpg.gui.GUIItem;
import su.nightexpress.quantumrpg.hooks.EHook;
import su.nightexpress.quantumrpg.hooks.external.VaultHook;
import su.nightexpress.quantumrpg.utils.Utils;

public class SellGUI extends GUI {
  private SellManager m;
  
  private int[] item_slots;
  
  public SellGUI(SellManager m, String title, int size, LinkedHashMap<String, GUIItem> items, int[] item_slots) {
    super(m.pl(), title, size, items);
    this.m = m;
    this.item_slots = item_slots;
  }
  
  protected boolean ignoreNullClick() {
    return false;
  }
  
  public void open(Player p) {
    Inventory inv = build();
    p.openInventory(inv);
    update(inv);
  }
  
  public Inventory build() {
    Inventory inv = getInventory();
    for (GUIItem gi : this.items.values()) {
      ItemStack item = gi.getItem().clone();
      byte b;
      int i, arrayOfInt[];
      for (i = (arrayOfInt = gi.getSlots()).length, b = 0; b < i; ) {
        int j = arrayOfInt[b];
        inv.setItem(j, item);
        b++;
      } 
    } 
    return inv;
  }
  
  public int[] getItemSlots() {
    return this.item_slots;
  }
  
  private void replaceCost(ItemStack item, Inventory inv) {
    ItemMeta meta = item.getItemMeta();
    List<String> lore = new ArrayList<>();
    String cost = String.valueOf(getTotalCost(inv));
    if (meta.hasDisplayName()) {
      String n = meta.getDisplayName()
        .replace("%cost%", cost);
      meta.setDisplayName(n);
    } 
    if (meta.hasLore()) {
      for (String s : meta.getLore())
        lore.add(s
            .replace("%cost%", cost)); 
      meta.setLore(lore);
    } 
    item.setItemMeta(meta);
  }
  
  private void update(Inventory inv) {
    for (GUIItem gi : getContent().values()) {
      ItemStack item = gi.getItem().clone();
      replaceCost(item, inv);
      byte b;
      int i, arrayOfInt[];
      for (i = (arrayOfInt = gi.getSlots()).length, b = 0; b < i; ) {
        int j = arrayOfInt[b];
        inv.setItem(j, item);
        b++;
      } 
    } 
  }
  
  private double getTotalCost(Inventory inv) {
    double d = 0.0D;
    if (inv == null)
      return d; 
    byte b;
    int i, arrayOfInt[];
    for (i = (arrayOfInt = getItemSlots()).length, b = 0; b < i; ) {
      int j = arrayOfInt[b];
      d += this.m.calcCost(inv.getItem(j));
      b++;
    } 
    return Utils.round3(d);
  }
  
  public boolean click(Player p, ItemStack item, ContentType type, int slot, InventoryClickEvent e) {
    final Inventory inv = e.getInventory();
    if (ArrayUtils.contains(getItemSlots(), slot) || slot >= getSize())
      e.setCancelled(false); 
    if (type == ContentType.NONE) {
      (new BukkitRunnable() {
          public void run() {
            SellGUI.this.update(inv);
          }
        }).runTaskLater(this.plugin, 1L);
    } else if (type == ContentType.ACCEPT) {
      double cost = getTotalCost(inv);
      byte b;
      int i, arrayOfInt[];
      for (i = (arrayOfInt = getItemSlots()).length, b = 0; b < i; ) {
        int j = arrayOfInt[b];
        ItemStack item2 = inv.getItem(j);
        if (item2 != null) {
          double c2 = this.m.calcCost(item2);
          if (c2 <= 0.0D)
            Utils.addItem(p, item2); 
        } 
        b++;
      } 
      inv.setContents(new ItemStack[0]);
      if (cost > 0.0D) {
        VaultHook vh = (VaultHook)EHook.VAULT.getHook();
        vh.give(p, cost);
        this.m.out((Entity)p, Lang.Sell_Sell.toMsg().replace("%cost%", String.valueOf(cost)));
      } 
      p.closeInventory();
    } else if (type == ContentType.EXIT) {
      p.closeInventory();
      this.m.out((Entity)p, Lang.Sell_Cancel.toMsg());
    } 
    return false;
  }
  
  public boolean onClose(Player p, InventoryCloseEvent e) {
    Inventory inv = e.getInventory();
    byte b;
    int i, arrayOfInt[];
    for (i = (arrayOfInt = getItemSlots()).length, b = 0; b < i; ) {
      int j = arrayOfInt[b];
      ItemStack item = inv.getItem(j);
      if (item != null)
        Utils.addItem(p, item); 
      b++;
    } 
    return false;
  }
}
