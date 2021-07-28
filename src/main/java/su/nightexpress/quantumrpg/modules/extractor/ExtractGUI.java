package su.nightexpress.quantumrpg.modules.extractor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.api.ItemAPI;
import su.nightexpress.quantumrpg.config.Lang;
import su.nightexpress.quantumrpg.gui.ContentType;
import su.nightexpress.quantumrpg.gui.GUI;
import su.nightexpress.quantumrpg.gui.GUIItem;
import su.nightexpress.quantumrpg.hooks.EHook;
import su.nightexpress.quantumrpg.hooks.external.VaultHook;
import su.nightexpress.quantumrpg.modules.QModuleSocket;
import su.nightexpress.quantumrpg.modules.extractor.events.QuantumPlayerSocketExtractEvent;
import su.nightexpress.quantumrpg.types.QSlotType;
import su.nightexpress.quantumrpg.utils.Utils;
import su.nightexpress.quantumrpg.utils.logs.LogType;
import su.nightexpress.quantumrpg.utils.logs.LogUtil;

public class ExtractGUI extends GUI {
  protected ExtractorManager m;
  
  protected String s_name;
  
  protected List<String> s_lore;
  
  protected int item_slot;
  
  protected int source_slot;
  
  protected int result_slot;
  
  private List<Integer> socket_slots;
  
  protected Map<QSlotType, GUIItem> ritems;
  
  public ExtractGUI(ExtractorManager m, String title, int size, LinkedHashMap<String, GUIItem> items, String s_name, List<String> s_lore, int item_slot, int source_slot, int result_slot, List<Integer> socket_slots, Map<QSlotType, GUIItem> ritems) {
    super(m.pl(), title, size, items);
    this.m = m;
    this.s_name = s_name;
    this.s_lore = s_lore;
    this.item_slot = item_slot;
    this.source_slot = source_slot;
    this.result_slot = result_slot;
    this.socket_slots = socket_slots;
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
  
  public List<Integer> getSocketSlots() {
    return this.socket_slots;
  }
  
  public Map<QSlotType, GUIItem> getSocketItems() {
    return this.ritems;
  }
  
  public void openExtractGUI(Player p, ItemStack item, ItemStack source, QSlotType type, int slot) {
    if (item == null) {
      item = new ItemStack(Material.AIR);
    } else {
      p.getInventory().removeItem(new ItemStack[] { item });
    } 
    if (source == null) {
      source = new ItemStack(Material.BARRIER);
    } else if (source.getAmount() > 1) {
      ItemStack gem2 = new ItemStack(source);
      gem2.setAmount(source.getAmount() - 1);
      source.setAmount(1);
      Utils.addItem(p, gem2);
    } 
    p.openInventory(build(new Object[] { p, item, source, type, Integer.valueOf(slot) }));
  }
  
  public Inventory build(Object... val) {
    Player p = (Player)val[0];
    ItemStack target = (ItemStack)val[1];
    ItemStack src = (ItemStack)val[2];
    QSlotType type = (QSlotType)val[3];
    int socket = ((Integer)val[4]).intValue();
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
    for (Map.Entry<QSlotType, GUIItem> e : this.ritems.entrySet()) {
      GUIItem gi = e.getValue();
      byte b;
      int i, arrayOfInt[];
      for (i = (arrayOfInt = gi.getSlots()).length, b = 0; b < i; ) {
        int j = arrayOfInt[b];
        ItemStack item = gi.getItem();
        if (type != null && e.getKey() == type) {
          ItemMeta meta = item.getItemMeta();
          meta.addEnchant(Enchantment.ARROW_DAMAGE, 1, true);
          item.setItemMeta(meta);
        } 
        inv.setItem(j, item);
        b++;
      } 
    } 
    inv.setItem(getItemSlot(), target);
    inv.setItem(getSourceSlot(), src);
    if (type != null) {
      QModuleSocket qs = type.getModule();
      if (qs != null) {
        int j = 0;
        for (String en : qs.getFilledSocketKeys(target)) {
          int slot = ((Integer)this.socket_slots.get(j)).intValue();
          String id = en.split(":")[0];
          int lvl = Integer.parseInt(en.split(":")[1]);
          ItemStack item = ItemAPI.getItemByModule(qs.type(), id, lvl, 0);
          replaceCostHave(p, item, type);
          if (socket == slot) {
            item.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 1);
            ItemStack result = new ItemStack(qs.extractSocket(target.clone(), j));
            inv.setItem(getResultSlot(), result);
          } 
          inv.setItem(slot, item);
          j++;
        } 
      } else {
        LogUtil.send("Invalid module for slot &f'" + type.name() + "'&7.", LogType.ERROR);
      } 
    } 
    return inv;
  }
  
  private void replaceCostHave(Player p, ItemStack item, QSlotType type) {
    if (item == null)
      return; 
    VaultHook vh = (VaultHook)EHook.VAULT.getHook();
    ItemMeta meta = item.getItemMeta();
    List<String> lore = new ArrayList<>();
    String cost = String.valueOf(type.getExtractCost());
    String have = String.valueOf(vh.getBalans(p));
    if (meta.hasDisplayName()) {
      String n = meta.getDisplayName();
      meta.setDisplayName(this.s_name.replace("%name%", n).replace("%cost%", cost).replace("%have%", have));
    } 
    for (String s : this.s_lore)
      lore.add(s.replace("%cost%", cost).replace("%have%", have)); 
    meta.setLore(lore);
    item.setItemMeta(meta);
  }
  
  private QSlotType getSelectedType(Inventory inv) {
    for (Map.Entry<QSlotType, GUIItem> ee : getSocketItems().entrySet()) {
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
    QSlotType rt = getSelectedType(inv);
    if (slot >= inv.getSize() && target.getType() == Material.AIR) {
      inv.setItem(getSourceSlot(), null);
      this.m.openExtractGUI(p, item, gem, rt, -1);
      p.getInventory().removeItem(new ItemStack[] { item });
      return false;
    } 
    if (slot < inv.getSize() && slot == getItemSlot() && target.getType() != Material.AIR) {
      inv.setItem(getSourceSlot(), null);
      this.m.openExtractGUI(p, (ItemStack)null, gem, rt, -1);
      return false;
    } 
    if (type == ContentType.NONE) {
      for (Map.Entry<QSlotType, GUIItem> ee : getSocketItems().entrySet()) {
        GUIItem gi = ee.getValue();
        if (ArrayUtils.contains(gi.getSlots(), slot)) {
          inv.setItem(getItemSlot(), null);
          inv.setItem(getSourceSlot(), null);
          inv.setItem(getResultSlot(), null);
          openExtractGUI(p, target, gem, ee.getKey(), -1);
          return false;
        } 
      } 
      if (getSocketSlots().contains(Integer.valueOf(slot))) {
        inv.setItem(getItemSlot(), null);
        inv.setItem(getSourceSlot(), null);
        inv.setItem(getResultSlot(), null);
        openExtractGUI(p, target, gem, rt, slot);
      } 
    } else {
      if (type == ContentType.ACCEPT) {
        ItemStack result = inv.getItem(getResultSlot());
        if (result == null || result.getType() == Material.AIR) {
          this.m.out((Entity)p, Lang.Extractor_Select.toMsg());
          p.closeInventory();
          return false;
        } 
        VaultHook vh = (VaultHook)EHook.VAULT.getHook();
        if (vh.getBalans(p) < rt.getExtractCost()) {
          this.m.out((Entity)p, Lang.Extractor_TooExpensive.toMsg());
          p.closeInventory();
          return false;
        } 
        QuantumPlayerSocketExtractEvent eve = new QuantumPlayerSocketExtractEvent(inv.getItem(getItemSlot()), result, p, rt);
        ((QuantumRPG)this.plugin).getPluginManager().callEvent((Event)eve);
        inv.setItem(getItemSlot(), null);
        inv.setItem(getSourceSlot(), null);
        this.m.out((Entity)p, Lang.Extractor_Done.toMsg());
        vh.take(p, rt.getExtractCost());
        p.getInventory().addItem(new ItemStack[] { result });
        p.closeInventory();
        return false;
      } 
      if (type == ContentType.EXIT) {
        this.m.out((Entity)p, Lang.Extractor_Cancel.toMsg());
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
