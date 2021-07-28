package su.nightexpress.quantumrpg.config;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.utils.ResourceExtractor;

public class ConfigManager {
  private QuantumRPG plugin;
  
  public MyConfig configLang;
  
  public MyConfig configMain;
  
  public MyConfig item_names;
  
  public MyConfig ench_names;
  
  public MyConfig temp_hash;
  
  public ConfigManager(QuantumRPG plugin) {
    this.plugin = plugin;
  }
  
  public void setup() {
    extract("lang/en");
    this.configMain = new MyConfig((JavaPlugin)this.plugin, "", "config.yml");
    Config.setup();
    this.configLang = new MyConfig((JavaPlugin)this.plugin, "/lang/" + Config.getLangCode(), "messages.yml");
    this.item_names = new MyConfig((JavaPlugin)this.plugin, "/lang/" + Config.getLangCode(), "item_names.yml");
    this.ench_names = new MyConfig((JavaPlugin)this.plugin, "/lang/" + Config.getLangCode(), "ench_names.yml");
    this.temp_hash = new MyConfig((JavaPlugin)this.plugin, "", "temp_hash.yml");
    JYML jYML1 = this.item_names.getConfig();
    byte b;
    int i;
    Material[] arrayOfMaterial;
    for (i = (arrayOfMaterial = Material.values()).length, b = 0; b < i; ) {
      Material m = arrayOfMaterial[b];
      if (!jYML1.contains("Material." + m.name())) {
        String name = WordUtils.capitalizeFully(m.name().replace("_", " "));
        jYML1.set("Material." + m.name(), name);
      } 
      b++;
    } 
    JYML jYML2 = this.ench_names.getConfig();
    Enchantment[] arrayOfEnchantment;
    for (int j = (arrayOfEnchantment = Enchantment.values()).length; i < j; ) {
      Enchantment e = arrayOfEnchantment[i];
      if (!jYML2.contains("Enchant." + e.getName())) {
        String name = "&7" + WordUtils.capitalizeFully(e.getName().replace("_", " "));
        jYML2.set("Enchant." + e.getName(), name);
      } 
      i++;
    } 
    this.item_names.save();
    this.ench_names.save();
    Lang.setup(this.configLang);
  }
  
  public void extract(String folder) {
    File f = new File(this.plugin.getDataFolder() + "/" + folder + "/");
    if (!f.exists()) {
      ResourceExtractor extract = new ResourceExtractor((JavaPlugin)this.plugin, new File(this.plugin.getDataFolder() + File.separator + folder), folder, ".*\\.(yml)$");
      try {
        extract.extract(false, true);
      } catch (IOException e) {
        e.printStackTrace();
      } 
    } 
  }
  
  public String getDefaultItemName(ItemStack item) {
    if (this.item_names.getConfig().contains("Material." + item.getType().name()))
      return this.item_names.getConfig().getString("Material." + item.getType().name()); 
    return WordUtils.capitalizeFully(item.getType().name().replace("_", " "));
  }
  
  public String getDefaultEnchantName(Enchantment e) {
    if (this.ench_names.getConfig().contains("Enchant." + e.getName()))
      return ChatColor.translateAlternateColorCodes('&', this.ench_names.getConfig().getString("Enchant." + e.getName())); 
    return WordUtils.capitalizeFully(e.getName().replace("_", " "));
  }
  
  public void createItemHash(String id) {
    JYML jYML = (this.plugin.getCM()).temp_hash.getConfig();
    if (!jYML.contains(id))
      jYML.set(id, UUID.randomUUID().toString()); 
    (this.plugin.getCM()).temp_hash.save();
  }
  
  public UUID getItemHash(String id) {
    JYML jYML = (this.plugin.getCM()).temp_hash.getConfig();
    if (!jYML.contains(id))
      createItemHash(id); 
    String s = jYML.getString(id);
    return UUID.fromString(s);
  }
}
