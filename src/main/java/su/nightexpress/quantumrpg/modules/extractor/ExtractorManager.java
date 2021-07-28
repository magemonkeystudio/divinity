package su.nightexpress.quantumrpg.modules.extractor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.config.JYML;
import su.nightexpress.quantumrpg.gui.GUIItem;
import su.nightexpress.quantumrpg.gui.GUIUtils;
import su.nightexpress.quantumrpg.modules.EModule;
import su.nightexpress.quantumrpg.modules.ModuleItem;
import su.nightexpress.quantumrpg.modules.QModuleDrop;
import su.nightexpress.quantumrpg.modules.cmds.MExecutor;
import su.nightexpress.quantumrpg.types.QSlotType;
import su.nightexpress.quantumrpg.utils.logs.LogType;

public class ExtractorManager extends QModuleDrop {
  private ExtractorSettings ss;
  
  private ExtractorTool item;
  
  private ExtractGUI gui;
  
  public ExtractorManager(QuantumRPG plugin, boolean enabled, MExecutor exe) {
    super(plugin, enabled, exe);
  }
  
  public EModule type() {
    return EModule.EXTRACTOR;
  }
  
  public String name() {
    return "Extractor";
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
    JYML jYML = this.cfg.getConfig();
    if (jYML.isConfigurationSection("extract.cost")) {
      byte b;
      int j;
      QSlotType[] arrayOfQSlotType;
      for (j = (arrayOfQSlotType = QSlotType.values()).length, b = 0; b < j; ) {
        QSlotType qs = arrayOfQSlotType[b];
        double d = jYML.getDouble("extract.cost." + qs.name(), 250.0D);
        qs.setExtractCost(d);
        b++;
      } 
    } 
    String path = "gui.";
    String g_title = ChatColor.translateAlternateColorCodes('&', jYML.getString(String.valueOf(path) + "title"));
    int g_size = jYML.getInt(String.valueOf(path) + "size");
    int item_slot = jYML.getInt(String.valueOf(path) + "item-slot");
    int source_slot = jYML.getInt(String.valueOf(path) + "source-slot");
    int result_slot = jYML.getInt(String.valueOf(path) + "result-slot");
    String s_name = ChatColor.translateAlternateColorCodes('&', jYML.getString(String.valueOf(path) + "socket-name"));
    List<String> s_lore = new ArrayList<>();
    for (String ss : jYML.getStringList(String.valueOf(path) + "socket-lore"))
      s_lore.add(ChatColor.translateAlternateColorCodes('&', ss)); 
    List<Integer> socket_slots = new ArrayList<>();
    String[] raw = jYML.getString(String.valueOf(path) + "socket-slots").replaceAll("\\s", "").split(",");
    for (int i = 0; i < raw.length; i++) {
      try {
        socket_slots.add(Integer.valueOf(Integer.parseInt(raw[i].trim())));
      } catch (NumberFormatException numberFormatException) {}
    } 
    LinkedHashMap<String, GUIItem> items = new LinkedHashMap<>();
    if (jYML.isConfigurationSection(String.valueOf(path) + "content"))
      for (String id : jYML.getConfigurationSection(String.valueOf(path) + "content").getKeys(false)) {
        GUIItem gi = GUIUtils.getItemFromSection((FileConfiguration)jYML, id, String.valueOf(path) + "content." + id + ".");
        items.put(id, gi);
      }  
    HashMap<QSlotType, GUIItem> ritems = new HashMap<>();
    if (jYML.isConfigurationSection(String.valueOf(path) + "socket-types"))
      for (String id : jYML.getConfigurationSection(String.valueOf(path) + "socket-types").getKeys(false)) {
        GUIItem gi = GUIUtils.getItemFromSection((FileConfiguration)jYML, id, String.valueOf(path) + "socket-types." + id + ".");
        QSlotType rt = null;
        try {
          rt = QSlotType.valueOf(id.toUpperCase());
          if (rt.getExtractCost() != -1.0D)
            ritems.put(rt, gi); 
        } catch (IllegalArgumentException ex) {
          log("Invalid socket type '" + id + "' in 'gui.socket-types'!", LogType.ERROR);
        } 
      }  
    this.gui = new ExtractGUI(
        this, 
        
        g_title, 
        g_size, 
        items, 
        
        s_name, 
        s_lore, 
        
        item_slot, 
        source_slot, 
        result_slot, 
        socket_slots, 
        
        ritems);
  }
  
  private void setupItem() {
    JYML jYML = this.cfg.getConfig();
    String path = "item.";
    boolean enabled = jYML.getBoolean(String.valueOf(path) + "enabled");
    if (!enabled)
      return; 
    this.item = new ExtractorTool(path, (FileConfiguration)jYML);
    this.items.put(this.item.getId(), this.item);
  }
  
  public ExtractorSettings getSettings() {
    return this.ss;
  }
  
  public void openExtractGUI(Player p, ItemStack item, ItemStack source, QSlotType type, int socket) {
    this.gui.openExtractGUI(p, item, source, type, socket);
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
    if (target == null || !target.hasItemMeta())
      return; 
    if (e.getInventory().getType() != InventoryType.CRAFTING)
      return; 
    if (e.getSlotType() == InventoryType.SlotType.ARMOR || e.getSlot() == 40)
      return; 
    Player p = (Player)e.getWhoClicked();
    e.setCancelled(true);
    openExtractGUI(p, target, tool, (QSlotType)null, -1);
    e.setCursor(null);
  }
  
  public class ExtractorTool extends ModuleItem {
    public ExtractorTool(String path, FileConfiguration cfg) {
      super("extract-tool", path, cfg, ExtractorManager.this.type());
    }
  }
  
  public class ExtractorSettings {}
}
