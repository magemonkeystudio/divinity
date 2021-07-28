package su.nightexpress.quantumrpg.modules.identify;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.api.ItemAPI;
import su.nightexpress.quantumrpg.config.JYML;
import su.nightexpress.quantumrpg.config.Lang;
import su.nightexpress.quantumrpg.config.MyConfig;
import su.nightexpress.quantumrpg.modules.EModule;
import su.nightexpress.quantumrpg.modules.ModuleItem;
import su.nightexpress.quantumrpg.modules.QModuleDrop;
import su.nightexpress.quantumrpg.modules.cmds.MExecutor;
import su.nightexpress.quantumrpg.modules.identify.events.QuantumPlayerIdentifyItemEvent;
import su.nightexpress.quantumrpg.modules.tiers.TierManager;
import su.nightexpress.quantumrpg.utils.logs.LogType;

public class IdentifyManager extends QModuleDrop {
  private MyConfig tomesCfg;
  
  private MyConfig itemsCfg;
  
  private final String t_pref = "tome-";
  
  private final String i_pref = "item-";
  
  public IdentifyManager(QuantumRPG plugin, boolean enabled, MExecutor exe) {
    super(plugin, enabled, exe);
  }
  
  public EModule type() {
    return EModule.IDENTIFY;
  }
  
  public String name() {
    return "Identify";
  }
  
  public String version() {
    return "1.0";
  }
  
  public boolean isResolvable() {
    return false;
  }
  
  public void updateCfg() {}
  
  public void setup() {
    this.tomesCfg = new MyConfig((JavaPlugin)this.plugin, "/modules/" + getId(), "tomes.yml");
    this.itemsCfg = new MyConfig((JavaPlugin)this.plugin, "/modules/" + getId(), "items.yml");
    setupMain();
  }
  
  public void shutdown() {
    this.items = null;
    this.tomesCfg = null;
    this.itemsCfg = null;
  }
  
  private void setupMain() {
    setupTomes();
    setupItems();
  }
  
  private void setupTomes() {
    JYML jYML = this.tomesCfg.getConfig();
    if (!jYML.isConfigurationSection("identify-tomes"))
      return; 
    for (String o : jYML.getConfigurationSection("identify-tomes").getKeys(false)) {
      String path = "identify-tomes." + o.toString() + ".";
      IdentifyTome tome = new IdentifyTome(o, path, (FileConfiguration)jYML);
      this.items.put(tome.getId(), tome);
    } 
  }
  
  private void setupItems() {
    JYML jYML = this.itemsCfg.getConfig();
    if (!jYML.isConfigurationSection("unidentified-items"))
      return; 
    for (String o : jYML.getConfigurationSection("unidentified-items").getKeys(false)) {
      String path = "unidentified-items." + o.toString() + ".";
      EModule e = null;
      try {
        e = EModule.valueOf(jYML.getString(String.valueOf(path) + "type").toUpperCase());
      } catch (IllegalArgumentException ex) {
        log("Invalid EModule type for '" + o + "'!", LogType.ERROR);
        continue;
      } 
      if (!e.isEnabled()) {
        log("EModule is disabled '" + o + "'!", LogType.ERROR);
        continue;
      } 
      String item = jYML.getString(String.valueOf(path) + "item-id").toLowerCase();
      String tome = jYML.getString(String.valueOf(path) + "tome").toLowerCase();
      UnidentifiedItem ui = new UnidentifiedItem(o, path, (FileConfiguration)jYML, e, item, tome);
      this.items.put(ui.getId(), ui);
    } 
  }
  
  public boolean isTome(ItemStack item) {
    String id = getItemId(item);
    return isTome(getItemById(id));
  }
  
  public boolean isTome(ModuleItem mi) {
    return mi instanceof IdentifyTome;
  }
  
  public boolean isUnidentified(ItemStack item) {
    String id = getItemId(item);
    return isUnidentified(getItemById(id));
  }
  
  public boolean isUnidentified(ModuleItem mi) {
    return mi instanceof UnidentifiedItem;
  }
  
  public boolean isValidTome(ItemStack item, ItemStack tome) {
    String tom_id = getItemId(tome);
    String ui_id = getItemId(item);
    if (tom_id == null || ui_id == null)
      return false; 
    ModuleItem mi = (ModuleItem)this.items.get(ui_id);
    if (!isUnidentified(mi))
      return false; 
    UnidentifiedItem ui = (UnidentifiedItem)mi;
    return ui.getTomeId().equalsIgnoreCase(tom_id);
  }
  
  @EventHandler
  public void onClick(InventoryClickEvent e) {
    if (!(e.getWhoClicked() instanceof Player))
      return; 
    ItemStack tome = e.getCursor();
    if (!isTome(tome))
      return; 
    ItemStack target = e.getCurrentItem();
    if (!isUnidentified(target))
      return; 
    if (e.getInventory().getType() != InventoryType.CRAFTING)
      return; 
    if (e.getSlotType() == InventoryType.SlotType.CRAFTING)
      return; 
    if (e.getSlotType() == InventoryType.SlotType.ARMOR || e.getSlot() == 40)
      return; 
    Player p = (Player)e.getWhoClicked();
    if (!isValidTome(target, tome)) {
      out((Entity)p, Lang.Identify_WrongTome.toMsg());
      return;
    } 
    UnidentifiedItem ui = (UnidentifiedItem)getItemById(getItemId(target));
    if (ui == null) {
      out((Entity)p, Lang.Other_Internal.toMsg());
      return;
    } 
    ItemStack unlock = null;
    if (ui.getType() == EModule.TIERS) {
      TierManager.Tier t = (TierManager.Tier)((TierManager)this.plugin.getMM().getModule(EModule.TIERS)).getItemById(ui.getItem(), TierManager.Tier.class);
      if (t != null)
        unlock = t.create(-1, target.getType()); 
    } else {
      unlock = ItemAPI.getItemByModule(ui.getType(), ui.getItem(), -1, -1);
    } 
    e.setCancelled(true);
    String t_id = getItemId(tome);
    IdentifyTome t_t = (IdentifyTome)getItemById(t_id, IdentifyTome.class);
    QuantumPlayerIdentifyItemEvent eve = new QuantumPlayerIdentifyItemEvent(ui, t_t, unlock, p);
    this.plugin.getPluginManager().callEvent((Event)eve);
    if (eve.isCancelled())
      return; 
    tome.setAmount(tome.getAmount() - 1);
    if (tome.getAmount() <= 0) {
      e.setCursor(null);
    } else {
      e.setCursor(tome);
    } 
    ItemAPI.updateClassRequirement(unlock, p);
    ItemAPI.updateLevelRequirement(unlock, p);
    e.setCurrentItem(unlock);
  }
  
  public class IdentifyTome extends ModuleItem {
    public IdentifyTome(String id, String path, FileConfiguration cfg) {
      super("tome-" + id, path, cfg, IdentifyManager.this.type());
    }
  }
  
  public class UnidentifiedItem extends ModuleItem {
    private EModule type;
    
    private String item;
    
    private String tome;
    
    public UnidentifiedItem(String id, String path, FileConfiguration cfg, EModule module, String item, String tome) {
      super("item-" + id, path, cfg, IdentifyManager.this.type());
      setType(module);
      setItem(item);
      setTomeId(tome);
    }
    
    public EModule getType() {
      return this.type;
    }
    
    public void setType(EModule type) {
      this.type = type;
    }
    
    public String getItem() {
      return this.item;
    }
    
    public void setItem(String item) {
      this.item = item;
    }
    
    public String getTomeId() {
      return this.tome;
    }
    
    public void setTomeId(String tome) {
      this.tome = "tome-" + tome;
    }
    
    protected ItemStack build() {
      ItemStack orig = ItemAPI.getItemByModule(this.type, getItem(), -1, -1);
      if (orig == null) {
        IdentifyManager.this.log("Invalid item for unidentified item '" + this.id + "'!", LogType.ERROR);
        return new ItemStack(Material.AIR);
      } 
      ItemStack item = super.build();
      item.setType(orig.getType());
      item.setDurability(orig.getDurability());
      return new ItemStack(item);
    }
  }
}
