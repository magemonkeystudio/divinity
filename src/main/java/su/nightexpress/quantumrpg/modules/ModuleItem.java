package su.nightexpress.quantumrpg.modules;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import su.nightexpress.quantumrpg.config.Config;
import su.nightexpress.quantumrpg.nbt.NBTItem;
import su.nightexpress.quantumrpg.types.ItemGroup;
import su.nightexpress.quantumrpg.types.ItemSubType;
import su.nightexpress.quantumrpg.utils.ItemUtils;
import su.nightexpress.quantumrpg.utils.LoreUT;
import su.nightexpress.quantumrpg.utils.Utils;

public abstract class ModuleItem {
  protected String id;
  
  protected String name;
  
  protected String material;
  
  protected List<String> lore;
  
  protected boolean enchanted;
  
  protected String hash;
  
  protected List<String> flags;
  
  protected boolean unbreak;
  
  protected List<String> item_types;
  
  protected EModule module;
  
  public ModuleItem(String id, String name, List<String> lore, EModule module) {
    this.id = id.toLowerCase();
    setName(name);
    setLore(lore);
    this.module = module;
  }
  
  public ModuleItem(String id, String path, FileConfiguration cfg, EModule module) {
    this.id = id.toLowerCase();
    setName(cfg.getString(String.valueOf(path) + "name"));
    if (module == EModule.ARROWS) {
      this.material = "ARROW";
    } else {
      this.material = cfg.getString(String.valueOf(path) + "material", "STONE");
    } 
    setLore(cfg.getStringList(String.valueOf(path) + "lore"));
    this.enchanted = cfg.getBoolean(String.valueOf(path) + "enchanted");
    this.hash = cfg.getString(String.valueOf(path) + "skull-hash");
    this.flags = cfg.getStringList(String.valueOf(path) + "item-flags");
    this.unbreak = cfg.getBoolean(String.valueOf(path) + "unbreakable");
    this.item_types = cfg.getStringList(String.valueOf(path) + "item-types");
    this.module = module;
  }
  
  protected String color(String s) {
    if (s == null)
      return ""; 
    return ChatColor.translateAlternateColorCodes('&', s);
  }
  
  public String getId() {
    return this.id;
  }
  
  public void setName(String name) {
    this.name = color(name);
  }
  
  public String getName() {
    return this.name;
  }
  
  public String getMaterial() {
    return this.material;
  }
  
  public void setLore(List<String> lore) {
    List<String> l = new ArrayList<>();
    for (String s : lore)
      l.add(color(s)); 
    this.lore = l;
  }
  
  public List<String> getLore() {
    return this.lore;
  }
  
  public EModule getType() {
    return this.module;
  }
  
  public ItemStack setModule(ItemStack item) {
    NBTItem nbt = new NBTItem(item);
    nbt.setString("E_MODULE", getType().name());
    return nbt.getItem();
  }
  
  public ItemStack setItemId(ItemStack item, String id) {
    NBTItem nbt = new NBTItem(item);
    nbt.setString("E_ITEM_ID", id.toLowerCase());
    return nbt.getItem();
  }
  
  public boolean isValidType(ItemStack target) {
    if (this.item_types.isEmpty())
      return true; 
    for (String s : this.item_types) {
      s = s.replace("*", "");
      ItemSubType ist = Config.getSubTypeById(s);
      if (ist != null && ist.isItemOfThis(target))
        return true; 
      try {
        ItemGroup ig = ItemGroup.valueOf(s.toUpperCase());
        if (ig.isItemOfThis(target))
          return true; 
      } catch (IllegalArgumentException illegalArgumentException) {}
      if (target.getType().name().equalsIgnoreCase(s))
        return true; 
    } 
    return false;
  }
  
  public ItemStack create() {
    return build();
  }
  
  protected ItemStack build() {
    ItemStack item = Utils.buildItem(this.material);
    if (item == null)
      return new ItemStack(Material.AIR); 
    item = Utils.getHashed(item, this.hash, this.id);
    ItemMeta meta = item.getItemMeta();
    List<String> lore = new ArrayList<>(this.lore);
    meta.setDisplayName(replacePlaceholders(this.name));
    for (int i = 0; i < lore.size(); i++) {
      String line = lore.get(i);
      lore.set(i, replacePlaceholders(line));
    } 
    meta.setLore(lore);
    if (this.flags.contains("*")) {
      meta.addItemFlags(ItemFlag.values());
    } else {
      for (String flag : this.flags) {
        try {
          meta.addItemFlags(new ItemFlag[] { ItemFlag.valueOf(flag.toUpperCase()) });
        } catch (IllegalArgumentException illegalArgumentException) {}
      } 
    } 
    meta.spigot().setUnbreakable(this.unbreak);
    if (this.enchanted)
      meta.addEnchant(Enchantment.ARROW_DAMAGE, 1, true); 
    item.setItemMeta(meta);
    item = setItemId(item, this.id);
    item = setModule(item);
    return item;
  }
  
  protected String replacePlaceholders(String s) {
    String type = "";
    if (!this.item_types.isEmpty()) {
      List<String> list = new ArrayList<>();
      for (String ss : this.item_types) {
        String g1 = ItemUtils.getGroupName(ss.replace("*", ""));
        String g2 = ItemUtils.getItemGroupName(ss.replace("*", ""));
        if (g1 == null && g2 == null)
          continue; 
        String gg = "";
        if (g1 != null) {
          gg = g1;
        } else {
          gg = g2;
        } 
        list.add(gg);
      } 
      String pref = Config.str_Req_Cls_Item_Single;
      String[] arr = list.<String>toArray(new String[list.size()]);
      type = pref.replace("%type%", LoreUT.getStrSeparated(arr));
    } 
    return color(s.replace("%type%", type).replace("%item-types%", type));
  }
}
