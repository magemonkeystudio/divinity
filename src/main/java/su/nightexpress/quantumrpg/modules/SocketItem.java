package su.nightexpress.quantumrpg.modules;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import su.nightexpress.quantumrpg.nbt.NBTItem;
import su.nightexpress.quantumrpg.utils.Utils;

public abstract class SocketItem extends LeveledItem {
  protected int min_suc;
  
  protected int max_suc;
  
  public SocketItem(String id, String path, FileConfiguration cfg, EModule module) {
    super(id, path, cfg, module);
    this.min_suc = Math.max(0, cfg.getInt(String.valueOf(path) + "min-success-rate"));
    this.max_suc = Math.min(100, cfg.getInt(String.valueOf(path) + "max-success-rate"));
  }
  
  public void setMinSuccessRate(int min_suc) {
    this.min_suc = Math.max(0, min_suc);
  }
  
  public int getMinSuccessRate() {
    return this.min_suc;
  }
  
  public void setMaxSuccessRate(int max_suc) {
    this.max_suc = Math.min(100, max_suc);
  }
  
  public int getMaxSuccessRate() {
    return this.max_suc;
  }
  
  public ItemStack setSocketRate(ItemStack item, int rate) {
    NBTItem nbt = new NBTItem(item);
    nbt.setInteger("E_ITEM_RATE", Integer.valueOf(rate));
    return nbt.getItem();
  }
  
  public ItemStack create() {
    return create(-1);
  }
  
  public ItemStack create(int lvl) {
    return create(lvl, -1);
  }
  
  public ItemStack create(int lvl, int success) {
    if (lvl == -1) {
      lvl = Utils.randInt(getMinLevel(), getMaxLevel());
    } else if (lvl > getMaxLevel()) {
      lvl = getMaxLevel();
    } else if (lvl < getMinLevel()) {
      lvl = getMinLevel();
    } 
    if (success > 100) {
      success = 100;
    } else if (success < 0) {
      success = Utils.randInt(getMinSuccessRate(), getMaxSuccessRate());
    } 
    return build(lvl, success);
  }
  
  protected ItemStack build(int lvl, int suc) {
    ItemStack item = Utils.buildItem(this.material);
    if (item == null)
      return new ItemStack(Material.AIR); 
    item = Utils.getHashed(item, this.hash, this.id);
    ItemMeta meta = item.getItemMeta();
    meta.setDisplayName(replacePlaceholders(this.name, lvl, suc));
    List<String> lore = new ArrayList<>(this.lore);
    for (int i = 0; i < lore.size(); i++) {
      String line = lore.get(i);
      lore.set(i, replacePlaceholders(line, lvl, suc));
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
    item = setSocketRate(item, suc);
    item = setModule(item);
    return item;
  }
  
  protected String replacePlaceholders(String s, int lvl, int suc) {
    s = replacePlaceholders(s, lvl);
    s = s
      .replace("%s%", String.valueOf(suc))
      .replace("%d%", String.valueOf(100 - suc))
      .replace("%success%", String.valueOf(suc))
      .replace("%failure%", String.valueOf(100 - suc));
    return s;
  }
}
