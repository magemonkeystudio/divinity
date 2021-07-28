package su.nightexpress.quantumrpg.api;

import java.util.ArrayList;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.api.events.QuantumItemDamageEvent;
import su.nightexpress.quantumrpg.config.Config;
import su.nightexpress.quantumrpg.config.Lang;
import su.nightexpress.quantumrpg.modules.EModule;
import su.nightexpress.quantumrpg.modules.LeveledItem;
import su.nightexpress.quantumrpg.modules.ModuleItem;
import su.nightexpress.quantumrpg.modules.QModule;
import su.nightexpress.quantumrpg.modules.QModuleDrop;
import su.nightexpress.quantumrpg.modules.SocketItem;
import su.nightexpress.quantumrpg.modules.identify.IdentifyManager;
import su.nightexpress.quantumrpg.modules.refine.RefineManager;
import su.nightexpress.quantumrpg.modules.soulbound.SoulboundManager;
import su.nightexpress.quantumrpg.nbt.NBTItem;
import su.nightexpress.quantumrpg.stats.ItemStat;
import su.nightexpress.quantumrpg.types.AmmoType;
import su.nightexpress.quantumrpg.types.ArmorType;
import su.nightexpress.quantumrpg.types.DamageType;
import su.nightexpress.quantumrpg.types.QSlotType;
import su.nightexpress.quantumrpg.types.WpnHand;
import su.nightexpress.quantumrpg.utils.ItemUtils;
import su.nightexpress.quantumrpg.utils.LoreUT;
import su.nightexpress.quantumrpg.utils.NBTUtils;
import su.nightexpress.quantumrpg.utils.Utils;
import su.nightexpress.quantumrpg.utils.logs.LogType;
import su.nightexpress.quantumrpg.utils.logs.LogUtil;
import su.nightexpress.quantumrpg.utils.msg.MsgUT;

public class ItemAPI {
  private static QuantumRPG plugin = QuantumRPG.instance;
  
  public static boolean hasOwner(ItemStack item) {
    if (EModule.SOULBOUND.isEnabled()) {
      SoulboundManager s = (SoulboundManager)plugin.getMM().getModule(EModule.SOULBOUND);
      return s.hasOwner(item);
    } 
    return false;
  }
  
  public static boolean isOwner(ItemStack item, Player p) {
    if (EModule.SOULBOUND.isEnabled()) {
      SoulboundManager s = (SoulboundManager)plugin.getMM().getModule(EModule.SOULBOUND);
      return s.isOwner(item, p);
    } 
    return true;
  }
  
  public static String getOwner(ItemStack item) {
    if (EModule.SOULBOUND.isEnabled()) {
      SoulboundManager s = (SoulboundManager)plugin.getMM().getModule(EModule.SOULBOUND);
      return s.getOwner(item);
    } 
    return "";
  }
  
  public static ItemStack setOwner(ItemStack item, Player p) {
    if (EModule.SOULBOUND.isEnabled()) {
      SoulboundManager s = (SoulboundManager)plugin.getMM().getModule(EModule.SOULBOUND);
      return s.setOwner(item, p);
    } 
    return item;
  }
  
  public static boolean isUntradeable(ItemStack item) {
    if (EModule.SOULBOUND.isEnabled()) {
      SoulboundManager s = (SoulboundManager)plugin.getMM().getModule(EModule.SOULBOUND);
      return s.isUntradeable(item);
    } 
    return false;
  }
  
  public static boolean isSoulboundRequired(ItemStack item) {
    if (EModule.SOULBOUND.isEnabled()) {
      SoulboundManager s = (SoulboundManager)plugin.getMM().getModule(EModule.SOULBOUND);
      if (s == null)
        return false; 
      return s.isSoulboundRequired(item);
    } 
    return false;
  }
  
  public static boolean isSoulBinded(ItemStack item) {
    if (EModule.SOULBOUND.isEnabled()) {
      SoulboundManager s = (SoulboundManager)plugin.getMM().getModule(EModule.SOULBOUND);
      return s.isSoulBinded(item);
    } 
    return false;
  }
  
  public static boolean isLevelRequired(ItemStack item) {
    return (LoreUT.getUserLevelIndex(item) >= 0);
  }
  
  public static boolean isClassRequired(ItemStack item) {
    return (LoreUT.getUserClassIndex(item) >= 0);
  }
  
  public static boolean isAllowedPlayerClass(ItemStack item, Player p) {
    if (!isClassRequired(item))
      return true; 
    String[] cls = NBTUtils.getItemClasses(item);
    String player_cls = Config.getClassPlugin().getClass(p);
    byte b;
    int i;
    String[] arrayOfString1;
    for (i = (arrayOfString1 = cls).length, b = 0; b < i; ) {
      String stored_cls = arrayOfString1[b];
      if (player_cls.equalsIgnoreCase(stored_cls))
        return true; 
      b++;
    } 
    return false;
  }
  
  public static int getFirstEmptyHotbat(Player p) {
    for (int i = 0; i < 9; i++) {
      ItemStack item = p.getInventory().getItem(i);
      if (item == null || item.getType() == Material.AIR)
        return i; 
    } 
    return -1;
  }
  
  public static String getItemFirstColor(ItemStack item) {
    String c = "&f";
    if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
      c = "&f";
    } else {
      ItemMeta meta = item.getItemMeta();
      String name = meta.getDisplayName();
      if (name.startsWith("§"))
        c = name.substring(0, 2); 
    } 
    return ChatColor.translateAlternateColorCodes('&', c).trim();
  }
  
  public static boolean canUse(ItemStack item, Player p) {
    if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore())
      return true; 
    if (getDurabilityMinOrMax(item, 0) == 0 && !Config.breakItems()) {
      MsgUT.sendDelayed(p, String.valueOf(Lang.Prefix.toMsg()) + Lang.Other_BrokenItem.toMsg(), 1);
      return false;
    } 
    if (!isAllowedPlayerClass(item, p) && !p.hasPermission("qrpg.bypass.class")) {
      MsgUT.sendDelayed(p, String.valueOf(Lang.Prefix.toMsg()) + Lang.Restrictions_Class.toMsg()
          .replace("%s", Config.getClassPlugin().getClass(p)), 1);
      return false;
    } 
    if (getLevelRequired(item) > Config.getLevelPlugin().getLevel(p) && !p.hasPermission("qrpg.bypass.level")) {
      MsgUT.sendDelayed(p, String.valueOf(Lang.Prefix.toMsg()) + Lang.Restrictions_Level.toMsg().replace("%s", String.valueOf(Config.getLevelPlugin().getLevel(p))), 1);
      return false;
    } 
    if (EModule.SOULBOUND.isEnabled()) {
      SoulboundManager s = (SoulboundManager)plugin.getMM().getModule(EModule.SOULBOUND);
      if (s.isSoulboundRequired(item) && !p.hasPermission("qrpg.bypass.soulbound")) {
        MsgUT.sendDelayed(p, String.valueOf(Lang.Prefix.toMsg()) + Lang.Restrictions_Usage.toMsg(), 1);
        return false;
      } 
      if ((s.isSoulBinded(item) || s.hasOwner(item)) && 
        !s.isOwner(item, p) && !p.hasPermission("qrpg.bypass.owner")) {
        MsgUT.sendDelayed(p, String.valueOf(Lang.Prefix.toMsg()) + Lang.Restrictions_NotOwner.toMsg(), 1);
        return false;
      } 
    } 
    if (EModule.IDENTIFY.isEnabled()) {
      IdentifyManager ide = (IdentifyManager)plugin.getMM().getModule(EModule.IDENTIFY);
      if (ide.isUnidentified(item)) {
        p.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Identify_NoEquip.toMsg());
        return false;
      } 
    } 
    return true;
  }
  
  public static int getLevelRequired(ItemStack item) {
    if (!isLevelRequired(item))
      return -1; 
    return NBTUtils.getItemLevel(item);
  }
  
  public static int getDurabilityMinOrMax(ItemStack item, int i) {
    int durability = -1;
    if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore())
      return durability; 
    List<String> lore = item.getItemMeta().getLore();
    String f1 = ItemStat.DURABILITY.getFormat();
    String unbreakable = String.valueOf(f1) + Config.str_durUnb;
    if (lore.contains(unbreakable))
      return -999; 
    for (String s : item.getItemMeta().getLore()) {
      if (s.startsWith(f1)) {
        String get = ChatColor.stripColor(s.replace(f1, ""));
        String g = get.split(ChatColor.stripColor(Config.str_durSep))[i];
        try {
          durability = Integer.parseInt(g);
        } catch (NumberFormatException ex) {
          LogUtil.send("Unable to get min or max durabiliy of item in: " + g, LogType.WARN);
        } 
        break;
      } 
    } 
    return durability;
  }
  
  public static boolean hasCustomDurability(ItemStack item) {
    return (getDurabilityMinOrMax(item, 0) != -1);
  }
  
  public static boolean isDamaged(ItemStack item) {
    return (hasCustomDurability(item) && getDurabilityMinOrMax(item, 0) < getDurabilityMinOrMax(item, 1));
  }
  
  public static boolean isUnbreakable(ItemStack item) {
    return (getDurabilityMinOrMax(item, 0) == -999);
  }
  
  private static double getDamageFromString(String from, int i) {
    double damag = 0.0D;
    String[] dd = from.split(Config.str_dmgSep);
    if (dd.length <= 1 && i > 0)
      return damag; 
    try {
      damag = Double.parseDouble(ChatColor.stripColor(dd[i].replace(Config.str_procent, "")));
    } catch (NumberFormatException numberFormatException) {}
    return damag;
  }
  
  public static double getAllDamageMinOrMax(ItemStack item, int i) {
    double value = getDefaultDamage(item);
    if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore())
      return value; 
    for (DamageType dt : Config.getDamageTypes().values()) {
      List<String> lore = item.getItemMeta().getLore();
      for (String s : lore) {
        if (s.startsWith(dt.getFormat())) {
          String s2 = s.replace(dt.getFormat(), "");
          value += getDamageFromString(s2, i);
          break;
        } 
      } 
    } 
    return value;
  }
  
  public static double getAllDamage(ItemStack item) {
    double value = getDefaultDamage(item);
    if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore())
      return value; 
    double d1 = getAllDamageMinOrMax(item, 0);
    double d2 = getAllDamageMinOrMax(item, 1);
    return Utils.getRandDouble(d1, d2);
  }
  
  public static double getDamageByTypeMinOrMax(String type, ItemStack item, int i) {
    if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore())
      return 0.0D; 
    double value = 0.0D;
    DamageType dt = (DamageType)Config.getDamageTypes().get(type.toLowerCase());
    if (dt != null) {
      List<String> lore = item.getItemMeta().getLore();
      for (String s : lore) {
        if (s.startsWith(dt.getFormat())) {
          s = s.split("§r§r§8§8§r§r §r§r§8§8§r§r")[0];
          String s2 = s.replace(dt.getFormat(), "").replace(Config.str_procent, "");
          return getDamageFromString(s2, i);
        } 
      } 
    } 
    if (value == 0.0D && dt.isDefault())
      value = getDefaultDamage(item); 
    return value;
  }
  
  public static double getDamageByType(String type, ItemStack item) {
    DamageType dt = (DamageType)Config.getDamageTypes().get(type.toLowerCase());
    double value = 0.0D;
    if (dt.isDefault())
      value = getDefaultDamage(item); 
    if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore())
      return value; 
    if (hasDamageType(type, item)) {
      double refine = 0.0D;
      if (EModule.REFINE.isEnabled()) {
        RefineManager m = (RefineManager)plugin.getModule(RefineManager.class);
        refine = m.getRefinedDamage(item, type);
      } 
      double val1 = getDamageByTypeMinOrMax(type, item, 0) + refine;
      double val2 = getDamageByTypeMinOrMax(type, item, 1) + refine;
      if (val2 == 0.0D)
        val2 = val1; 
      value = Utils.getRandDouble(val1, val2);
    } 
    return value;
  }
  
  public static boolean hasDamageType(String type, ItemStack item) {
    DamageType dt = (DamageType)Config.getDamageTypes().get(type.toLowerCase());
    if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore())
      return false; 
    if (dt != null) {
      List<String> lore = item.getItemMeta().getLore();
      for (String s : lore) {
        if (s.startsWith(dt.getFormat()))
          return true; 
      } 
    } 
    return false;
  }
  
  public static int getDamageLoreIndex(String type, ItemStack item) {
    if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore())
      return -1; 
    DamageType dt = Config.getDamageTypeById(type);
    if (dt != null) {
      int i = 0;
      List<String> lore = item.getItemMeta().getLore();
      for (String s : lore) {
        if (s.startsWith(dt.getFormat()))
          return i; 
        i++;
      } 
    } 
    return -1;
  }
  
  public static boolean hasDefenseType(String type, ItemStack item) {
    if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore())
      return false; 
    ArmorType dt = Config.getArmorTypeById(type);
    if (dt != null) {
      List<String> lore = item.getItemMeta().getLore();
      for (String s : lore) {
        if (s.startsWith(dt.getFormat()))
          return true; 
      } 
    } 
    return false;
  }
  
  public static int getDefenseLoreIndex(String type, ItemStack item) {
    if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore())
      return -1; 
    ArmorType dt = Config.getArmorTypeById(type);
    if (dt != null) {
      int i = 0;
      List<String> lore = item.getItemMeta().getLore();
      for (String s : lore) {
        if (s.startsWith(dt.getFormat()))
          return i; 
        i++;
      } 
    } 
    return -1;
  }
  
  public static double getDefenseByType(String type, ItemStack item) {
    if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore())
      return 0.0D; 
    double value = 0.0D;
    ArmorType dt = Config.getArmorTypeById(type);
    if (dt != null) {
      List<String> lore = item.getItemMeta().getLore();
      for (String s : lore) {
        if (s.startsWith(dt.getFormat())) {
          s = s.split("§r§r§8§8§r§r §r§r§8§8§r§r")[0];
          String s2 = ChatColor.stripColor(s.replace(dt.getFormat(), "").replace(Config.str_procent, ""));
          value = Double.parseDouble(s2);
          if (EModule.REFINE.isEnabled()) {
            RefineManager m = (RefineManager)plugin.getModule(RefineManager.class);
            value += m.getRefinedDefense(item, type);
          } 
        } 
      } 
    } 
    return value;
  }
  
  public static boolean hasAttribute(ItemStack item, ItemStat type) {
    if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore())
      return false; 
    for (String s : item.getItemMeta().getLore()) {
      if (s.startsWith(type.getFormat()))
        return true; 
    } 
    return false;
  }
  
  public static int getStatLoreIndex(ItemStack item, ItemStat type) {
    int value = -1;
    if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore())
      return value; 
    List<String> lore = item.getItemMeta().getLore();
    int i = 0;
    for (String s : lore) {
      if (s.startsWith(type.getFormat()))
        return i; 
      i++;
    } 
    return value;
  }
  
  public static double getStatOnItem(ItemStack item, ItemStat type) {
    double value = getStatBase(item, type);
    if (type.getCapability() >= 0.0D && value > type.getCapability())
      value = type.getCapability(); 
    return value;
  }
  
  private static double getStatBase(ItemStack item, ItemStat type) {
    if (type == ItemStat.DURABILITY)
      return getDurabilityMinOrMax(item, 0); 
    double value = 0.0D;
    if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore())
      return value; 
    if (EModule.REFINE.isEnabled()) {
      RefineManager m = (RefineManager)plugin.getModule(RefineManager.class);
      value += m.getRefinedStat(item, type);
    } 
    List<String> lore = item.getItemMeta().getLore();
    for (String s : lore) {
      if (s.startsWith(type.getFormat())) {
        s = s.split("§r§r§8§8§r§r §r§r§8§8§r§r")[0];
        String g = ChatColor.stripColor(s
            .replace(type.getFormat(), "").replace(Config.str_procent, "")
            .replace(Config.str_Modifier, ""));
        double value2 = 0.0D;
        try {
          value2 = Double.parseDouble(g);
        } catch (NumberFormatException e) {
          LogUtil.send("Unable to get base stat value from string: " + g, LogType.WARN);
        } 
        value += value2;
        break;
      } 
    } 
    return value;
  }
  
  public static double getDefaultDamage(ItemStack item) {
    if (item == null || item.getType() == Material.AIR || ItemUtils.isArmor(item))
      return 0.0D; 
    double damage = 1.0D;
    if (item.getType() == Material.DIAMOND_SWORD || item.getType() == Material.GOLD_AXE || item.getType() == Material.WOOD_AXE) {
      damage = 7.0D;
    } else if (item.getType() == Material.DIAMOND_AXE || item.getType() == Material.IRON_AXE || item.getType() == Material.STONE_AXE || item.getType().name().equals("TRIDENT")) {
      damage = 9.0D;
    } else if (item.getType() == Material.DIAMOND_PICKAXE || item.getType() == Material.DIAMOND_SPADE || item.getType() == Material.STONE_SWORD) {
      damage = 5.0D;
    } else if (item.getType() == Material.IRON_SWORD) {
      damage = 6.0D;
    } else if (item.getType() == Material.IRON_PICKAXE || item.getType() == Material.IRON_SPADE || item.getType() == Material.GOLD_SWORD || item.getType() == Material.WOOD_SWORD) {
      damage = 4.0D;
    } else if (item.getType() == Material.STONE_PICKAXE || item.getType() == Material.STONE_SPADE) {
      damage = 3.0D;
    } else if (item.getType() == Material.GOLD_PICKAXE || item.getType() == Material.GOLD_SPADE || item.getType() == Material.WOOD_PICKAXE || item.getType() == Material.WOOD_SPADE) {
      damage = 2.0D;
    } else if (item.getType() == Material.BOW) {
      damage = 10.0D;
    } 
    return damage;
  }
  
  public static double getDefaultDefense(ItemStack item) {
    if (item == null)
      return 0.0D; 
    if (item.getType() == Material.GOLD_BOOTS || item.getType() == Material.LEATHER_HELMET || 
      item.getType() == Material.CHAINMAIL_BOOTS)
      return 1.0D; 
    if (item.getType() == Material.LEATHER_LEGGINGS || item.getType() == Material.CHAINMAIL_HELMET || item.getType() == Material.IRON_HELMET || 
      item.getType() == Material.IRON_BOOTS || item.getType() == Material.GOLD_HELMET)
      return 2.0D; 
    if (item.getType() == Material.LEATHER_CHESTPLATE || item.getType() == Material.DIAMOND_HELMET || item.getType() == Material.DIAMOND_BOOTS || 
      item.getType() == Material.GOLD_LEGGINGS)
      return 3.0D; 
    if (item.getType() == Material.CHAINMAIL_LEGGINGS)
      return 4.0D; 
    if (item.getType() == Material.CHAINMAIL_CHESTPLATE || item.getType() == Material.IRON_LEGGINGS || item.getType() == Material.GOLD_CHESTPLATE)
      return 5.0D; 
    if (item.getType() == Material.IRON_CHESTPLATE || item.getType() == Material.DIAMOND_LEGGINGS)
      return 6.0D; 
    if (item.getType() == Material.DIAMOND_CHESTPLATE)
      return 8.0D; 
    return 0.0D;
  }
  
  public static double getDefaultToughness(ItemStack item) {
    if (item == null || !ItemUtils.isArmor(item))
      return 0.0D; 
    if (item.getType().name().startsWith("DIAMOND_"))
      return 2.0D; 
    return 0.0D;
  }
  
  public static double getDefaultDefense(LivingEntity e1) {
    double def = 0.0D;
    byte b;
    int i;
    ItemStack[] arrayOfItemStack;
    for (i = (arrayOfItemStack = EntityAPI.getEquipment(e1, true)).length, b = 0; b < i; ) {
      ItemStack item = arrayOfItemStack[b];
      if (item != null && item.getType() != Material.AIR)
        if (item.getType() == Material.LEATHER_LEGGINGS || item.getType() == Material.CHAINMAIL_HELMET || item.getType() == Material.IRON_HELMET || 
          item.getType() == Material.IRON_BOOTS || item.getType() == Material.GOLD_HELMET) {
          def += 2.0D;
        } else if (item.getType() == Material.LEATHER_CHESTPLATE || item.getType() == Material.DIAMOND_HELMET || item.getType() == Material.DIAMOND_BOOTS || 
          item.getType() == Material.GOLD_LEGGINGS) {
          def += 3.0D;
        } else if (item.getType() == Material.CHAINMAIL_LEGGINGS) {
          def += 4.0D;
        } else if (item.getType() == Material.CHAINMAIL_CHESTPLATE || item.getType() == Material.IRON_LEGGINGS || item.getType() == Material.GOLD_CHESTPLATE) {
          def += 5.0D;
        } else if (item.getType() == Material.IRON_CHESTPLATE || item.getType() == Material.DIAMOND_LEGGINGS) {
          def += 6.0D;
        } else if (item.getType() == Material.DIAMOND_CHESTPLATE) {
          def += 8.0D;
        }  
      b++;
    } 
    return def;
  }
  
  public static double getDefaultAttackSpeed(ItemStack item) {
    if (item == null)
      return -0.5D; 
    double damage = -0.5D;
    switch (item.getType()) {
      case IRON_SPADE:
      case WOOD_SPADE:
      case STONE_SPADE:
      case DIAMOND_SPADE:
      case DIAMOND_AXE:
      case GOLD_SPADE:
      case GOLD_AXE:
      case WOOD_HOE:
      case GOLD_HOE:
        damage = 3.0D;
        return damage;
      case IRON_SWORD:
      case WOOD_SWORD:
      case STONE_SWORD:
      case DIAMOND_SWORD:
      case GOLD_SWORD:
        damage = 2.4D;
        return damage;
      case IRON_PICKAXE:
      case WOOD_PICKAXE:
      case STONE_PICKAXE:
      case DIAMOND_PICKAXE:
      case GOLD_PICKAXE:
        damage = 2.8D;
        return damage;
      case IRON_AXE:
      case WOOD_AXE:
      case STONE_AXE:
        damage = 3.2D;
        return damage;
      case STONE_HOE:
        damage = 2.0D;
        return damage;
      case IRON_HOE:
        damage = 1.0D;
        return damage;
      case DIAMOND_HOE:
        damage = 0.0D;
        return damage;
    } 
    damage = -0.5D;
    return damage;
  }
  
  public static ItemStack setAmmoType(ItemStack item, AmmoType type, int line) {
    if (!type.isEnabled())
      return item; 
    if (item.getType() != Material.BOW)
      return item; 
    String f1 = type.getFormat();
    int pos = 0;
    ItemMeta meta = item.getItemMeta();
    List<String> lore = meta.getLore();
    if (lore == null)
      lore = new ArrayList<>(); 
    for (String s : lore) {
      if (s.startsWith(f1)) {
        pos = lore.indexOf(s);
        lore.remove(pos);
        break;
      } 
    } 
    pos = line;
    if (pos < 0 || pos >= lore.size()) {
      lore.add(f1);
    } else {
      lore.add(pos, f1);
    } 
    meta.setLore(lore);
    item.setItemMeta(meta);
    return item;
  }
  
  public static AmmoType getAmmoType(ItemStack item) {
    AmmoType at1 = AmmoType.ARROW;
    if (item == null || !item.hasItemMeta())
      return at1; 
    ItemMeta meta = item.getItemMeta();
    if (!meta.hasLore())
      return at1; 
    List<String> lore = meta.getLore();
    byte b;
    int i;
    AmmoType[] arrayOfAmmoType;
    for (i = (arrayOfAmmoType = AmmoType.values()).length, b = 0; b < i; ) {
      AmmoType at = arrayOfAmmoType[b];
      String f1 = at.getFormat();
      if (lore.contains(f1))
        return at; 
      b++;
    } 
    return at1;
  }
  
  public static ItemStack setHandType(ItemStack item, WpnHand type, int line) {
    if (!ItemUtils.isWeapon(item))
      return item; 
    if (!type.isEnabled())
      return item; 
    String f1 = type.getFormat();
    int pos = 0;
    ItemMeta meta = item.getItemMeta();
    List<String> lore = meta.getLore();
    if (lore == null)
      lore = new ArrayList<>(); 
    for (String s : lore) {
      if (s.startsWith(f1)) {
        pos = lore.indexOf(s);
        lore.remove(pos);
        break;
      } 
    } 
    pos = line;
    if (pos < 0 || pos >= lore.size()) {
      lore.add(f1);
    } else {
      lore.add(pos, f1);
    } 
    meta.setLore(lore);
    item.setItemMeta(meta);
    return item;
  }
  
  public static WpnHand getHandType(ItemStack item) {
    WpnHand hand = WpnHand.ONE;
    if (item == null || !item.hasItemMeta())
      return hand; 
    ItemMeta meta = item.getItemMeta();
    if (!meta.hasLore())
      return hand; 
    List<String> lore = meta.getLore();
    byte b;
    int i;
    WpnHand[] arrayOfWpnHand;
    for (i = (arrayOfWpnHand = WpnHand.values()).length, b = 0; b < i; ) {
      WpnHand at = arrayOfWpnHand[b];
      String f1 = at.getFormat();
      if (lore.contains(f1))
        return at; 
      b++;
    } 
    return hand;
  }
  
  @Deprecated
  public static ItemStack setDurability(ItemStack item, int current, int maximal) {
    if (current > maximal)
      current = maximal; 
    ItemStat a1 = ItemStat.DURABILITY;
    String f1 = a1.getFormat();
    ItemMeta meta = item.getItemMeta();
    List<String> lore = meta.getLore();
    for (String s : lore) {
      if (s.startsWith(f1)) {
        int index = lore.indexOf(s);
        lore.remove(index);
        lore.add(index, String.valueOf(f1) + current + Config.str_durSep + a1.getValue() + maximal);
        meta.setLore(lore);
        item.setItemMeta(meta);
        break;
      } 
    } 
    return item;
  }
  
  public static void reduceDurability(LivingEntity li, ItemStack item, int amount) {
    if (!(li instanceof Player) && !Config.mobsCanReduceDurability())
      return; 
    if (!hasCustomDurability(item) || isUnbreakable(item))
      return; 
    if (item.hasItemMeta() && item.getItemMeta().hasEnchant(Enchantment.DURABILITY)) {
      double lvl = item.getItemMeta().getEnchantLevel(Enchantment.DURABILITY);
      double num = Utils.randInt(0, 100);
      double chance = 100.0D / (lvl + 1.0D);
      if (num <= chance)
        return; 
    } 
    int current = getDurabilityMinOrMax(item, 0);
    if (current == -999 || current == 0)
      return; 
    int max = getDurabilityMinOrMax(item, 1);
    int lose = current - Math.min(amount, current);
    QuantumItemDamageEvent eve = new QuantumItemDamageEvent(item, li);
    plugin.getPluginManager().callEvent((Event)eve);
    if (eve.isCancelled())
      return; 
    if (lose <= 0) {
      if (Config.breakItems()) {
        item.setAmount(0);
        li.getWorld().playSound(li.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.8F, 0.8F);
        return;
      } 
      if (current == 0)
        return; 
    } 
    setDurability(item, lose, max);
  }
  
  public static EModule getItemModule(ItemStack item) {
    if (item == null || item.getType() == Material.AIR)
      return null; 
    NBTItem nbt = new NBTItem(item);
    if (nbt.hasKey("E_MODULE").booleanValue())
      return EModule.valueOf(nbt.getString("E_MODULE")); 
    return null;
  }
  
  public static ModuleItem getAbstractItemByModule(EModule e, String id) {
    if (!e.isEnabled())
      return null; 
    QModule q = plugin.getMM().getModule(e);
    if (q == null || !q.isDropable())
      return null; 
    QModuleDrop d = (QModuleDrop)q;
    return d.getItemById(id);
  }
  
  public static ItemStack getItemByModule(EModule e, String id, int lvl, int suc) {
    ModuleItem mi = getAbstractItemByModule(e, id);
    if (mi == null)
      return null; 
    if (mi instanceof SocketItem) {
      SocketItem si = (SocketItem)mi;
      return si.create(lvl, suc);
    } 
    if (mi instanceof LeveledItem) {
      LeveledItem si = (LeveledItem)mi;
      return si.create(lvl);
    } 
    return mi.create();
  }
  
  public static void setUntradeable(ItemStack item, boolean b, int line) {
    if (EModule.SOULBOUND.isEnabled()) {
      SoulboundManager s = (SoulboundManager)plugin.getMM().getModule(EModule.SOULBOUND);
      s.setUntradeable(item, b, line);
    } 
  }
  
  public static void updateLevelRequirement(ItemStack item, Player p) {
    String lvl_str;
    int pos = LoreUT.getUserLevelIndex(item);
    if (pos < 0)
      return; 
    ItemMeta meta = item.getItemMeta();
    List<String> lore = meta.getLore();
    int user_lvl = Config.getLevelPlugin().getLevel(p);
    int item_lvl = NBTUtils.getItemLevel(item);
    if (user_lvl >= item_lvl) {
      lvl_str = Config.str_Req_Lvl_User_Single.replace("%lvl%", String.valueOf(item_lvl)).replace("%state%", Lang.Lore_State_true.toMsg());
    } else {
      lvl_str = Config.str_Req_Lvl_User_Single.replace("%lvl%", String.valueOf(item_lvl)).replace("%state%", Lang.Lore_State_false.toMsg());
    } 
    lore.set(pos, lvl_str);
    meta.setLore(lore);
    item.setItemMeta(meta);
  }
  
  public static void updateLevelRequirement(ItemStack item) {
    int pos = LoreUT.getUserLevelIndex(item);
    if (pos < 0)
      return; 
    ItemMeta meta = item.getItemMeta();
    List<String> lore = meta.getLore();
    int item_lvl = NBTUtils.getItemLevel(item);
    String lvl_str = Config.str_Req_Lvl_User_Single.replace("%lvl%", String.valueOf(item_lvl)).replace("%state%", Lang.Lore_State_false.toMsg());
    lore.set(pos, lvl_str);
    meta.setLore(lore);
    item.setItemMeta(meta);
  }
  
  public static ItemStack setLevelRequirement(ItemStack item, int level, int line) {
    int pos = LoreUT.getUserLevelIndex(item);
    ItemMeta meta = item.getItemMeta();
    List<String> lore = meta.getLore();
    if (lore == null)
      lore = new ArrayList<>(); 
    String lvl_no = Config.str_Req_Lvl_User_Single.replace("%lvl%", String.valueOf(level)).replace("%state%", Lang.Lore_State_false.toMsg());
    if (pos >= 0)
      lore.remove(pos); 
    if (level > 0) {
      if (line >= 0)
        pos = line; 
      if (pos < 0 || pos >= lore.size()) {
        lore.add(lvl_no);
      } else {
        lore.add(pos, lvl_no);
      } 
      meta.setLore(lore);
      item.setItemMeta(meta);
    } 
    item = NBTUtils.setItemLevel(item, level);
    return item;
  }
  
  public static void updateClassRequirement(ItemStack item, Player p) {
    String lvl_str;
    int pos = LoreUT.getUserClassIndex(item);
    if (pos < 0)
      return; 
    ItemMeta meta = item.getItemMeta();
    List<String> lore = meta.getLore();
    String[] item_c = NBTUtils.getItemClasses(item);
    String val = LoreUT.getStrSeparated(item_c);
    if (isAllowedPlayerClass(item, p)) {
      lvl_str = Config.str_Req_Cls_User_Single.replace("%class%", String.valueOf(val)).replace("%state%", Lang.Lore_State_true.toMsg());
    } else {
      lvl_str = Config.str_Req_Cls_User_Single.replace("%class%", String.valueOf(val)).replace("%state%", Lang.Lore_State_false.toMsg());
    } 
    lore.set(pos, lvl_str);
    meta.setLore(lore);
    item.setItemMeta(meta);
  }
  
  public static void updateClassRequirement(ItemStack item) {
    int pos = LoreUT.getUserClassIndex(item);
    if (pos < 0)
      return; 
    ItemMeta meta = item.getItemMeta();
    List<String> lore = meta.getLore();
    String[] item_c = NBTUtils.getItemClasses(item);
    String val = LoreUT.getStrSeparated(item_c);
    String lvl_str = Config.str_Req_Cls_User_Single.replace("%class%", String.valueOf(val)).replace("%state%", Lang.Lore_State_false.toMsg());
    lore.set(pos, lvl_str);
    meta.setLore(lore);
    item.setItemMeta(meta);
  }
  
  public static ItemStack setClassRequirement(ItemStack item, String[] classes, int line) {
    if (item == null || item.getType() == Material.AIR)
      return item; 
    int pos = LoreUT.getUserClassIndex(item);
    ItemMeta meta = item.getItemMeta();
    List<String> lore = meta.getLore();
    if (lore == null)
      lore = new ArrayList<>(); 
    String val = LoreUT.getStrSeparated(classes);
    String c_no = Config.str_Req_Cls_User_Single.replace("%class%", String.valueOf(val)).replace("%state%", Lang.Lore_State_false.toMsg());
    if (pos >= 0)
      lore.remove(pos); 
    if (classes.length > 0) {
      if (line >= 0)
        pos = line; 
      if (pos < 0 || pos >= lore.size()) {
        lore.add(c_no);
      } else {
        lore.add(pos, c_no);
      } 
      meta.setLore(lore);
      item.setItemMeta(meta);
    } 
    item = NBTUtils.setItemClass(item, classes);
    return item;
  }
  
  public static void setSoulboundRequirement(ItemStack item, boolean b, int line) {
    if (EModule.SOULBOUND.isEnabled()) {
      SoulboundManager s = (SoulboundManager)plugin.getMM().getModule(EModule.SOULBOUND);
      s.setSoulboundRequirement(item, b, line);
    } 
  }
  
  public static ItemStack addItemStat(ItemStack item, ItemStat type, double val, int line) {
    ItemMeta meta = item.getItemMeta();
    List<String> lore = meta.getLore();
    if (lore == null)
      lore = new ArrayList<>(); 
    String f1 = type.getFormat();
    boolean bonus = !type.isMainItem(item);
    int pos = -1;
    for (String s : lore) {
      if (s.startsWith(f1)) {
        pos = lore.indexOf(s);
        lore.remove(s);
        break;
      } 
    } 
    if (line > 0)
      pos = line; 
    item = delItemStat(item, type);
    if (type == ItemStat.ATTACK_SPEED && !ItemUtils.isWeapon(item))
      return item; 
    if (!bonus)
      val = type.fineValue(val); 
    if (val == 0.0D)
      return item; 
    meta = item.getItemMeta();
    lore = meta.getLore();
    if (lore == null)
      lore = new ArrayList<>(); 
    switch (type) {
      case DURABILITY:
        if (!bonus) {
          String add;
          int i = (int)val;
          if (i > 0) {
            add = String.valueOf(f1) + i + Config.str_durSep + type.getValue() + i;
          } else {
            add = String.valueOf(f1) + Config.str_durUnb;
          } 
          if (pos < 0 || pos >= lore.size()) {
            lore.add(add);
          } else {
            lore.add(pos, add);
          } 
        } 
        meta.setLore(lore);
        item.setItemMeta(meta);
        item = plugin.getNMS().fixNBT(item);
        return item;
    } 
    double d1 = val;
    if (type != ItemStat.RANGE || (item.getType() != Material.BOW && d1 > 0.0D)) {
      String add;
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
      if (pos < 0 || pos >= lore.size()) {
        lore.add(String.valueOf(f1) + add);
      } else {
        lore.add(pos, String.valueOf(f1) + add);
      } 
    } 
    meta.setLore(lore);
    item.setItemMeta(meta);
    item = plugin.getNMS().fixNBT(item);
    return item;
  }
  
  public static ItemStack delItemStat(ItemStack item, ItemStat type) {
    if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore())
      return item; 
    String f1 = type.getFormat();
    ItemMeta meta = item.getItemMeta();
    List<String> lore = meta.getLore();
    for (String s : lore) {
      if (s.startsWith(f1)) {
        lore.remove(s);
        break;
      } 
    } 
    meta.setLore(lore);
    item.setItemMeta(meta);
    item = plugin.getNMS().fixNBT(item);
    return item;
  }
  
  public static ItemStack addNBTTag(ItemStack item, String tag, String value) {
    NBTItem nbt = new NBTItem(item);
    if (StringUtils.isNumeric(value)) {
      if (Double.valueOf(value) != null) {
        nbt.setDouble(tag, Double.valueOf(Double.parseDouble(value)));
      } else if (Integer.valueOf(value) != null) {
        nbt.setInteger(tag, Integer.valueOf(Integer.parseInt(value)));
      } 
    } else if (value.equals("true") || value.equals("false")) {
      boolean b = Boolean.valueOf(value).booleanValue();
      nbt.setBoolean(tag, Boolean.valueOf(b));
    } else {
      nbt.setString(tag, value);
    } 
    return nbt.getItem();
  }
  
  public static ItemStack delNBTTag(ItemStack item, String tag) {
    NBTItem nbt = new NBTItem(item);
    nbt.removeKey(tag);
    return nbt.getItem();
  }
  
  public static void addDamageType(ItemStack item, DamageType type, double min, double max, int line) {
    String f1 = type.getFormat();
    int pos = 0;
    ItemMeta meta = item.getItemMeta();
    List<String> lore = meta.getLore();
    if (lore == null)
      lore = new ArrayList<>(); 
    for (String s : lore) {
      if (s.startsWith(f1)) {
        pos = lore.indexOf(s);
        lore.remove(pos);
        break;
      } 
    } 
    pos = line;
    double r_min = Math.min(min, max);
    double r_max = Math.max(min, max);
    if (r_max > 0.0D) {
      String val = String.valueOf(f1) + r_min + Config.str_dmgSep + type.getValue() + r_max;
      if (pos < 0 || pos >= lore.size()) {
        lore.add(val);
      } else {
        lore.add(pos, val);
      } 
    } 
    meta.setLore(lore);
    item.setItemMeta(meta);
  }
  
  public static void addDefenseType(ItemStack item, ArmorType type, double amount, int line) {
    amount = Utils.round3(amount);
    String f1 = type.getFormat();
    int pos = 0;
    ItemMeta meta = item.getItemMeta();
    List<String> lore = meta.getLore();
    if (lore == null)
      lore = new ArrayList<>(); 
    for (String s : lore) {
      if (s.startsWith(f1)) {
        pos = lore.indexOf(s);
        lore.remove(pos);
        break;
      } 
    } 
    pos = line;
    if (amount > 0.0D) {
      String val;
      if (type.isPercent()) {
        val = String.valueOf(f1) + Config.str_positive + amount + Config.str_procent;
      } else {
        val = String.valueOf(f1) + amount;
      } 
      if (pos < 0 || pos >= lore.size()) {
        lore.add(val);
      } else {
        lore.add(pos, val);
      } 
    } 
    meta.setLore(lore);
    item.setItemMeta(meta);
  }
  
  public static void addDivineSlot(ItemStack item, QSlotType type, int pos) {
    if (type.getModule() == null || !type.getModule().isActive())
      return; 
    ItemMeta meta = item.getItemMeta();
    List<String> lore = meta.getLore();
    if (lore == null)
      lore = new ArrayList<>(); 
    String non = type.getEmpty();
    if (pos < 0 || pos >= lore.size()) {
      lore.add(non);
    } else {
      lore.add(pos, non);
    } 
    meta.setLore(lore);
    item.setItemMeta(meta);
  }
  
  public static void addFlag(ItemStack item, ItemFlag f) {
    ItemMeta meta = item.getItemMeta();
    meta.addItemFlags(new ItemFlag[] { f });
    item.setItemMeta(meta);
  }
  
  public static void delFlag(ItemStack item, ItemFlag f) {
    ItemMeta meta = item.getItemMeta();
    meta.removeItemFlags(new ItemFlag[] { f });
    item.setItemMeta(meta);
  }
  
  public static void setName(ItemStack item, String name) {
    ItemMeta meta = item.getItemMeta();
    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name.trim()));
    item.setItemMeta(meta);
  }
  
  public static void addLoreLine(ItemStack item, String s, int pos) {
    ItemMeta meta = item.getItemMeta();
    List<String> lore = meta.getLore();
    if (lore == null)
      lore = new ArrayList<>(); 
    s = ChatColor.translateAlternateColorCodes('&', s);
    if (pos > 0 && pos < lore.size()) {
      lore.add(pos, s);
    } else {
      lore.add(s);
    } 
    meta.setLore(lore);
    item.setItemMeta(meta);
  }
  
  public static void delLoreLine(ItemStack item, int pos) {
    ItemMeta meta = item.getItemMeta();
    List<String> lore = meta.getLore();
    if (lore == null)
      return; 
    if (pos >= lore.size() || pos < 0)
      pos = lore.size() - 1; 
    lore.remove(pos);
    meta.setLore(lore);
    item.setItemMeta(meta);
  }
  
  public static void delLoreLine(ItemStack item, String s) {
    ItemMeta meta = item.getItemMeta();
    List<String> lore = meta.getLore();
    if (lore == null)
      return; 
    s = ChatColor.translateAlternateColorCodes('&', s);
    if (!lore.contains(s))
      return; 
    int pos = lore.indexOf(s);
    lore.remove(pos);
    meta.setLore(lore);
    item.setItemMeta(meta);
  }
  
  public static void clearLore(ItemStack item) {
    ItemMeta meta = item.getItemMeta();
    List<String> lore = meta.getLore();
    if (lore == null)
      return; 
    meta.setLore(new ArrayList());
    item.setItemMeta(meta);
  }
  
  public static void enchant(ItemStack item, Enchantment e, int lvl) {
    ItemMeta meta = item.getItemMeta();
    if (lvl <= 0) {
      meta.removeEnchant(e);
    } else {
      meta.addEnchant(e, lvl, true);
    } 
    item.setItemMeta(meta);
  }
  
  public static void addPotionEffect(ItemStack item, PotionEffectType type, int lvl, int dur, boolean ambient, boolean particles, boolean icon) {
    PotionMeta meta = (PotionMeta)item.getItemMeta();
    lvl--;
    if (lvl < 0) {
      meta.removeCustomEffect(type);
    } else {
      meta.addCustomEffect(new PotionEffect(type, dur * 20, lvl, ambient, particles), true);
    } 
    item.setItemMeta((ItemMeta)meta);
  }
  
  public static void setLeatherColor(ItemStack item, Color c) {
    LeatherArmorMeta meta = (LeatherArmorMeta)item.getItemMeta();
    meta.setColor(c);
    item.setItemMeta((ItemMeta)meta);
  }
}
