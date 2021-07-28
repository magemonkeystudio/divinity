package su.nightexpress.quantumrpg.modules.sets.cmds;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.cmds.ICmd;
import su.nightexpress.quantumrpg.config.Lang;
import su.nightexpress.quantumrpg.modules.QModule;
import su.nightexpress.quantumrpg.modules.sets.SetManager;

class SetsDropCmd extends ICmd {
  private QuantumRPG plugin;
  
  private SetManager m;
  
  public SetsDropCmd(QuantumRPG plugin, QModule m) {
    this.plugin = plugin;
    this.m = (SetManager)m;
  }
  
  public String getLabel() {
    return "drop";
  }
  
  public String getUsage() {
    return "<world> <x> <y> <z> <id> [amount]";
  }
  
  public String getDesc() {
    return "Drops an specified set item in the world.";
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
    SetManager.ItemSet g = this.m.getSetById(id);
    if (g == null) {
      sender.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Sets_Invalid.toMsg().replace("%s", id));
      return;
    } 
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
    String type = args[6];
    int amount = 1;
    if (args.length == 8)
      try {
        amount = Integer.parseInt(args[7]);
      } catch (NumberFormatException ex) {
        sender.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Other_InvalidNumber.toMsg().replace("%s", args[7]));
      }  
    Location loc = new Location(w, x, y, z);
    for (int i = 0; i < amount; i++) {
      if (id.equalsIgnoreCase("random"))
        g = this.m.getSetById(id); 
      if (g != null) {
        ItemStack item = g.create(type);
        w.dropItemNaturally(loc, item);
      } 
    } 
    sender.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Module_Cmd_Drop_Done.toMsg()
        .replace("%w%", w.getName())
        .replace("%x%", String.valueOf(x))
        .replace("%y%", String.valueOf(y))
        .replace("%z%", String.valueOf(z))
        .replace("%item%", g.getName())
        .replace("%amount%", String.valueOf(amount)));
  }
}
