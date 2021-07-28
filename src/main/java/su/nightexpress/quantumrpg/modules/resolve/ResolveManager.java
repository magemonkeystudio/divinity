package su.nightexpress.quantumrpg.modules.resolve;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.api.ItemAPI;
import su.nightexpress.quantumrpg.config.JYML;
import su.nightexpress.quantumrpg.config.Lang;
import su.nightexpress.quantumrpg.gui.GUIItem;
import su.nightexpress.quantumrpg.gui.GUIUtils;
import su.nightexpress.quantumrpg.modules.EModule;
import su.nightexpress.quantumrpg.modules.ModuleItem;
import su.nightexpress.quantumrpg.modules.QModuleDrop;
import su.nightexpress.quantumrpg.modules.cmds.MExecutor;
import su.nightexpress.quantumrpg.utils.Utils;
import su.nightexpress.quantumrpg.utils.logs.LogType;

public class ResolveManager extends QModuleDrop {
  private ResolveSettings ss;
  
  private Map<EModule, Map<String, List<SourceItem>>> src;
  
  private ResolveGUI gui;
  
  public ResolveManager(QuantumRPG plugin, boolean enabled, MExecutor exe) {
    super(plugin, enabled, exe);
  }
  
  public EModule type() {
    return EModule.RESOLVE;
  }
  
  public String name() {
    return "Resolve";
  }
  
  public String version() {
    return "1.0";
  }
  
  public boolean isResolvable() {
    return false;
  }
  
  public void updateCfg() {}
  
  public void setup() {
    setupMain();
  }
  
  public void shutdown() {
    this.ss = null;
    this.src = null;
    if (this.gui != null) {
      this.gui.shutdown();
      this.gui = null;
    } 
  }
  
  public void setupMain() {
    setupTables();
    setupItem();
  }
  
  private void setupItem() {
    JYML jYML = this.cfg.getConfig();
    String path = "item.";
    boolean enabled = jYML.getBoolean(String.valueOf(path) + "enabled");
    if (!enabled)
      return; 
    ResolveItem item = new ResolveItem(path, (FileConfiguration)jYML);
    this.items.put(item.getId(), item);
  }
  
  private void setupTables() {
    this.src = new HashMap<>();
    this.plugin.getCM().extract("modules/" + getId() + "/source");
    JYML jYML = this.cfg.getConfig();
    byte b;
    int j;
    EModule[] arrayOfEModule;
    for (j = (arrayOfEModule = EModule.values()).length, b = 0; b < j; ) {
      EModule e = arrayOfEModule[b];
      if (jYML.contains("resolve.source-table." + e.name())) {
        String table = jYML.getString("resolve.source-table." + e.name());
        File f = new File(this.plugin.getDataFolder() + getPath() + "/source/", String.valueOf(table) + ".yml");
        if (!f.exists()) {
          log("Invalid source table '" + table + "' for '" + e.name() + "'!", LogType.ERROR);
        } else {
          YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(f);
          if (yamlConfiguration.isConfigurationSection("items")) {
            Map<String, List<SourceItem>> map = new HashMap<>();
            for (String gid : yamlConfiguration.getConfigurationSection("items").getKeys(false)) {
              List<SourceItem> list = new ArrayList<>();
              if (yamlConfiguration.contains("items." + gid + ".chance"))
                continue; 
              if (!yamlConfiguration.isConfigurationSection("items." + gid)) {
                log("No items found for section '" + gid + "'.", LogType.WARN);
                continue;
              } 
              for (String id : yamlConfiguration.getConfigurationSection("items." + gid).getKeys(false)) {
                String str1 = "items." + gid + "." + id + ".";
                double chance = yamlConfiguration.getDouble(String.valueOf(str1) + "chance");
                ItemStack item = new ItemStack(Material.AIR);
                if (yamlConfiguration.contains(String.valueOf(str1) + "item")) {
                  String str2 = yamlConfiguration.getString(String.valueOf(str1) + "item.material");
                  item = Utils.buildItem(str2);
                  if (item == null) {
                    log("Invalid material '" + str2 + "' for source item '" + id + "'!", LogType.ERROR);
                    continue;
                  } 
                  String str3 = null;
                  List<String> list1 = null;
                  if (yamlConfiguration.contains(String.valueOf(str1) + "item.name"))
                    str3 = ChatColor.translateAlternateColorCodes('&', yamlConfiguration.getString(String.valueOf(str1) + "item.name")); 
                  if (yamlConfiguration.contains(String.valueOf(str1) + "item.lore")) {
                    list1 = new ArrayList<>();
                    for (String s2 : yamlConfiguration.getStringList(String.valueOf(str1) + "item.lore"))
                      list1.add(ChatColor.translateAlternateColorCodes('&', s2)); 
                  } 
                  if (str3 != null || list1 != null) {
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName(str3);
                    meta.setLore(list1);
                    item.setItemMeta(meta);
                  } 
                } 
                List<String> cmds = yamlConfiguration.getStringList(String.valueOf(str1) + "commands");
                String m = yamlConfiguration.getString(String.valueOf(str1) + "preview.material");
                ItemStack preview = Utils.buildItem(m);
                if (preview == null) {
                  log("Invalid material '" + m + "' for preview item '" + id + "'!", LogType.ERROR);
                  continue;
                } 
                String name = null;
                List<String> lore = null;
                if (yamlConfiguration.contains(String.valueOf(str1) + "preview.name"))
                  name = ChatColor.translateAlternateColorCodes('&', yamlConfiguration.getString(String.valueOf(str1) + "preview.name")); 
                if (yamlConfiguration.contains(str1 = "preview.lore")) {
                  lore = new ArrayList<>();
                  for (String s2 : yamlConfiguration.getStringList(String.valueOf(str1) + "preview.lore"))
                    lore.add(ChatColor.translateAlternateColorCodes('&', s2)); 
                } 
                if (name != null || lore != null) {
                  ItemMeta meta = preview.getItemMeta();
                  meta.setDisplayName(name);
                  meta.setLore(lore);
                  preview.setItemMeta(meta);
                } 
                SourceItem si = new SourceItem(id, chance, item, cmds, preview);
                list.add(si);
              } 
              map.put(gid.toLowerCase(), list);
            } 
            this.src.put(e, map);
          } 
        } 
      } 
      b++;
    } 
    String path = "gui.";
    String g_title = ChatColor.translateAlternateColorCodes('&', jYML.getString(String.valueOf(path) + "title"));
    int g_size = jYML.getInt(String.valueOf(path) + "size");
    int item_slot = jYML.getInt(String.valueOf(path) + "item-slot");
    int source_slot = jYML.getInt(String.valueOf(path) + "source-slot");
    String[] raw = jYML.getString(String.valueOf(path) + "result-slots").replaceAll("\\s", "").split(",");
    int[] result_slots = new int[raw.length];
    for (int i = 0; i < raw.length; i++) {
      try {
        result_slots[i] = Integer.parseInt(raw[i].trim());
      } catch (NumberFormatException numberFormatException) {}
    } 
    LinkedHashMap<String, GUIItem> items = new LinkedHashMap<>();
    if (jYML.isConfigurationSection(String.valueOf(path) + "content"))
      for (String id : jYML.getConfigurationSection(String.valueOf(path) + "content").getKeys(false)) {
        GUIItem gi = GUIUtils.getItemFromSection((FileConfiguration)jYML, id, String.valueOf(path) + "content." + id + ".");
        items.put(id, gi);
      }  
    this.gui = new ResolveGUI(
        this, 
        
        g_title, 
        g_size, 
        items, 
        
        item_slot, 
        source_slot, 
        result_slots);
  }
  
  public void openResolveGUI(Player p, ItemStack item, ItemStack source) {
    if (!canResolve(item)) {
      out((Entity)p, Lang.Resolve_Invalid.toMsg().replace("%item%", Utils.getItemName(item)));
      return;
    } 
    if (source == null)
      source = new ItemStack(Material.HOPPER); 
    this.gui.openResolveGUI(p, item, source);
  }
  
  public List<SourceItem> getSource(ItemStack item) {
    List<SourceItem> list = new ArrayList<>();
    EModule e = ItemAPI.getItemModule(item);
    if (e == null)
      return list; 
    if (this.src.containsKey(e)) {
      QModuleDrop q = (QModuleDrop)this.plugin.getMM().getModule(e);
      String id = q.getItemId(item);
      if (id == null || id.isEmpty())
        return list; 
      Map<String, List<SourceItem>> map = this.src.get(e);
      if (map.containsKey(id))
        return map.get(id); 
      if (map.containsKey("default"))
        return map.get("default"); 
      return list;
    } 
    return list;
  }
  
  public boolean canResolve(ItemStack item) {
    return !getSource(item).isEmpty();
  }
  
  @EventHandler
  public void onClickTool(InventoryClickEvent e) {
    if (!(e.getWhoClicked() instanceof Player))
      return; 
    ItemStack tool = e.getCursor();
    if (!isItemOfThisModule(tool))
      return; 
    if (e.getInventory().getType() != InventoryType.CRAFTING)
      return; 
    if (e.getSlotType() == InventoryType.SlotType.ARMOR || e.getSlot() == 40)
      return; 
    Player p = (Player)e.getWhoClicked();
    ItemStack target = e.getCurrentItem();
    if (!canResolve(target))
      return; 
    e.setCancelled(true);
    openResolveGUI(p, target, tool);
    e.setCursor(null);
  }
  
  public class SourceItem {
    private String id;
    
    private double chance;
    
    private ItemStack item;
    
    private List<String> cmds;
    
    private ItemStack preview;
    
    public SourceItem(String id, double chance, ItemStack item, List<String> cmds, ItemStack preview) {
      this.id = id.toLowerCase();
      this.chance = chance;
      this.item = item;
      this.cmds = cmds;
      this.preview = preview;
    }
    
    public String getId() {
      return this.id;
    }
    
    public double getChance() {
      return this.chance;
    }
    
    public void setChance(double chance) {
      this.chance = chance;
    }
    
    public ItemStack getItem() {
      return this.item.clone();
    }
    
    public List<String> getCommands() {
      return this.cmds;
    }
    
    public ItemStack getPreview() {
      return this.preview.clone();
    }
  }
  
  public class ResolveItem extends ModuleItem {
    public ResolveItem(String path, FileConfiguration cfg) {
      super("resolve-tool", path, cfg, ResolveManager.this.type());
    }
  }
  
  public class ResolveSettings {}
}
