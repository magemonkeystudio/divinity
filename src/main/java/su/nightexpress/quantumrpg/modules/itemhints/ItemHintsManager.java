package su.nightexpress.quantumrpg.modules.itemhints;

import java.util.Arrays;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.api.ItemAPI;
import su.nightexpress.quantumrpg.config.JYML;
import su.nightexpress.quantumrpg.libs.glowapi.GlowAPI;
import su.nightexpress.quantumrpg.modules.EModule;
import su.nightexpress.quantumrpg.modules.QModule;
import su.nightexpress.quantumrpg.modules.cmds.MExecutor;

public class ItemHintsManager extends QModule {
  private HintSettings ss;
  
  public ItemHintsManager(QuantumRPG plugin, boolean enabled, MExecutor exe) {
    super(plugin, enabled, exe);
  }
  
  public EModule type() {
    return EModule.ITEM_HINTS;
  }
  
  public String name() {
    return "Item Hints";
  }
  
  public String version() {
    return "1.0.1";
  }
  
  public boolean isResolvable() {
    return false;
  }
  
  public void updateCfg() {
    JYML jYML = this.cfg.getConfig();
    byte b;
    int i;
    EModule[] arrayOfEModule;
    for (i = (arrayOfEModule = EModule.values()).length, b = 0; b < i; ) {
      EModule e = arrayOfEModule[b];
      QModule q = this.plugin.getMM().getModule(e);
      if (q != null && q instanceof su.nightexpress.quantumrpg.modules.QModuleDrop && 
        !jYML.contains("for-modules." + e.name())) {
        jYML.set("for-modules." + e.name(), Boolean.valueOf(true));
        e.setHint(true);
      } 
      b++;
    } 
    String ver = jYML.getString("cfg_version");
    if (!ver.equals(version())) {
      jYML.set("cfg_version", version());
      jYML.set("general.name-black-list", Arrays.asList(new String[] { "some word", "***{" }));
      jYML.set("general.lore-black-list", Arrays.asList(new String[] { "some word" }));
    } 
    this.cfg.save();
  }
  
  public void setup() {
    setupCfg();
  }
  
  public void shutdown() {
    this.ss = null;
  }
  
  private void setupCfg() {
    JYML jYML = this.cfg.getConfig();
    byte b;
    int i;
    EModule[] arrayOfEModule;
    for (i = (arrayOfEModule = EModule.values()).length, b = 0; b < i; ) {
      EModule e = arrayOfEModule[b];
      boolean bool = jYML.getBoolean("for-modules." + e.name());
      e.setHint(bool);
      b++;
    } 
    boolean custom_only = jYML.getBoolean("general.only-custom-name-items", false);
    boolean glow = jYML.getBoolean("general.glow");
    String format_single = jYML.getString("format.single");
    String format_multi = jYML.getString("format.multi");
    List<String> hint_black = jYML.getStringList("general.hint-black-list");
    List<String> glow_black = jYML.getStringList("general.glow-black-list");
    List<String> name_black = jYML.getStringList("general.name-black-list");
    List<String> lore_black = jYML.getStringList("general.lore-black-list");
    this.ss = new HintSettings(
        custom_only, 
        glow, 
        
        format_single, 
        format_multi, 
        
        hint_black, 
        glow_black, 
        name_black, 
        lore_black);
  }
  
  @EventHandler(ignoreCancelled = true)
  public void onItemSpawn(ItemSpawnEvent e) {
    Item item = e.getEntity();
    ItemStack is = item.getItemStack();
    setItemHint(item, is, 0, this.ss.getFormatSingle());
  }
  
  @EventHandler
  public void onItemMerge(ItemMergeEvent e) {
    Item item2 = e.getEntity();
    Item item = e.getTarget();
    setItemHint(item, item.getItemStack(), item2.getItemStack().getAmount(), this.ss.getFormatMulti());
  }
  
  private void setItemHint(Item item, ItemStack is, int a, String format) {
    EModule e = ItemAPI.getItemModule(is);
    if (e != null && !e.isHinted())
      return; 
    if (!this.ss.isHintBlacklisted(is)) {
      String str1 = "";
      if (is.hasItemMeta()) {
        ItemMeta meta = is.getItemMeta();
        if (meta.hasDisplayName()) {
          str1 = meta.getDisplayName();
          for (String bl : this.ss.getNameBlacklist()) {
            if (str1.contains(bl))
              return; 
          } 
        } 
        if (meta.hasLore()) {
          List<String> lore = meta.getLore();
          for (String bl : this.ss.getNameBlacklist()) {
            for (String ll : lore) {
              if (ll.contains(bl))
                return; 
            } 
          } 
        } 
      } else {
        if (this.ss.isCustomOnly())
          return; 
        str1 = this.plugin.getCM().getDefaultItemName(is);
      } 
      String amount = String.valueOf(is.getAmount() + a);
      String name2 = format.replace("%name%", str1).replace("%amount%", amount);
      item.setCustomName(name2);
      item.setCustomNameVisible(true);
    } 
    String name = item.getCustomName();
    if (!this.ss.isGlowBlacklisted(is) && this.ss.isGlow()) {
      item.setGlowing(true);
      if (name != null && name.length() > 2) {
        GlowAPI.Color c2;
        String ss = "f";
        if (name.startsWith("§"))
          ss = name.substring(1, 2); 
        ChatColor cc = ChatColor.getByChar(ss);
        if (cc == null) {
          c2 = GlowAPI.Color.WHITE;
        } else {
          try {
            c2 = GlowAPI.Color.valueOf(cc.name());
          } catch (IllegalArgumentException ex) {
            c2 = GlowAPI.Color.WHITE;
          } 
        } 
        try {
          GlowAPI.setGlowing((Entity)item, c2, this.plugin.getServer().getOnlinePlayers());
        } catch (Exception exception) {}
      } 
    } 
  }
  
  public HintSettings getSettings() {
    return this.ss;
  }
  
  class HintSettings {
    private boolean custom_only;
    
    private boolean glow;
    
    private String format_single;
    
    private String format_multi;
    
    private List<String> hint_black;
    
    private List<String> glow_black;
    
    private List<String> name_black;
    
    private List<String> lore_black;
    
    public HintSettings(boolean custom_only, boolean glow, String format_single, String format_multi, List<String> hint_black, List<String> glow_black, List<String> name_black, List<String> lore_black) {
      this.custom_only = custom_only;
      this.glow = glow;
      this.format_single = ChatColor.translateAlternateColorCodes('&', format_single);
      this.format_multi = ChatColor.translateAlternateColorCodes('&', format_multi);
      this.hint_black = hint_black;
      this.glow_black = glow_black;
      this.name_black = name_black;
      this.lore_black = lore_black;
    }
    
    public boolean isCustomOnly() {
      return this.custom_only;
    }
    
    public boolean isGlow() {
      return this.glow;
    }
    
    public String getFormatSingle() {
      return this.format_single;
    }
    
    public String getFormatMulti() {
      return this.format_multi;
    }
    
    public List<String> getHintBlacklist() {
      return this.hint_black;
    }
    
    public List<String> getGlowBlacklist() {
      return this.glow_black;
    }
    
    public List<String> getNameBlacklist() {
      return this.name_black;
    }
    
    public List<String> getLoreBlacklist() {
      return this.lore_black;
    }
    
    public boolean isHintBlacklisted(ItemStack item) {
      String n = item.getType().name();
      return this.hint_black.contains(n);
    }
    
    public boolean isGlowBlacklisted(ItemStack item) {
      String n = item.getType().name();
      return this.glow_black.contains(n);
    }
  }
}
