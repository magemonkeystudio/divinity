package su.nightexpress.quantumrpg.modules;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import su.nightexpress.quantumrpg.utils.LoreUT;
import su.nightexpress.quantumrpg.utils.NBTUtils;
import su.nightexpress.quantumrpg.utils.Utils;

public abstract class LeveledItem extends ModuleItem {
  protected int min_lvl;
  
  protected int max_lvl;
  
  protected Map<Integer, String> lvl_req;
  
  public LeveledItem(String id, String name, List<String> lore, EModule module) {
    super(id, name, lore, module);
  }
  
  public LeveledItem(String id, String path, FileConfiguration cfg, EModule module) {
    super(id, path, cfg, module);
    this.min_lvl = cfg.getInt(String.valueOf(path) + "min-level");
    this.max_lvl = cfg.getInt(String.valueOf(path) + "max-level");
    this.lvl_req = new TreeMap<>();
    if (cfg.isConfigurationSection(String.valueOf(path) + "item-level-requirements"))
      for (String s : cfg.getConfigurationSection(String.valueOf(path) + "item-level-requirements").getKeys(false)) {
        int lvl = Integer.parseInt(s);
        String range = cfg.getString(String.valueOf(path) + "item-level-requirements." + s);
        this.lvl_req.put(Integer.valueOf(lvl), range);
      }  
  }
  
  public LeveledItem(String id, String name, List<String> lore, EModule module, int min_lvl, int max_lvl) {
    super(id, name, lore, module);
    this.min_lvl = min_lvl;
    this.max_lvl = max_lvl;
  }
  
  public void setMinLevel(int min_lvl) {
    this.min_lvl = min_lvl;
  }
  
  public int getMinLevel() {
    return this.min_lvl;
  }
  
  public void setMaxLevel(int max_lvl) {
    this.max_lvl = max_lvl;
  }
  
  public int getMaxLevel() {
    return this.max_lvl;
  }
  
  public boolean hasLevelRequirements() {
    return !this.lvl_req.isEmpty();
  }
  
  public int getMinLevelRequirement(int lvl) {
    if (!hasLevelRequirements())
      return -1; 
    if (this.lvl_req.containsKey(Integer.valueOf(lvl)))
      return getLevelRequirement(lvl, 0); 
    int last = -1;
    for (Iterator<Integer> iterator = this.lvl_req.keySet().iterator(); iterator.hasNext(); ) {
      int i = ((Integer)iterator.next()).intValue();
      if (lvl >= i)
        last = i; 
    } 
    return getLevelRequirement(last, 0);
  }
  
  public int getMaxLevelRequirement(int lvl) {
    if (!hasLevelRequirements())
      return -1; 
    if (this.lvl_req.containsKey(Integer.valueOf(lvl)))
      return getLevelRequirement(lvl, 1); 
    int last = -1;
    for (Iterator<Integer> iterator = this.lvl_req.keySet().iterator(); iterator.hasNext(); ) {
      int i = ((Integer)iterator.next()).intValue();
      if (lvl >= i)
        last = i; 
    } 
    return getLevelRequirement(last, 1);
  }
  
  private int getLevelRequirement(int lvl, int i) {
    String range = this.lvl_req.get(Integer.valueOf(lvl));
    if (range == null)
      return -1; 
    if (range.contains("-"))
      return Integer.parseInt(range.split("-")[i]); 
    return Integer.parseInt(range);
  }
  
  public ItemStack setLevel(ItemStack item, int lvl) {
    return NBTUtils.setItemLevel(item, lvl);
  }
  
  public ItemStack create() {
    return create(-1);
  }
  
  public ItemStack create(int lvl) {
    if (lvl == -1) {
      lvl = Utils.randInt(getMinLevel(), getMaxLevel());
    } else if (lvl > getMaxLevel()) {
      lvl = getMaxLevel();
    } else if (lvl < getMinLevel()) {
      lvl = getMinLevel();
    } 
    return build(lvl);
  }
  
  protected ItemStack build(int lvl) {
    ItemStack item = Utils.buildItem(this.material);
    if (item == null)
      return new ItemStack(Material.AIR); 
    item = Utils.getHashed(item, this.hash, this.id);
    ItemMeta meta = item.getItemMeta();
    meta.setDisplayName(replacePlaceholders(this.name, lvl));
    List<String> lore = new ArrayList<>(this.lore);
    for (int i = 0; i < lore.size(); i++) {
      String line = lore.get(i);
      lore.set(i, replacePlaceholders(line, lvl));
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
    item = setLevel(item, lvl);
    item = setModule(item);
    return item;
  }
  
  protected String replacePlaceholders(String s, int lvl) {
    s = replacePlaceholders(s);
    s = s
      .replace("%item-level%", LoreUT.getStrItemLevelReq(getMinLevelRequirement(lvl), getMaxLevelRequirement(lvl)))
      .replace("%level%", String.valueOf(lvl))
      .replace("%rlevel%", Utils.IntegerToRomanNumeral(lvl));
    return s;
  }
}
