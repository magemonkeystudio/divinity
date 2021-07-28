package su.nightexpress.quantumrpg.modules.repair;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import su.nightexpress.quantumrpg.api.ItemAPI;
import su.nightexpress.quantumrpg.config.Lang;
import su.nightexpress.quantumrpg.gui.ContentType;
import su.nightexpress.quantumrpg.gui.GUI;
import su.nightexpress.quantumrpg.gui.GUIItem;
import su.nightexpress.quantumrpg.utils.Utils;

public class RepairGUI extends GUI {
  protected RepairManager m;
  
  protected int item_slot;
  
  protected int source_slot;
  
  protected int result_slot;
  
  protected Map<RepairManager.RepairType, GUIItem> ritems;
  
  public RepairGUI(RepairManager m, String title, int size, LinkedHashMap<String, GUIItem> items, int item_slot, int source_slot, int result_slot, Map<RepairManager.RepairType, GUIItem> ritems) {
    super(m.pl(), title, size, items);
    this.m = m;
    this.item_slot = item_slot;
    this.source_slot = source_slot;
    this.result_slot = result_slot;
    this.ritems = ritems;
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
  
  public Map<RepairManager.RepairType, GUIItem> getRepairButtons() {
    return this.ritems;
  }
  
  public void openRepairGUI(Player p, ItemStack item, ItemStack source, RepairManager.RepairType type) {
    ItemStack result = new ItemStack(Material.AIR);
    if (item == null) {
      item = new ItemStack(Material.AIR);
    } else {
      if (ItemAPI.isDamaged(item))
        result = this.m.getResult(item, p); 
      p.getInventory().removeItem(new ItemStack[] { item });
    } 
    if (source == null) {
      source = new ItemStack(Material.ANVIL);
    } else if (source.getAmount() > 1) {
      ItemStack gem2 = new ItemStack(source);
      gem2.setAmount(source.getAmount() - 1);
      source.setAmount(1);
      Utils.addItem(p, gem2);
    } 
    p.openInventory(build(new Object[] { p, item, source, result, type }));
  }
  
  public Inventory build(Object... val) {
    Player p = (Player)val[0];
    ItemStack target = (ItemStack)val[1];
    ItemStack src = (ItemStack)val[2];
    ItemStack res = (ItemStack)val[3];
    RepairManager.RepairType type = (RepairManager.RepairType)val[4];
    Inventory inv = getInventory();
    for (GUIItem gi : this.items.values()) {
      byte b;
      int i;
      int[] arrayOfInt;
      for (i = (arrayOfInt = gi.getSlots()).length, b = 0; b < i; ) {
        int j = arrayOfInt[b];
        ItemStack item = gi.getItem();
        inv.setItem(j, item);
        b++;
      } 
    } 
    inv.setItem(getItemSlot(), target);
    inv.setItem(getSourceSlot(), src);
    inv.setItem(getResultSlot(), res);
    for (Map.Entry<RepairManager.RepairType, GUIItem> e : this.ritems.entrySet()) {
      GUIItem gi = e.getValue();
      byte b;
      int i, arrayOfInt[];
      for (i = (arrayOfInt = gi.getSlots()).length, b = 0; b < i; ) {
        int j = arrayOfInt[b];
        ItemStack item = gi.getItem();
        if (type != null && type == e.getKey()) {
          ItemMeta meta = item.getItemMeta();
          meta.addEnchant(Enchantment.ARROW_DAMAGE, 1, true);
          item.setItemMeta(meta);
        } 
        replaceCostHave(p, target, item, e.getKey());
        inv.setItem(j, item);
        b++;
      } 
    } 
    return inv;
  }
  
  private void replaceCostHave(Player p, ItemStack tor, ItemStack item, RepairManager.RepairType type) {
    ItemMeta meta = item.getItemMeta();
    List<String> lore = new ArrayList<>();
    String cost = String.valueOf(this.m.calcCost(tor, type));
    String have = String.valueOf(this.m.getPlayerBalance(p, type, tor));
    String nm = this.m.getMaterialName(tor);
    if (meta.hasDisplayName()) {
      String n = meta.getDisplayName().replace("%cost%", cost).replace("%have%", have).replace("%mat%", nm);
      meta.setDisplayName(n);
    } 
    if (meta.hasLore()) {
      for (String s : meta.getLore())
        lore.add(s.replace("%cost%", cost).replace("%have%", have).replace("%mat%", nm)); 
      meta.setLore(lore);
    } 
    item.setItemMeta(meta);
  }
  
  private RepairManager.RepairType getSelectedType(Inventory inv) {
    for (Map.Entry<RepairManager.RepairType, GUIItem> ee : getRepairButtons().entrySet()) {
      byte b;
      int i;
      int[] arrayOfInt;
      for (i = (arrayOfInt = ((GUIItem)ee.getValue()).getSlots()).length, b = 0; b < i; ) {
        int j = arrayOfInt[b];
        ItemStack button = inv.getItem(j);
        if (button != null && button.hasItemMeta() && button.getItemMeta().hasEnchants())
          return ee.getKey(); 
        b++;
      } 
    } 
    return null;
  }
  
  public boolean click(Player p, ItemStack item, ContentType type, int slot, InventoryClickEvent e) {
    Inventory inv = e.getInventory();
    ItemStack target = getItem(inv, getItemSlot());
    ItemStack gem = getItem(inv, getSourceSlot());
    RepairManager.RepairType rt = getSelectedType(inv);
    if (slot >= inv.getSize() && target.getType() == Material.AIR) {
      if (item == null || item.getType() == Material.AIR) {
        this.m.out((Entity)p, Lang.Repair_NoItem.toMsg());
        return false;
      } 
      if (!ItemAPI.hasCustomDurability(item)) {
        this.m.out((Entity)p, Lang.Repair_InvalidItem.toMsg().replace("%item%", Utils.getItemName(item)));
        return false;
      } 
      if (!ItemAPI.isDamaged(item)) {
        this.m.out((Entity)p, Lang.Repair_NotDamaged.toMsg().replace("%item%", Utils.getItemName(item)));
        return false;
      } 
      inv.setItem(getSourceSlot(), null);
      this.m.openRepairGUI(p, item, gem, rt);
      p.getInventory().removeItem(new ItemStack[] { item });
      return false;
    } 
    if (slot < inv.getSize() && slot == getItemSlot() && target.getType() != Material.AIR) {
      inv.setItem(getSourceSlot(), null);
      this.m.openRepairGUI(p, (ItemStack)null, gem, rt);
      return false;
    } 
    if (type == ContentType.NONE) {
      for (Map.Entry<RepairManager.RepairType, GUIItem> ee : getRepairButtons().entrySet()) {
        GUIItem gi = ee.getValue();
        if (ArrayUtils.contains(gi.getSlots(), slot)) {
          inv.setItem(getItemSlot(), null);
          inv.setItem(getSourceSlot(), null);
          inv.setItem(getResultSlot(), null);
          this.m.openRepairGUI(p, target, gem, ee.getKey());
          return false;
        } 
      } 
    } else {
      if (type == ContentType.ACCEPT) {
        ItemStack result = getItem(inv, getResultSlot());
        if (result.getType() == Material.AIR)
          return false; 
        inv.setItem(getItemSlot(), null);
        inv.setItem(getSourceSlot(), null);
        if (rt == null) {
          inv.setItem(getItemSlot(), target);
          inv.setItem(getSourceSlot(), gem);
          this.m.out((Entity)p, Lang.Repair_Select.toMsg());
        } else if (this.m.payForRepair(p, rt, target)) {
          p.getInventory().addItem(new ItemStack[] { result });
          this.m.out((Entity)p, Lang.Repair_Done.toMsg());
          this.m.getSettings().playEffect(p);
          this.m.getSettings().playSound(p);
        } else {
          inv.setItem(getItemSlot(), target);
          inv.setItem(getSourceSlot(), gem);
          this.m.out((Entity)p, Lang.Repair_TooExpensive.toMsg());
        } 
        p.closeInventory();
        return false;
      } 
      if (type == ContentType.EXIT) {
        this.m.out((Entity)p, Lang.Repair_Cancel.toMsg());
        p.closeInventory();
      } 
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
