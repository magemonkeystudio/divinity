package su.nightexpress.quantumrpg.modules.soulbound;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.api.ItemAPI;
import su.nightexpress.quantumrpg.config.JYML;
import su.nightexpress.quantumrpg.config.Lang;
import su.nightexpress.quantumrpg.gui.GUIItem;
import su.nightexpress.quantumrpg.gui.GUIUtils;
import su.nightexpress.quantumrpg.modules.EModule;
import su.nightexpress.quantumrpg.modules.QModule;
import su.nightexpress.quantumrpg.modules.cmds.MExecutor;
import su.nightexpress.quantumrpg.nbt.NBTItem;

public class SoulboundManager extends QModule {
  private String untrade;
  
  private String soul;
  
  private String soulset;
  
  private List<String> cmds;
  
  private boolean b_drop;
  
  private boolean b_pick;
  
  private boolean b_click;
  
  private boolean b_use;
  
  private boolean i_pick;
  
  private boolean i_use;
  
  private boolean i_drop;
  
  private boolean i_death;
  
  private SoulGUI gui;
  
  private Map<Player, Long> msg_d;
  
  private final String NBT_KEY_SOUL = "Owner";
  
  public SoulboundManager(QuantumRPG plugin, boolean enabled, MExecutor exe) {
    super(plugin, enabled, exe);
  }
  
  public EModule type() {
    return EModule.SOULBOUND;
  }
  
  public String name() {
    return "Soulbound";
  }
  
  public String version() {
    return "1.0";
  }
  
  public boolean isResolvable() {
    return false;
  }
  
  public void updateCfg() {}
  
  public void setup() {
    this.msg_d = new WeakHashMap<>();
    JYML jYML = this.cfg.getConfig();
    String path = "lore-format.";
    this.untrade = ChatColor.translateAlternateColorCodes('&', jYML.getString(String.valueOf(path) + "untradable"));
    this.soul = ChatColor.translateAlternateColorCodes('&', jYML.getString(String.valueOf(path) + "soulbound-req"));
    this.soulset = ChatColor.translateAlternateColorCodes('&', jYML.getString(String.valueOf(path) + "soulbound-set"));
    this.cmds = jYML.getStringList("general.block-commands");
    path = "bind-to-player.";
    this.b_drop = jYML.getBoolean(String.valueOf(path) + "on-item-drop");
    this.b_pick = jYML.getBoolean(String.valueOf(path) + "on-item-pickup");
    this.b_click = jYML.getBoolean(String.valueOf(path) + "on-item-click");
    this.b_use = jYML.getBoolean(String.valueOf(path) + "on-item-use");
    path = "interact.";
    this.i_pick = jYML.getBoolean(String.valueOf(path) + "allow-pickup");
    this.i_use = jYML.getBoolean(String.valueOf(path) + "allow-use");
    this.i_drop = jYML.getBoolean(String.valueOf(path) + "allow-drop");
    this.i_death = jYML.getBoolean(String.valueOf(path) + "drop-on-death");
    path = "gui.";
    String g_title = ChatColor.translateAlternateColorCodes('&', jYML.getString(String.valueOf(path) + "title"));
    int g_size = jYML.getInt(String.valueOf(path) + "size");
    int item_slot = jYML.getInt(String.valueOf(path) + "item-slot");
    int source_slot = jYML.getInt(String.valueOf(path) + "source-slot");
    int result_slot = jYML.getInt(String.valueOf(path) + "result-slot");
    LinkedHashMap<String, GUIItem> items = new LinkedHashMap<>();
    if (jYML.isConfigurationSection(String.valueOf(path) + "content"))
      for (String id : jYML.getConfigurationSection(String.valueOf(path) + "content").getKeys(false)) {
        GUIItem gi = GUIUtils.getItemFromSection((FileConfiguration)jYML, id, String.valueOf(path) + "content." + id + ".");
        items.put(id, gi);
      }  
    this.gui = new SoulGUI(this, g_title, g_size, items, item_slot, source_slot, result_slot);
  }
  
  public void shutdown() {
    this.cmds = null;
    if (this.gui != null) {
      this.gui.shutdown();
      this.gui = null;
    } 
  }
  
  public ItemStack removeSoulbound(ItemStack item) {
    String[] unlock10 = getSoulSetString().split("%p");
    String unlock1 = "";
    if (unlock10[0] != null) {
      unlock1 = unlock10[0];
    } else {
      unlock1 = unlock10[1];
    } 
    ItemMeta meta = item.getItemMeta();
    List<String> lore = new ArrayList<>(meta.getLore());
    int pos = 0;
    for (String s : lore) {
      if (s.contains(unlock1)) {
        pos = lore.indexOf(s);
        lore.remove(pos);
        lore.add(pos, getSoulString());
        break;
      } 
      if (s.contains(getUntradeString())) {
        pos = lore.indexOf(s);
        lore.remove(pos);
        break;
      } 
    } 
    meta.setLore(lore);
    item.setItemMeta(meta);
    NBTItem nbt = new NBTItem(item);
    nbt.removeKey("Owner");
    return nbt.getItem();
  }
  
  public boolean hasSoulbound(ItemStack item) {
    return isSoulBinded(item);
  }
  
  public ItemStack setSoulbound(ItemStack target, Player p) {
    ItemMeta meta = target.getItemMeta();
    List<String> lore = meta.getLore();
    int pos = lore.indexOf(getSoulString());
    lore.remove(pos);
    lore.add(pos, getSoulSetString().replace("%p", p.getName()));
    meta.setLore(lore);
    target.setItemMeta(meta);
    target = setOwner(target, p);
    return target;
  }
  
  public boolean hasOwner(ItemStack item) {
    if (item == null || item.getType() == Material.AIR)
      return false; 
    NBTItem nbt = new NBTItem(item);
    return nbt.hasKey("Owner").booleanValue();
  }
  
  public boolean isOwner(ItemStack item, Player p) {
    return !(!p.getUniqueId().toString().equals(getOwner(item)) && !p.getName().equalsIgnoreCase(getOwner(item)));
  }
  
  public String getOwner(ItemStack item) {
    return (new NBTItem(item)).getString("Owner");
  }
  
  public ItemStack setOwner(ItemStack item, Player p) {
    NBTItem nbt = new NBTItem(item);
    nbt.setString("Owner", p.getUniqueId().toString());
    return nbt.getItem();
  }
  
  public boolean isUntradeable(ItemStack item) {
    if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore())
      return false; 
    ItemMeta meta = item.getItemMeta();
    List<String> lore = meta.getLore();
    for (String s : lore) {
      if (s.contains(getUntradeString()))
        return true; 
    } 
    return false;
  }
  
  public void setUntradeable(ItemStack item, boolean b, int line) {
    if (item == null || item.getType() == Material.AIR)
      return; 
    ItemMeta meta = item.getItemMeta();
    List<String> lore = meta.getLore();
    if (lore == null)
      lore = new ArrayList<>(); 
    String non = getUntradeString();
    lore.remove(non);
    if (b)
      if (line > 0 && lore.size() > line) {
        lore.add(line, non);
      } else {
        lore.add(non);
      }  
    meta.setLore(lore);
    item.setItemMeta(meta);
  }
  
  public boolean isSoulboundRequired(ItemStack item) {
    if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore())
      return false; 
    ItemMeta meta = item.getItemMeta();
    List<String> lore = meta.getLore();
    for (String s : lore) {
      if (s.contains(getSoulString()))
        return true; 
    } 
    return false;
  }
  
  public void setSoulboundRequirement(ItemStack item, boolean b, int line) {
    if (item == null || item.getType() == Material.AIR)
      return; 
    ItemMeta meta = item.getItemMeta();
    List<String> lore = meta.getLore();
    if (lore == null)
      lore = new ArrayList<>(); 
    String non = getSoulString();
    lore.remove(non);
    if (b)
      if (line > 0 && line < lore.size()) {
        lore.add(line, non);
      } else {
        lore.add(non);
      }  
    meta.setLore(lore);
    item.setItemMeta(meta);
  }
  
  public boolean isSoulBinded(ItemStack item) {
    UUID id;
    String name;
    if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore())
      return false; 
    if (!hasOwner(item))
      return false; 
    ItemMeta meta = item.getItemMeta();
    List<String> lore = meta.getLore();
    String owner = getOwner(item);
    try {
      id = UUID.fromString(owner);
    } catch (IllegalArgumentException ex) {
      return false;
    } 
    Player p = this.plugin.getServer().getPlayer(id);
    if (p == null) {
      name = this.plugin.getServer().getOfflinePlayer(UUID.fromString(owner)).getName();
    } else {
      name = p.getName();
    } 
    if (getSoulSetString() == null)
      return false; 
    String ss = getSoulSetString().replace("%p", name);
    if (lore.contains(ss))
      return true; 
    return false;
  }
  
  public String getUntradeString() {
    return this.untrade;
  }
  
  public String getSoulString() {
    return this.soul;
  }
  
  public String getSoulSetString() {
    return this.soulset;
  }
  
  public List<String> getCmds() {
    return this.cmds;
  }
  
  public boolean bindOnDrop() {
    return this.b_drop;
  }
  
  public boolean bindOnPickup() {
    return this.b_pick;
  }
  
  public boolean bindOnClick() {
    return this.b_click;
  }
  
  public boolean bindOnUse() {
    return this.b_use;
  }
  
  public boolean allowPickup() {
    return this.i_pick;
  }
  
  public boolean allowUse() {
    return this.i_use;
  }
  
  public boolean allowDropDeath() {
    return this.i_death;
  }
  
  private void openGUI(Player p, ItemStack item) {
    ItemStack src = new ItemStack(Material.SKULL_ITEM);
    SkullMeta meta = (SkullMeta)src.getItemMeta();
    meta.setDisplayName("§c" + p.getName());
    meta.setOwner(p.getName());
    src.setItemMeta((ItemMeta)meta);
    ItemStack result = setSoulbound(item.clone(), p);
    this.gui.openSocketing(p, item, src, result);
  }
  
  private void sendPickupError(Player p) {
    if (this.msg_d.containsKey(p)) {
      long l = ((Long)this.msg_d.get(p)).longValue();
      if (System.currentTimeMillis() < l)
        return; 
    } 
    p.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Soulbound_Error_Pickup.toMsg());
    this.msg_d.put(p, Long.valueOf(System.currentTimeMillis() + 2000L));
  }
  
  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
  public void onPickup(EntityPickupItemEvent e) {
    if (!(e.getEntity() instanceof Player))
      return; 
    ItemStack item = e.getItem().getItemStack();
    Player p = (Player)e.getEntity();
    if (ItemAPI.hasOwner(item) && !ItemAPI.isOwner(item, p)) {
      e.setCancelled(true);
      sendPickupError(p);
      return;
    } 
    if (ItemAPI.isUntradeable(item) && bindOnPickup())
      e.getItem().setItemStack(ItemAPI.setOwner(item, p)); 
  }
  
  @EventHandler(ignoreCancelled = true)
  public void onInventoryUClick(InventoryClickEvent e) {
    Player p = (Player)e.getWhoClicked();
    ItemStack item = e.getCurrentItem();
    if (e.getInventory().getType() == InventoryType.CRAFTING && e.getInventory().getHolder().equals(p)) {
      if (item == null || !item.hasItemMeta())
        return; 
      if (isSoulboundRequired(item) && e.isRightClick() && !e.isShiftClick() && e.getSlotType() != InventoryType.SlotType.CRAFTING) {
        if (e.getSlotType() == InventoryType.SlotType.ARMOR || e.getSlot() == 40)
          return; 
        openGUI(p, item);
        e.setCurrentItem(null);
        e.setCancelled(true);
        return;
      } 
    } else if ((isSoulBinded(item) || hasOwner(item)) && 
      !isOwner(item, p) && !p.hasPermission("qrpg.bypass.owner")) {
      e.setCancelled(true);
      return;
    } 
    if (isUntradeable(item) && !hasOwner(item) && bindOnClick()) {
      item = setOwner(item, p);
      e.setCurrentItem(item);
    } 
  }
  
  @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  public void onInter(PlayerInteractEvent e) {
    Player p = e.getPlayer();
    ItemStack item = e.getItem();
    if (item == null || item.getItemMeta() == null || item.getItemMeta().getLore() == null)
      return; 
    if (isUntradeable(item) && !hasOwner(item) && bindOnUse())
      if (e.getHand() == EquipmentSlot.OFF_HAND) {
        p.getInventory().setItemInOffHand(setOwner(item, p));
      } else {
        p.getInventory().setItemInMainHand(setOwner(item, p));
      }  
  }
  
  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onItemDrop(PlayerDropItemEvent e) {
    ItemStack item = e.getItemDrop().getItemStack();
    if (!this.i_drop && (
      isSoulBinded(item) || isUntradeable(item))) {
      e.setCancelled(true);
      return;
    } 
    if (item == null)
      return; 
    if (!isUntradeable(item))
      return; 
    if (hasOwner(item))
      return; 
    if (!bindOnDrop())
      return; 
    Player p = e.getPlayer();
    if (isUntradeable(item))
      e.getItemDrop().setItemStack(setOwner(item, p)); 
  }
  
  @EventHandler
  public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent e) {
    Player p = e.getPlayer();
    String msg = e.getMessage();
    ItemStack item = p.getInventory().getItemInMainHand();
    ItemStack item2 = p.getInventory().getItemInOffHand();
    if (item == null && item2 == null)
      return; 
    for (String s : getCmds()) {
      if (msg.startsWith(s) && (
        isUntradeable(item) || isSoulBinded(item) || 
        isUntradeable(item2) || isSoulBinded(item2))) {
        e.setCancelled(true);
        out((Entity)p, Lang.Restrictions_NoCommands.toMsg());
        return;
      } 
    } 
  }
  
  @EventHandler(ignoreCancelled = true)
  public void onHopper(InventoryPickupItemEvent e) {
    ItemStack item = e.getItem().getItemStack();
    if ((isUntradeable(item) || isSoulBinded(item)) && 
      e.getInventory().getType() == InventoryType.HOPPER)
      e.setCancelled(true); 
  }
  
  @EventHandler(priority = EventPriority.HIGHEST)
  public void onDeath(PlayerDeathEvent e) {
    if (allowDropDeath())
      return; 
    List<ItemStack> list = new ArrayList<>(e.getDrops());
    final List<ItemStack> list2 = new ArrayList<>();
    for (ItemStack i : list) {
      if (hasOwner(i)) {
        e.getDrops().remove(i);
        list2.add(i);
      } 
    } 
    final Player p = e.getEntity();
    this.plugin.getServer().getScheduler().scheduleSyncDelayedTask((Plugin)this.plugin, new Runnable() {
          public void run() {
            for (ItemStack i : list2) {
              p.getInventory().addItem(new ItemStack[] { i });
            } 
          }
        });
  }
}
