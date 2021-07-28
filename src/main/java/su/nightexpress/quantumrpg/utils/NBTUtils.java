package su.nightexpress.quantumrpg.utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.quantumrpg.nbt.NBTItem;

public class NBTUtils {
  public static final String EMODULE = "E_MODULE";
  
  public static final String ITEM_ID = "E_ITEM_ID";
  
  private static final String ITEM_LVL = "E_ITEM_LVL";
  
  private static final String ITEM_CLASS = "E_ITEM_CLASS";
  
  public static final String ITEM_RATE = "E_ITEM_RATE";
  
  public static ItemStack setItemLevel(ItemStack item, int lvl) {
    NBTItem nbt = new NBTItem(item);
    if (lvl > 0) {
      nbt.setInteger("E_ITEM_LVL", Integer.valueOf(lvl));
    } else {
      nbt.removeKey("E_ITEM_LVL");
    } 
    return nbt.getItem();
  }
  
  public static int getItemLevel(ItemStack item) {
    if (item == null || item.getType() == Material.AIR)
      return -1; 
    NBTItem nbt = new NBTItem(item);
    if (nbt.hasKey("E_ITEM_LVL").booleanValue())
      return nbt.getInteger("E_ITEM_LVL").intValue(); 
    return -1;
  }
  
  public static ItemStack setItemClass(ItemStack item, String[] cls) {
    if (item == null || item.getType() == Material.AIR)
      return item; 
    NBTItem nbt = new NBTItem(item);
    if (cls != null && cls.length > 0) {
      String cc = "";
      byte b;
      int i;
      String[] arrayOfString;
      for (i = (arrayOfString = cls).length, b = 0; b < i; ) {
        String val = arrayOfString[b];
        val = ChatColor.stripColor(val);
        if (cc.isEmpty()) {
          cc = String.valueOf(cc) + val;
        } else {
          cc = String.valueOf(cc) + "," + val;
        } 
        b++;
      } 
      nbt.setString("E_ITEM_CLASS", cc);
    } else {
      nbt.removeKey("E_ITEM_CLASS");
    } 
    return nbt.getItem();
  }
  
  public static String[] getItemClasses(ItemStack item) {
    if (item == null || item.getType() == Material.AIR)
      return null; 
    NBTItem nbt = new NBTItem(item);
    if (nbt.hasKey("E_ITEM_CLASS").booleanValue())
      return ChatColor.stripColor(nbt.getString("E_ITEM_CLASS")).split(","); 
    return null;
  }
}
