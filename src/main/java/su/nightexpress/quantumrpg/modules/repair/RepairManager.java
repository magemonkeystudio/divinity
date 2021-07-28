package su.nightexpress.quantumrpg.modules.repair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.api.ItemAPI;
import su.nightexpress.quantumrpg.config.JYML;
import su.nightexpress.quantumrpg.gui.GUIItem;
import su.nightexpress.quantumrpg.gui.GUIUtils;
import su.nightexpress.quantumrpg.hooks.EHook;
import su.nightexpress.quantumrpg.hooks.external.VaultHook;
import su.nightexpress.quantumrpg.modules.EModule;
import su.nightexpress.quantumrpg.modules.ModuleItem;
import su.nightexpress.quantumrpg.modules.QModuleDrop;
import su.nightexpress.quantumrpg.modules.cmds.MExecutor;
import su.nightexpress.quantumrpg.types.QClickType;
import su.nightexpress.quantumrpg.utils.Exp;
import su.nightexpress.quantumrpg.utils.ParticleUtils;
import su.nightexpress.quantumrpg.utils.Utils;
import su.nightexpress.quantumrpg.utils.logs.LogType;

public class RepairManager extends QModuleDrop {
  private RepairSettings ss;
  
  private RepairItem item;
  
  private RepairGUI gui;
  
  public RepairManager(QuantumRPG plugin, boolean enabled, MExecutor exe) {
    super(plugin, enabled, exe);
  }
  
  public EModule type() {
    return EModule.REPAIR;
  }
  
  public String name() {
    return "Repair";
  }
  
  public String version() {
    return "1.0";
  }
  
  public boolean isResolvable() {
    return false;
  }
  
  public void updateCfg() {}
  
  public void setup() {
    setupSettings();
    setupItem();
  }
  
  public void shutdown() {
    this.ss = null;
    this.item = null;
    if (this.gui != null) {
      this.gui.shutdown();
      this.gui = null;
    } 
  }
  
  private void setupSettings() {
    QClickType act;
    JYML jYML = this.cfg.getConfig();
    String path = "anvil.";
    boolean anvil_use = jYML.getBoolean(String.valueOf(path) + "enabled", true);
    try {
      act = QClickType.valueOf(jYML.getString(String.valueOf(path) + "open-action"));
    } catch (IllegalArgumentException ex) {
      act = QClickType.SHIFT_RIGHT;
      log("Invalid action type in 'anvil.open-action'!", LogType.WARN);
    } 
    path = "repair.";
    boolean eff_enabled = jYML.getBoolean(String.valueOf(path) + "effects.enabled");
    String eff_value = jYML.getString(String.valueOf(path) + "effects.effect");
    boolean sound_enabled = jYML.getBoolean(String.valueOf(path) + "sounds.enabled");
    Sound sound_value = Sound.BLOCK_ANVIL_USE;
    try {
      sound_value = Sound.valueOf(jYML.getString(String.valueOf(path) + "sounds.sound").toUpperCase());
    } catch (IllegalArgumentException ex) {
      log("Invalid sound in 'repair.sounds.sound'!", LogType.WARN);
    } 
    byte b;
    int i;
    RepairType[] arrayOfRepairType;
    for (i = (arrayOfRepairType = RepairType.values()).length, b = 0; b < i; ) {
      RepairType rt = arrayOfRepairType[b];
      if (jYML.contains(String.valueOf(path) + "types." + rt.name())) {
        boolean bool = jYML.getBoolean(String.valueOf(path) + "types." + rt.name() + ".enabled");
        double d = jYML.getDouble(String.valueOf(path) + "types." + rt.name() + ".cost-per-unit", 1.0D);
        if (rt != RepairType.VAULT || EHook.VAULT.isEnabled()) {
          rt.setEnabled(bool);
          rt.setCost(d);
        } 
      } 
      b++;
    } 
    path = "repair.materials.";
    boolean ig_meta = jYML.getBoolean(String.valueOf(path) + "ignore-items-with-meta");
    HashMap<String, MaterialGroup> mat_group = new HashMap<>();
    for (String o : jYML.getConfigurationSection(String.valueOf(path) + "materials-group").getKeys(false)) {
      String path2 = String.valueOf(path) + "materials-group." + o;
      String id = o;
      List<Material> mats = new ArrayList<>();
      for (String s : jYML.getStringList(path2)) {
        Material m = Material.getMaterial(s.toUpperCase());
        if (m == null) {
          log("Invalid material '" + s + "' in material group '" + o + "'!", LogType.ERROR);
          continue;
        } 
        mats.add(m);
      } 
      MaterialGroup mg = new MaterialGroup(id, mats);
      mat_group.put(mg.getId(), mg);
    } 
    HashMap<String, MaterialTable> mat_table = new HashMap<>();
    for (String o : jYML.getConfigurationSection(String.valueOf(path) + "materials-table").getKeys(false)) {
      String path2 = String.valueOf(path) + "materials-table." + o + ".";
      String id = o;
      if (!mat_group.containsKey(o)) {
        log("Invalid material group '" + o + "' in materials table!", LogType.ERROR);
        continue;
      } 
      String name = ChatColor.translateAlternateColorCodes('&', jYML.getString(String.valueOf(path2) + "name"));
      List<String> mats = jYML.getStringList(String.valueOf(path2) + "can-repair");
      MaterialTable mt = new MaterialTable(id, name, mats);
      mat_table.put(mt.getId(), mt);
    } 
    this.ss = new RepairSettings(
        eff_enabled, 
        eff_value, 
        
        sound_enabled, 
        sound_value, 
        
        anvil_use, 
        act, 
        
        ig_meta, 
        mat_group, 
        mat_table);
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
    HashMap<RepairType, GUIItem> ritems = new HashMap<>();
    if (jYML.isConfigurationSection(String.valueOf(path) + "repair-types"))
      for (String id : jYML.getConfigurationSection(String.valueOf(path) + "repair-types").getKeys(false)) {
        GUIItem gi = GUIUtils.getItemFromSection((FileConfiguration)jYML, id, String.valueOf(path) + "repair-types." + id + ".");
        RepairType rt = null;
        try {
          rt = RepairType.valueOf(id.toUpperCase());
          if (rt.isEnabled())
            ritems.put(rt, gi); 
        } catch (IllegalArgumentException ex) {
          log("Invalid repair type '" + id + "' in 'gui.repair-types'!", LogType.ERROR);
        } 
      }  
    this.gui = new RepairGUI(
        this, 
        
        g_title, 
        g_size, 
        items, 
        item_slot, 
        source_slot, 
        result_slot, 
        ritems);
  }
  
  private void setupItem() {
    JYML jYML = this.cfg.getConfig();
    String path = "item.";
    boolean enabled = jYML.getBoolean(String.valueOf(path) + "enabled");
    if (!enabled)
      return; 
    this.item = new RepairItem(path, (FileConfiguration)jYML);
    this.items.put(this.item.getId(), this.item);
  }
  
  public RepairSettings getSettings() {
    return this.ss;
  }
  
  public void openRepairGUI(Player p, ItemStack item, ItemStack source, RepairType type) {
    this.gui.openRepairGUI(p, item, source, type);
  }
  
  ItemStack getResult(ItemStack target, Player p) {
    int max = ItemAPI.getDurabilityMinOrMax(target, 1);
    ItemStack result = new ItemStack(target);
    return ItemAPI.setDurability(result, max, max);
  }
  
  private MaterialGroup getMaterialGroup(ItemStack item) {
    String type = item.getType().name();
    for (MaterialTable mt : this.ss.getMaterialTables().values()) {
      List<String> mats = mt.getMaterials();
      for (String mat : mats) {
        if (mat.endsWith("*")) {
          String s2 = mat.replace("*", "").toUpperCase();
          if (type.startsWith(s2))
            return this.ss.getMaterialGroup(mt.getId()); 
          continue;
        } 
        if (mat.startsWith("*")) {
          String s2 = mat.replace("*", "").toUpperCase();
          if (type.endsWith(s2))
            return this.ss.getMaterialGroup(mt.getId()); 
          continue;
        } 
        if (type.equalsIgnoreCase(mat.toUpperCase()))
          return this.ss.getMaterialGroup(mt.getId()); 
      } 
    } 
    return null;
  }
  
  public String getMaterialName(ItemStack item) {
    String s = "";
    MaterialGroup mg = getMaterialGroup(item);
    if (mg != null) {
      MaterialTable mt = this.ss.getMaterialTable(mg.getId());
      if (mt != null)
        return mt.getName(); 
    } 
    return s;
  }
  
  private int getPlayerMaterials(Player p, MaterialGroup mg) {
    int have = 0;
    List<Material> mats = mg.getMaterials();
    byte b;
    int i;
    ItemStack[] arrayOfItemStack;
    for (i = (arrayOfItemStack = p.getInventory().getContents()).length, b = 0; b < i; ) {
      ItemStack item = arrayOfItemStack[b];
      if (item != null && (
        !this.ss.isMaterialMetaIgnored() || !item.hasItemMeta()) && 
        mats.contains(item.getType()))
        have += item.getAmount(); 
      b++;
    } 
    return have;
  }
  
  private void takeMaterials(Player p, MaterialGroup mg, int cost) {
    List<Material> mats = mg.getMaterials();
    byte b;
    int i;
    ItemStack[] arrayOfItemStack;
    for (i = (arrayOfItemStack = p.getInventory().getContents()).length, b = 0; b < i; ) {
      ItemStack item = arrayOfItemStack[b];
      if (item != null && (
        !this.ss.isMaterialMetaIgnored() || !item.hasItemMeta()) && 
        mats.contains(item.getType())) {
        int a = item.getAmount();
        if (a > cost) {
          item.setAmount(a - cost);
          return;
        } 
        item.setAmount(0);
        cost -= a;
        if (cost <= 0)
          return; 
      } 
      b++;
    } 
  }
  
  public int calcCost(ItemStack item, RepairType type) {
    int cost = 0;
    if (item == null)
      return cost; 
    double d1 = ItemAPI.getDurabilityMinOrMax(item, 0);
    double d2 = ItemAPI.getDurabilityMinOrMax(item, 1);
    double value = type.getCostPerUnit();
    double get = value * (d2 - d1);
    cost = (int)get;
    return Math.max(1, cost);
  }
  
  boolean payForRepair(Player p, RepairType type, ItemStack item) {
    int pay = calcCost(item, type);
    if (getPlayerBalance(p, type, item) < pay)
      return false; 
    if (type == RepairType.EXP) {
      Exp.setExp(p, -pay);
    } else if (type == RepairType.MATERIAL) {
      MaterialGroup mg = getMaterialGroup(item);
      takeMaterials(p, mg, pay);
    } else if (type == RepairType.VAULT) {
      VaultHook vh = (VaultHook)EHook.VAULT.getHook();
      return vh.take(p, pay);
    } 
    return true;
  }
  
  public int getPlayerBalance(Player p, RepairType type, ItemStack item) {
    int bal = 0;
    if (type == RepairType.EXP)
      return Exp.getTotalExperience(p); 
    if (type == RepairType.MATERIAL) {
      MaterialGroup mg = getMaterialGroup(item);
      if (mg != null)
        return getPlayerMaterials(p, mg); 
    } else if (type == RepairType.VAULT) {
      VaultHook vh = (VaultHook)EHook.VAULT.getHook();
      return (int)vh.getBalans(p);
    } 
    return bal;
  }
  
  @EventHandler
  public void onClickInventory(InventoryClickEvent e) {
    if (this.item == null)
      return; 
    if (!(e.getWhoClicked() instanceof Player))
      return; 
    ItemStack tool = e.getCursor();
    if (!isItemOfThisModule(tool))
      return; 
    ItemStack target = e.getCurrentItem();
    if (!ItemAPI.hasCustomDurability(target))
      return; 
    if (!ItemAPI.isDamaged(target))
      return; 
    if (e.getInventory().getType() != InventoryType.CRAFTING)
      return; 
    if (e.getSlotType() == InventoryType.SlotType.ARMOR || e.getSlot() == 40)
      return; 
    Player p = (Player)e.getWhoClicked();
    e.setCancelled(true);
    openRepairGUI(p, target, tool, (RepairType)null);
    e.setCursor(null);
  }
  
  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
  public void onAnvil(PlayerInteractEvent e) {
    if (!this.ss.isAnvilEnabled())
      return; 
    if (e.getHand() == EquipmentSlot.OFF_HAND)
      return; 
    if (e.getClickedBlock() == null || e.getClickedBlock().getType() != Material.ANVIL)
      return; 
    Action a = e.getAction();
    final Player p = e.getPlayer();
    QClickType type = QClickType.getFromAction(a, p.isSneaking());
    if (type == this.ss.getAction()) {
      final ItemStack i = p.getInventory().getItemInMainHand();
      e.setCancelled(true);
      (new BukkitRunnable() {
          public void run() {
            RepairManager.this.openRepairGUI(p, i, (ItemStack)null, (RepairManager.RepairType)null);
          }
        }).runTaskLater((Plugin)this.plugin, 1L);
    } 
  }
  
  public class MaterialGroup {
    private String id;
    
    private List<Material> mats;
    
    public MaterialGroup(String id, List<Material> mats) {
      this.id = id.toLowerCase();
      this.mats = mats;
    }
    
    public String getId() {
      return this.id;
    }
    
    public List<Material> getMaterials() {
      return this.mats;
    }
  }
  
  public class MaterialTable {
    private String id;
    
    private String name;
    
    private List<String> mats;
    
    public MaterialTable(String id, String name, List<String> mats) {
      this.id = id.toLowerCase();
      this.name = name;
      this.mats = mats;
    }
    
    public String getId() {
      return this.id;
    }
    
    public String getName() {
      return this.name;
    }
    
    public List<String> getMaterials() {
      return this.mats;
    }
  }
  
  public class RepairItem extends ModuleItem {
    public RepairItem(String path, FileConfiguration cfg) {
      super("repair-item", path, cfg, RepairManager.this.type());
    }
  }
  
  public class RepairSettings {
    private boolean eff_enabled;
    
    private String eff_value;
    
    private boolean sound_enabled;
    
    private Sound sound_value;
    
    private boolean anvil_enabled;
    
    private QClickType anvil_action;
    
    private boolean mat_nometa;
    
    private HashMap<String, RepairManager.MaterialGroup> mat_group;
    
    private HashMap<String, RepairManager.MaterialTable> mat_table;
    
    public RepairSettings(boolean eff_enabled, String eff_value, boolean sound_enabled, Sound sound_value, boolean anvil_enabled, QClickType anvil_action, boolean mat_nometa, HashMap<String, RepairManager.MaterialGroup> mat_group, HashMap<String, RepairManager.MaterialTable> mat_table) {
      this.eff_enabled = eff_enabled;
      this.eff_value = eff_value;
      this.sound_enabled = sound_enabled;
      this.sound_value = sound_value;
      setAnvilEnabled(anvil_enabled);
      this.anvil_action = anvil_action;
      this.mat_nometa = mat_nometa;
      this.mat_group = mat_group;
      this.mat_table = mat_table;
    }
    
    public void playEffect(Player p) {
      if (!this.eff_enabled)
        return; 
      Location l = p.getLocation();
      Block b = p.getTargetBlock(null, 100);
      if (b != null && b.getType().name().endsWith("ANVIL"))
        l = Utils.getCenter(b.getLocation()); 
      ParticleUtils.repairEffect(l, this.eff_value);
    }
    
    public void playSound(Player p) {
      if (!this.sound_enabled)
        return; 
      p.playSound(p.getLocation(), this.sound_value, 0.8F, 0.8F);
    }
    
    public boolean isAnvilEnabled() {
      return this.anvil_enabled;
    }
    
    public void setAnvilEnabled(boolean anvil_enabled) {
      this.anvil_enabled = anvil_enabled;
    }
    
    public QClickType getAction() {
      return this.anvil_action;
    }
    
    public boolean isMaterialMetaIgnored() {
      return this.mat_nometa;
    }
    
    public HashMap<String, RepairManager.MaterialGroup> getMaterialGroups() {
      return this.mat_group;
    }
    
    public HashMap<String, RepairManager.MaterialTable> getMaterialTables() {
      return this.mat_table;
    }
    
    public RepairManager.MaterialGroup getMaterialGroup(String id) {
      return this.mat_group.get(id.toLowerCase());
    }
    
    public RepairManager.MaterialTable getMaterialTable(String id) {
      return this.mat_table.get(id.toLowerCase());
    }
  }
  
  public enum RepairType {
    EXP(true, 0.1D),
    MATERIAL(true, 0.02D),
    VAULT(true, 1.33D);
    
    private boolean enabled;
    
    private double cost;
    
    RepairType(boolean enabled, double cost) {
      this.enabled = enabled;
      this.cost = cost;
    }
    
    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }
    
    public boolean isEnabled() {
      return this.enabled;
    }
    
    public void setCost(double cost) {
      this.cost = cost;
    }
    
    public double getCostPerUnit() {
      return this.cost;
    }
  }
}
