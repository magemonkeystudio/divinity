package su.nightexpress.quantumrpg.gui;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import su.nightexpress.quantumrpg.nbt.NBTItem;
import su.nightexpress.quantumrpg.utils.Utils;
import su.nightexpress.quantumrpg.utils.logs.LogType;
import su.nightexpress.quantumrpg.utils.logs.LogUtil;

public class GUIUtils {
  private static final String key = "jGUI";
  
  private static final String PAGE = "jGUI_PAGE";
  
  private static final String ID = "jGUI_ID";
  
  public static ContentType getItemType(ItemStack item) {
    if (item == null || item.getType() == Material.AIR)
      return ContentType.NONE; 
    NBTItem nbt = new NBTItem(item);
    byte b;
    int i;
    ContentType[] arrayOfContentType;
    for (i = (arrayOfContentType = ContentType.values()).length, b = 0; b < i; ) {
      ContentType ct = arrayOfContentType[b];
      String cc = ct.name();
      if (nbt.hasKey(cc).booleanValue())
        return ct; 
      b++;
    } 
    return ContentType.NONE;
  }
  
  public static ItemStack setPage(ItemStack item, int page) {
    if (item == null || item.getType() == Material.AIR)
      return item; 
    NBTItem nbt = new NBTItem(item);
    nbt.setInteger("jGUI_PAGE", Integer.valueOf(page));
    return nbt.getItem();
  }
  
  public static int getPage(ItemStack item) {
    if (item == null || item.getType() == Material.AIR)
      return 1; 
    NBTItem nbt = new NBTItem(item);
    if (!nbt.hasKey("jGUI_PAGE").booleanValue())
      return 1; 
    return nbt.getInteger("jGUI_PAGE").intValue();
  }
  
  public static ItemStack setId(ItemStack item, String id) {
    if (item == null || item.getType() == Material.AIR)
      return item; 
    NBTItem nbt = new NBTItem(item);
    nbt.setString("jGUI_ID", id);
    return nbt.getItem();
  }
  
  public static String getId(ItemStack item) {
    if (item == null || item.getType() == Material.AIR)
      return ""; 
    NBTItem nbt = new NBTItem(item);
    if (!nbt.hasKey("jGUI_ID").booleanValue())
      return ""; 
    return nbt.getString("jGUI_ID");
  }
  
  public static GUIItem getItemFromSection(FileConfiguration cfg, String id, String path) {
    String mat = cfg.getString(String.valueOf(path) + "material");
    ItemStack item = Utils.buildItem(mat);
    if (item == null) {
      LogUtil.send("Invalid item material in '" + path + "'!", LogType.WARN);
      return null;
    } 
    String hash = cfg.getString(String.valueOf(path) + "skull-hash");
    if (hash != null)
      item = Utils.getHashed(item, hash, null); 
    ItemMeta meta = item.getItemMeta();
    String name = cfg.getString(String.valueOf(path) + "name");
    if (name != null)
      meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name)); 
    List<String> lore = new ArrayList<>();
    for (String s : cfg.getStringList(String.valueOf(path) + "lore"))
      lore.add(ChatColor.translateAlternateColorCodes('&', s)); 
    meta.setLore(lore);
    if (cfg.getBoolean(String.valueOf(path) + "enchanted", false))
      meta.addEnchant(Enchantment.ARROW_DAMAGE, 1, true); 
    meta.addItemFlags(ItemFlag.values());
    meta.setUnbreakable(true);
    item.setItemMeta(meta);
    int[] slots = new int[1];
    if (cfg.contains(String.valueOf(path) + "slots")) {
      String[] raw = cfg.getString(String.valueOf(path) + "slots").replaceAll("\\s", "").split(",");
      slots = new int[raw.length];
      for (int i = 0; i < raw.length; i++) {
        try {
          slots[i] = Integer.parseInt(raw[i].trim());
        } catch (NumberFormatException numberFormatException) {}
      } 
    } 
    ContentType type = ContentType.NONE;
    if (cfg.contains(String.valueOf(path) + "type"))
      try {
        type = ContentType.valueOf(cfg.getString(String.valueOf(path) + "type").toUpperCase());
      } catch (IllegalArgumentException ex) {
        LogUtil.send("Invalid item type for '" + path + "'!", LogType.WARN);
        type = ContentType.NONE;
      }  
    GUIItem gi = new GUIItem(id, type, item, slots);
    return gi;
  }
}
