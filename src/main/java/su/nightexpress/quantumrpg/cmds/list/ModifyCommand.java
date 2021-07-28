package su.nightexpress.quantumrpg.cmds.list;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffectType;
import su.nightexpress.quantumrpg.api.ItemAPI;
import su.nightexpress.quantumrpg.cmds.ICmd;
import su.nightexpress.quantumrpg.config.Lang;
import su.nightexpress.quantumrpg.utils.Utils;

public class ModifyCommand extends ICmd {
  public String getLabel() {
    return "modify";
  }
  
  public String getUsage() {
    return "";
  }
  
  public String getDesc() {
    return "Modify an item.";
  }
  
  public String getPermission() {
    return "qrpg.admin";
  }
  
  public boolean playersOnly() {
    return true;
  }
  
  public void perform(CommandSender sender, String label, String[] args) {
    Player p = (Player)sender;
    ItemStack item = p.getInventory().getItemInMainHand();
    if (item == null || item.getType() == Material.AIR) {
      p.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Other_InvalidItem.toMsg());
      return;
    } 
    if (args.length >= 3 && args[1].equalsIgnoreCase("name")) {
      String name = "";
      for (int i = 2; i < args.length; i++)
        name = String.valueOf(name) + args[i] + " "; 
      ItemAPI.setName(item, name.trim());
      p.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Admin_Set.toMsg());
    } else if (args.length >= 3 && args[1].equalsIgnoreCase("lore")) {
      if (args[2].equalsIgnoreCase("add") && args.length >= 4) {
        String line = "";
        int l = args.length;
        int x = l;
        int pos = -1;
        if (StringUtils.isNumeric(args[l - 1])) {
          x--;
          pos = Integer.parseInt(args[l - 1]);
        } 
        for (int i = 3; i < x; i++)
          line = String.valueOf(line) + args[i] + " "; 
        ItemAPI.addLoreLine(item, line.trim(), pos);
        p.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Admin_Set.toMsg());
      } else if (args[2].equalsIgnoreCase("del") && args.length >= 4) {
        if (StringUtils.isNumeric(args[3])) {
          int pos = Integer.parseInt(args[3]);
          ItemAPI.delLoreLine(item, pos);
          p.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Admin_Set.toMsg());
        } 
      } else if (args[2].equalsIgnoreCase("clear")) {
        ItemAPI.clearLore(item);
        p.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Admin_Set.toMsg());
      } else {
        printHelp(p);
      } 
    } else if (args.length == 4 && args[1].equalsIgnoreCase("flag")) {
      String flag = args[3].toUpperCase();
      ItemFlag f = null;
      try {
        f = ItemFlag.valueOf(flag);
      } catch (IllegalArgumentException ex) {
        p.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Other_InvalidType.toMsg().replace("%s", Utils.getEnums(ItemFlag.class, "&c", "&7")));
        return;
      } 
      if (args[2].equalsIgnoreCase("add")) {
        ItemAPI.addFlag(item, f);
        p.getInventory().setItemInMainHand(item);
        p.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Admin_Set.toMsg());
      } else if (args[2].equalsIgnoreCase("del")) {
        ItemAPI.delFlag(item, f);
        p.getInventory().setItemInMainHand(item);
        p.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Admin_Set.toMsg());
      } else {
        printHelp(p);
      } 
    } else if (args.length >= 4 && args[1].equalsIgnoreCase("nbt")) {
      String tag = args[3];
      if (args[2].equalsIgnoreCase("add") && args.length == 5) {
        String value = args[4];
        item = ItemAPI.addNBTTag(item, tag, value);
        p.getInventory().setItemInMainHand(item);
        p.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Admin_Set.toMsg());
      } else if (args[2].equalsIgnoreCase("del")) {
        item = ItemAPI.delNBTTag(item, tag);
        p.getInventory().setItemInMainHand(item);
        p.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Admin_Set.toMsg());
      } else {
        printHelp(p);
      } 
    } else if (args.length == 4 && args[1].equalsIgnoreCase("enchant")) {
      String ench = args[2].toUpperCase();
      Enchantment e = Enchantment.getByName(ench);
      if (e == null) {
        p.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Other_InvalidType.toMsg().replace("%s", Utils.getEnums(Enchantment.class, "&c", "&7")));
        return;
      } 
      int lvl = 1;
      try {
        lvl = Integer.parseInt(args[3]);
      } catch (NumberFormatException ex) {
        p.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Other_InvalidNumber.toMsg().replace("%s", args[3]));
        return;
      } 
      ItemAPI.enchant(item, e, lvl);
      p.getInventory().setItemInMainHand(item);
      p.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Admin_Set.toMsg());
    } else if (args.length >= 5 && args[1].equalsIgnoreCase("potion")) {
      PotionMeta pm = (PotionMeta)item.getItemMeta();
      if (pm == null) {
        p.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Other_NotAPotion.toMsg());
        return;
      } 
      String eff = args[2].toUpperCase();
      PotionEffectType type = PotionEffectType.getByName(eff);
      if (type == null) {
        p.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Other_InvalidType.toMsg().replace("%s", Utils.getEnums(PotionEffectType.class, "&b", "&7")));
        return;
      } 
      int lvl = 1;
      try {
        lvl = Integer.parseInt(args[3]);
      } catch (NumberFormatException ex) {
        p.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Other_InvalidNumber.toMsg().replace("%s", args[3]));
        return;
      } 
      int dur = 20;
      try {
        dur = Integer.parseInt(args[4]);
      } catch (NumberFormatException ex) {
        p.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Other_InvalidNumber.toMsg().replace("%s", args[4]));
        return;
      } 
      boolean ambient = false;
      boolean particles = false;
      if (args.length == 6)
        ambient = Boolean.valueOf(args[5]).booleanValue(); 
      if (args.length == 7)
        particles = Boolean.valueOf(args[6]).booleanValue(); 
      boolean icon = false;
      if (args.length == 8)
        icon = Boolean.valueOf(args[7]).booleanValue(); 
      ItemAPI.addPotionEffect(item, type, lvl, dur, ambient, particles, icon);
      p.getInventory().setItemInMainHand(item);
      p.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Admin_Set.toMsg());
    } else if (args.length == 3 && args[1].equalsIgnoreCase("color")) {
      if (!item.getType().name().startsWith("LEATHER_")) {
        p.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Other_NotALeather.toMsg());
        return;
      } 
      Color c = Color.WHITE;
      String[] s1 = args[2].split(",");
      int r = 0;
      int g = 0;
      int b = 0;
      try {
        r = Integer.parseInt(s1[0]);
        g = Integer.parseInt(s1[1]);
        b = Integer.parseInt(s1[2]);
      } catch (NumberFormatException ex) {
        p.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Other_InvalidRGB.toMsg().replace("%s", args[7]));
        return;
      } 
      c = Color.fromRGB(r, g, b);
      ItemAPI.setLeatherColor(item, c);
      p.getInventory().setItemInMainHand(item);
      p.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Admin_Set.toMsg());
    } else {
      printHelp(p);
    } 
  }
  
  private void printHelp(Player p) {
    for (String s : Lang.Help_Modify.getList())
      p.sendMessage(s); 
  }
}
