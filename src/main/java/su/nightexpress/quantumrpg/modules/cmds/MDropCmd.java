package su.nightexpress.quantumrpg.modules.cmds;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.api.ItemAPI;
import su.nightexpress.quantumrpg.cmds.ICmd;
import su.nightexpress.quantumrpg.config.Lang;
import su.nightexpress.quantumrpg.modules.QModuleDrop;
import su.nightexpress.quantumrpg.utils.Utils;

class MDropCmd extends ICmd {
  private QuantumRPG plugin;
  
  private QModuleDrop m;
  
  public MDropCmd(QuantumRPG plugin, QModuleDrop m) {
    this.plugin = plugin;
    this.m = m;
  }
  
  public String getLabel() {
    return "drop";
  }
  
  public String getUsage() {
    return "<world> <x> <y> <z> <id> <level> [amount]";
  }
  
  public List<String> getTab(int i) {
    if (i == 2)
      return Utils.getWorldNames(); 
    if (i == 3)
      return Arrays.asList(new String[] { "<x>" }); 
    if (i == 4)
      return Arrays.asList(new String[] { "<y>" }); 
    if (i == 5)
      return Arrays.asList(new String[] { "<z>" }); 
    if (i == 6)
      return null; 
    if (i == 7)
      return this.m.getItemIds(); 
    if (i == 8) {
      if (this.m instanceof su.nightexpress.quantumrpg.modules.QModuleLevel)
        return Arrays.asList(new String[] { "[level]", "-1", "1-5", "1" }); 
      return Arrays.asList(new String[] { "1", "10" });
    } 
    if (i == 9 && this.m instanceof su.nightexpress.quantumrpg.modules.QModuleLevel)
      return Arrays.asList(new String[] { "1", "10" }); 
    return Collections.emptyList();
  }
  
  public String getDesc() {
    return "Drops a specified item in the world.";
  }
  
  public String getPermission() {
    return "qrpg.module.drop";
  }
  
  public boolean playersOnly() {
    return false;
  }
  
  public void perform(CommandSender sender, String label, String[] args) {
    if (args.length < 7 || args.length > 8) {
      printUsage(sender, label);
      return;
    } 
    String id = args[5];
    World w = this.plugin.getServer().getWorld(args[1]);
    if (w == null) {
      sender.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Other_InvalidWorld.toMsg().replace("%s", args[1]));
      return;
    } 
    double x = 0.0D, y = 0.0D, z = 0.0D;
    try {
      x = Double.parseDouble(args[2]);
      y = Double.parseDouble(args[3]);
      z = Double.parseDouble(args[4]);
    } catch (NumberFormatException ex) {
      sender.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Other_InvalidNumber.toMsg().replace("%s", String.valueOf(args[2]) + "/" + args[3] + "/" + args[4]));
      return;
    } 
    int level = -1;
    int amount = 1;
    if (this.m instanceof su.nightexpress.quantumrpg.modules.QModuleLevel) {
      if (args.length >= 7)
        if (args[6].contains("-")) {
          try {
            int l1 = Integer.parseInt(args[6].split("-")[0]);
            int l2 = Integer.parseInt(args[6].split("-")[1]);
            level = Utils.randInt(l1, l2);
          } catch (NumberFormatException numberFormatException) {}
        } else {
          try {
            level = Integer.parseInt(args[6]);
          } catch (NumberFormatException numberFormatException) {}
        }  
      if (args.length == 8)
        try {
          amount = Integer.parseInt(args[7]);
        } catch (NumberFormatException ex) {
          sender.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Other_InvalidNumber.toMsg().replace("%s", args[7]));
        }  
    } else if (args.length == 7) {
      try {
        amount = Integer.parseInt(args[6]);
      } catch (NumberFormatException ex) {
        sender.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Other_InvalidNumber.toMsg().replace("%s", args[6]));
      } 
    } 
    ItemStack item = null;
    Location loc = new Location(w, x, y, z);
    for (int i = 0; i < amount; i++) {
      item = ItemAPI.getItemByModule(this.m.type(), id, level, -1);
      if (item != null)
        w.dropItemNaturally(loc, item); 
    } 
    String name = id;
    if (item != null && 
      !id.equalsIgnoreCase("random"))
      name = Utils.getItemName(item); 
    sender.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Module_Cmd_Drop_Done.toMsg()
        .replace("%w%", w.getName())
        .replace("%x%", String.valueOf(x))
        .replace("%y%", String.valueOf(y))
        .replace("%z%", String.valueOf(z))
        .replace("%item%", name)
        .replace("%amount%", String.valueOf(amount)));
  }
}
