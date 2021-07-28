package su.nightexpress.quantumrpg.utils;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import su.nightexpress.quantumrpg.config.Config;
import su.nightexpress.quantumrpg.config.Lang;

public class LoreUT {
  public static String getStrItemLevelReq(int min, int max) {
    if (min < 0)
      min = 0; 
    if (max < 0)
      max = 0; 
    if (min == max)
      return Config.str_Req_Lvl_Item_Single.replace("%min%", String.valueOf(min)); 
    return Config.str_Req_Lvl_Item_Range
      .replace("%min%", String.valueOf(min))
      .replace("%max%", String.valueOf(max));
  }
  
  public static int getUserLevelIndex(ItemStack item) {
    if (item == null || !item.hasItemMeta())
      return -1; 
    int lvl = NBTUtils.getItemLevel(item);
    if (lvl < 0)
      return -1; 
    ItemMeta meta = item.getItemMeta();
    if (!meta.hasLore())
      return -1; 
    String lvl_has = Config.str_Req_Lvl_User_Single.replace("%lvl%", String.valueOf(lvl)).replace("%state%", Lang.Lore_State_true.toMsg());
    String lvl_no = Config.str_Req_Lvl_User_Single.replace("%lvl%", String.valueOf(lvl)).replace("%state%", Lang.Lore_State_false.toMsg());
    int pos = meta.getLore().indexOf(lvl_has);
    if (pos < 0)
      pos = meta.getLore().indexOf(lvl_no); 
    return pos;
  }
  
  public static int getUserClassIndex(ItemStack item) {
    if (item == null || !item.hasItemMeta())
      return -1; 
    ItemMeta meta = item.getItemMeta();
    if (!meta.hasLore())
      return -1; 
    String[] cls = NBTUtils.getItemClasses(item);
    if (cls == null || cls.length <= 0)
      return -1; 
    String values = getStrSeparated(cls);
    String c_has = Config.str_Req_Cls_User_Single.replace("%class%", String.valueOf(values)).replace("%state%", Lang.Lore_State_true.toMsg());
    String c_no = Config.str_Req_Cls_User_Single.replace("%class%", String.valueOf(values)).replace("%state%", Lang.Lore_State_false.toMsg());
    int pos = meta.getLore().indexOf(c_has);
    if (pos < 0)
      pos = meta.getLore().indexOf(c_no); 
    return pos;
  }
  
  public static String getStrSeparated(String[] values) {
    String sep = Config.str_Separ_Char;
    String cls = "";
    byte b;
    int i;
    String[] arrayOfString;
    for (i = (arrayOfString = values).length, b = 0; b < i; ) {
      String c = arrayOfString[b];
      if (cls.isEmpty()) {
        cls = String.valueOf(cls) + Config.str_Separ_Color + c;
      } else {
        cls = String.valueOf(cls) + sep + Config.str_Separ_Color + c;
      } 
      b++;
    } 
    return cls;
  }
}
