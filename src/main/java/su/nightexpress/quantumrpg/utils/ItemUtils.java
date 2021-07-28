package su.nightexpress.quantumrpg.utils;

import java.util.List;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.config.Config;
import su.nightexpress.quantumrpg.config.Lang;
import su.nightexpress.quantumrpg.modules.EModule;
import su.nightexpress.quantumrpg.modules.sets.SetManager;
import su.nightexpress.quantumrpg.modules.soulbound.SoulboundManager;
import su.nightexpress.quantumrpg.stats.ItemStat;
import su.nightexpress.quantumrpg.types.ItemGroup;
import su.nightexpress.quantumrpg.types.ItemSubType;

public class ItemUtils {
  private static QuantumRPG plugin = QuantumRPG.instance;
  
  public static ItemStack replaceEnchants(ItemStack item) {
    int pos = item.getItemMeta().getLore().indexOf("%ENCHANTS%");
    if (pos < 0)
      return item; 
    if (item.hasItemMeta() && item.getItemMeta().hasEnchants()) {
      ItemMeta meta = item.getItemMeta();
      List<String> lore = meta.getLore();
      for (Enchantment e : meta.getEnchants().keySet()) {
        String value = String.valueOf(plugin.getCM().getDefaultEnchantName(e)) + " " + Utils.IntegerToRomanNumeral(meta.getEnchantLevel(e));
        lore.add(pos, value);
      } 
      meta.setLore(lore);
      item.setItemMeta(meta);
    } 
    item = replaceLore(item, "ENCHANTS", "delz");
    return item;
  }
  
  public static String getItemStatString(ItemStack item, ItemStat type, double val) {
    String f1 = type.getFormat();
    boolean bonus = !type.isMainItem(item);
    String add = "";
    if (type == ItemStat.ATTACK_SPEED && !isWeapon(item))
      return add; 
    val = type.fineValue(val);
    if (val == 0.0D)
      return add; 
    switch (type) {
      case DURABILITY:
        if (!bonus) {
          int i = (int)val;
          if (i > 0) {
            add = String.valueOf(f1) + i + Config.str_durSep + type.getValue() + i;
          } else {
            add = String.valueOf(f1) + Config.str_durUnb;
          } 
        } 
        return add;
    } 
    double d1 = val;
    if (type != ItemStat.RANGE || (item.getType() != Material.BOW && d1 > 0.0D)) {
      if (bonus) {
        if (d1 > 0.0D) {
          add = String.valueOf(Config.str_positive) + val + Config.str_procent;
        } else {
          add = String.valueOf(Config.str_negative) + val + Config.str_procent;
        } 
      } else {
        add = String.valueOf(val);
        if (type.isPlus())
          if (d1 > 0.0D) {
            add = String.valueOf(Config.str_positive) + String.valueOf(val);
          } else {
            add = String.valueOf(Config.str_negative) + String.valueOf(val);
          }  
        if (type == ItemStat.CRITICAL_DAMAGE) {
          add = String.valueOf(add) + Config.str_Modifier;
        } else if (type.isPercent()) {
          add = String.valueOf(add) + Config.str_procent;
        } 
      } 
      add = String.valueOf(f1) + add;
    } 
    return add;
  }
  
  public static ItemStack replaceSet(ItemStack item) {
    if (!EModule.SETS.isEnabled())
      return replaceLore(item, "SET", "delz"); 
    SetManager set = (SetManager)plugin.getMM().getModule(EModule.SETS);
    int pos = item.getItemMeta().getLore().indexOf("%SET%");
    if (pos < 0)
      return item; 
    item = set.replaceLore(item);
    return replaceLore(item, "SET", "delz");
  }
  
  public static ItemStack replaceSoul(ItemStack item) {
    if (!EModule.SOULBOUND.isEnabled())
      return replaceLore(item, "SOULBOUND", "delz"); 
    SoulboundManager s = (SoulboundManager)plugin.getMM().getModule(EModule.SOULBOUND);
    return replaceLore(item, "SOULBOUND", s.getSoulString());
  }
  
  public static ItemStack replaceUntrade(ItemStack item) {
    if (!EModule.SOULBOUND.isEnabled())
      return replaceLore(item, "UNTRADEABLE", "delz"); 
    SoulboundManager s = (SoulboundManager)plugin.getMM().getModule(EModule.SOULBOUND);
    return replaceLore(item, "UNTRADEABLE", s.getUntradeString());
  }
  
  public static ItemStack replaceLevel(ItemStack item, int lvl) {
    item = NBTUtils.setItemLevel(item, lvl);
    String lvl_no = Config.str_Req_Lvl_User_Single.replace("%lvl%", String.valueOf(lvl)).replace("%state%", Lang.Lore_State_false.toMsg());
    return replaceLore(item, "LEVEL", lvl_no);
  }
  
  public static ItemStack replaceLore(ItemStack item, String type, String replacer) {
    ItemMeta meta = item.getItemMeta();
    List<String> lore = meta.getLore();
    if (lore == null)
      return item; 
    String newt = "%" + type.toUpperCase() + "%";
    int pos = 0;
    for (String s : lore) {
      if (s.contains(newt)) {
        pos = lore.indexOf(s);
        lore.remove(pos);
        if (!replacer.equals("delz") && !replacer.equals(""))
          lore.add(pos, s.replace(newt, replacer)); 
        break;
      } 
    } 
    meta.setLore(lore);
    item.setItemMeta(meta);
    return item;
  }
  
  public static String getSlotByItemType(ItemStack item) {
    String s = item.getType().name();
    if (s.contains("HELMET") || s.contains("SKULL_ITEM"))
      return "head"; 
    if (s.contains("CHESTPLATE") || s.contains("ELYTRA"))
      return "chest"; 
    if (s.contains("LEGGINGS"))
      return "legs"; 
    if (s.contains("BOOTS"))
      return "feet"; 
    if (s.contains("SHIELD"))
      return "offhand"; 
    return "mainhand";
  }
  
  public static String[] getAllNBTSlots(ItemStack item) {
    if (isArmor(item) || !Config.allowAttributesToOffHand())
      return new String[] { getSlotByItemType(item) }; 
    return new String[] { "offhand", "mainhand" };
  }
  
  public static String getValidSkullName(Entity e) {
    EntityType et = e.getType();
    switch (et) {
      case WITHER_SKELETON:
        return "MHF_WSkeleton";
      case MAGMA_CUBE:
        return "MHF_LavaSlime";
      case ELDER_GUARDIAN:
        return "MHF_EGuardian";
    } 
    String s = et.name().toLowerCase().replace("_", " ");
    return "MHF_" + WordUtils.capitalizeFully(s.replace(" ", ""));
  }
  
  public static String getGroupName(String type) {
    ItemSubType ist = Config.getSubTypeById(type);
    if (ist != null)
      return ist.getName(); 
    try {
      ItemGroup ig = ItemGroup.valueOf(type.toUpperCase());
      return ig.getName();
    } catch (IllegalArgumentException illegalArgumentException) {
      return null;
    } 
  }
  
  public static String getItemGroupName(String type) {
    ItemSubType ist = Config.getItemSubType(type);
    if (ist != null)
      return ist.getName(); 
    ItemGroup ig = ItemGroup.getItemGroup(type);
    if (ig != null)
      return ig.getName(); 
    Material m = Material.getMaterial(type.toUpperCase());
    if (m != null)
      return plugin.getCM().getDefaultItemName(new ItemStack(m)); 
    return null;
  }
  
  public static boolean isValidItemGroup(String group) {
    ItemSubType ist = Config.getSubTypeById(group);
    if (ist != null)
      return true; 
    try {
      ItemGroup.valueOf(group.toUpperCase());
      return true;
    } catch (IllegalArgumentException illegalArgumentException) {
      Material m = Material.getMaterial(group.toUpperCase());
      if (m != null)
        return true; 
      return false;
    } 
  }
  
  public static boolean isWeapon(ItemStack item) {
    return !(!ItemGroup.WEAPON.isItemOfThis(item) && !ItemGroup.TOOL.isItemOfThis(item));
  }
  
  public static boolean isArmor(ItemStack item) {
    return ItemGroup.ARMOR.isItemOfThis(item);
  }
  
  public static boolean isTool(ItemStack item) {
    return ItemGroup.TOOL.isItemOfThis(item);
  }
  
  public static boolean isBow(ItemStack item) {
    return (item != null && item.getType() == Material.BOW);
  }
}
