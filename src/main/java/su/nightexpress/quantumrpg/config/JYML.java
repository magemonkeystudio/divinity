package su.nightexpress.quantumrpg.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import su.nightexpress.quantumrpg.gui.ContentType;
import su.nightexpress.quantumrpg.gui.GUIItem;
import su.nightexpress.quantumrpg.utils.LocUT;
import su.nightexpress.quantumrpg.utils.Utils;
import su.nightexpress.quantumrpg.utils.logs.LogType;
import su.nightexpress.quantumrpg.utils.logs.LogUtil;

public class JYML extends YamlConfiguration {
  private File f;
  
  public JYML(String path, String file) {
    this.f = new File(path, file);
    try {
      load(this.f);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InvalidConfigurationException e) {
      e.printStackTrace();
    } 
  }
  
  public JYML(File f) {
    this.f = f;
    try {
      load(this.f);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InvalidConfigurationException e) {
      e.printStackTrace();
    } 
  }
  
  public Set<String> getSection(String path) {
    if (!isConfigurationSection(path))
      return Collections.emptySet(); 
    return getConfigurationSection(path).getKeys(false);
  }
  
  public void set(String path, Location loc) {
    String raw = LocUT.serialize(loc);
    set(path, raw);
  }
  
  public Location getLocation(String path) {
    return LocUT.deserialize(getString(path));
  }
  
  public static List<JYML> getFilesFolder(String path) {
    List<JYML> names = new ArrayList<>();
    File folder = new File(path);
    File[] listOfFiles = folder.listFiles();
    if (listOfFiles == null)
      return names; 
    byte b;
    int i;
    File[] arrayOfFile1;
    for (i = (arrayOfFile1 = listOfFiles).length, b = 0; b < i; ) {
      File f = arrayOfFile1[b];
      if (f.isFile()) {
        names.add(new JYML(f));
      } else if (f.isDirectory()) {
        names.addAll(getFilesFolder(f.getPath()));
      } 
      b++;
    } 
    return names;
  }
  
  public ItemStack getItemFromSection(String path) {
    if (!path.endsWith("."))
      path = String.valueOf(path) + "."; 
    String mat = getString(String.valueOf(path) + "material");
    ItemStack item = Utils.buildItem(mat);
    if (item == null) {
      LogUtil.send("Invalid item material on &f'" + path + "'!" + " &c(" + this.f.getName() + ")", LogType.ERROR);
      return null;
    } 
    String hash = getString(String.valueOf(path) + "skull-hash");
    if (hash != null) {
      String[] ss = path.split("\\.");
      String id = ss[ss.length - 1];
      item = Utils.getHashed(item, hash, id);
    } 
    ItemMeta meta = item.getItemMeta();
    String name = getString(String.valueOf(path) + "name");
    if (name != null)
      meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name)); 
    List<String> lore = new ArrayList<>();
    for (String s : getStringList(String.valueOf(path) + "lore"))
      lore.add(ChatColor.translateAlternateColorCodes('&', s)); 
    meta.setLore(lore);
    if (getBoolean(String.valueOf(path) + "enchanted"))
      meta.addEnchant(Enchantment.ARROW_DAMAGE, 1, true); 
    List<String> flags = getStringList(String.valueOf(path) + "item-flags");
    if (flags.contains("*")) {
      meta.addItemFlags(ItemFlag.values());
    } else {
      for (String flag : flags) {
        try {
          meta.addItemFlags(new ItemFlag[] { ItemFlag.valueOf(flag.toUpperCase()) });
        } catch (IllegalArgumentException illegalArgumentException) {}
      } 
    } 
    meta.setUnbreakable(getBoolean(String.valueOf(path) + "unbreakable"));
    item.setItemMeta(meta);
    return item;
  }
  
  public GUIItem getGUIItemFromSection(String path, Class<? extends Enum<?>> e) {
    ContentType type;
    if (!path.endsWith("."))
      path = String.valueOf(path) + "."; 
    ItemStack item = getItemFromSection(path);
    ItemMeta meta = item.getItemMeta();
    meta.addItemFlags(ItemFlag.values());
    meta.setUnbreakable(true);
    item.setItemMeta(meta);
    int[] slots = new int[1];
    if (contains(String.valueOf(path) + "slots")) {
      String[] raw = getString(String.valueOf(path) + "slots").replaceAll("\\s", "").split(",");
      slots = new int[raw.length];
      for (int i = 0; i < raw.length; i++) {
        try {
          slots[i] = Integer.parseInt(raw[i].trim());
        } catch (NumberFormatException numberFormatException) {}
      } 
    } 
    try {
      type = ContentType.valueOf(getString(String.valueOf(path) + "type", "NONE"));
    } catch (IllegalArgumentException ex) {
      type = ContentType.NONE;
    } 
    String[] ss = path.split("\\.");
    String id = ss[ss.length - 1];
    if (id.isEmpty())
      id = String.valueOf(this.f.getName().replace(".yml", "")) + "-icon-" + Utils.randInt(0, 3000); 
    GUIItem gi = new GUIItem(id, type, item, slots);
    return gi;
  }
  
  public void saveItemToSection(ItemStack item, String path) {
    if (item == null)
      return; 
    if (!path.endsWith("."))
      path = String.valueOf(path) + "."; 
    Material m = item.getType();
    ItemMeta meta = item.getItemMeta();
    int data = item.getDurability();
    String mat = String.valueOf(m.name()) + ":" + data + ":" + item.getAmount();
    set(String.valueOf(path) + "material", mat);
    if (meta.hasDisplayName())
      set(String.valueOf(path) + "name", meta.getDisplayName()); 
    if (meta.hasLore())
      set(String.valueOf(path) + "lore", meta.getLore()); 
    String hash = Utils.getHashOf(item);
    if (hash != null && !hash.isEmpty())
      set(String.valueOf(path) + "skull-hash", hash); 
    if (meta.hasEnchants())
      set(String.valueOf(path) + "enchanted", Boolean.valueOf(true)); 
    List<String> f2 = new ArrayList<>();
    Set<ItemFlag> flags = meta.getItemFlags();
    for (ItemFlag f : flags)
      f2.add(f.name()); 
    set(String.valueOf(path) + "item-flags", f2);
    set(String.valueOf(path) + "unbreakable", Boolean.valueOf(meta.isUnbreakable()));
  }
  
  public void addMissing(String path, Object val) {
    if (contains(path))
      return; 
    set(path, val);
  }
  
  public File getFile() {
    return this.f;
  }
  
  public String getFileName() {
    return this.f.getName();
  }
  
  public void save() {
    try {
      save(this.f);
    } catch (IOException e) {
      LogUtil.send("Unable to save config: &f" + this.f.getName() + "&7! &c(" + e.getMessage() + ")", LogType.ERROR);
    } 
  }
}
