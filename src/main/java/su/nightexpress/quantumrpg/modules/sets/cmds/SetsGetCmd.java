package su.nightexpress.quantumrpg.modules.sets.cmds;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.cmds.ICmd;
import su.nightexpress.quantumrpg.config.Lang;
import su.nightexpress.quantumrpg.modules.QModule;
import su.nightexpress.quantumrpg.modules.sets.SetManager;
import su.nightexpress.quantumrpg.utils.Utils;

class SetsGetCmd extends ICmd {
  private SetManager m;
  
  public SetsGetCmd(QuantumRPG plugin, QModule m) {
    this.m = (SetManager)m;
  }
  
  public String getLabel() {
    return "get";
  }
  
  public String getUsage() {
    return "<id> [amount]";
  }
  
  public String getDesc() {
    return "Get an specified set item.";
  }
  
  public String getPermission() {
    return "qrpg.module.get";
  }
  
  public boolean playersOnly() {
    return true;
  }
  
  public void perform(CommandSender sender, String label, String[] args) {
    if (args.length < 3 || args.length > 4) {
      printUsage(sender, label);
      return;
    } 
    String id = args[1];
    SetManager.ItemSet g = this.m.getSetById(id);
    if (g == null) {
      sender.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Sets_Invalid.toMsg().replace("%s", id));
      return;
    } 
    String type = args[2];
    int amount = 1;
    if (args.length == 4)
      try {
        amount = Integer.parseInt(args[3]);
      } catch (NumberFormatException ex) {
        sender.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Other_InvalidNumber.toMsg().replace("%s", args[3]));
      }  
    Player p = (Player)sender;
    for (int i = 0; i < amount; i++) {
      if (id.equalsIgnoreCase("random"))
        g = this.m.getSetById(id); 
      if (g != null) {
        ItemStack item = g.create(type);
        Utils.addItem(p, item);
      } 
    } 
    sender.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Module_Cmd_Get_Done.toMsg()
        .replace("%item%", g.getName())
        .replace("%amount%", String.valueOf(amount)));
  }
}
