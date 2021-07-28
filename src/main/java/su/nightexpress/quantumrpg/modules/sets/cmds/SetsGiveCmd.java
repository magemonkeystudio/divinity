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

class SetsGiveCmd extends ICmd {
  private QuantumRPG plugin;
  
  private SetManager m;
  
  public SetsGiveCmd(QuantumRPG plugin, QModule m) {
    this.plugin = plugin;
    this.m = (SetManager)m;
  }
  
  public String getLabel() {
    return "give";
  }
  
  public String getUsage() {
    return "<player> <id> [amount]";
  }
  
  public String getDesc() {
    return "Give an set item to a player.";
  }
  
  public String getPermission() {
    return "qrpg.module.give";
  }
  
  public boolean playersOnly() {
    return false;
  }
  
  public void perform(CommandSender sender, String label, String[] args) {
    if (args.length < 4 || args.length > 5) {
      printUsage(sender, label);
      return;
    } 
    String id = args[2];
    SetManager.ItemSet g = this.m.getSetById(id);
    if (g == null) {
      sender.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Sets_Invalid.toMsg().replace("%s", id));
      return;
    } 
    Player p = this.plugin.getServer().getPlayer(args[1]);
    if (p == null) {
      sender.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Other_InvalidPlayer.toMsg());
      return;
    } 
    String type = args[3];
    int amount = 1;
    if (args.length == 5)
      try {
        amount = Integer.parseInt(args[4]);
      } catch (NumberFormatException ex) {
        sender.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Other_InvalidNumber.toMsg().replace("%s", args[4]));
      }  
    for (int i = 0; i < amount; i++) {
      if (id.equalsIgnoreCase("random"))
        g = this.m.getSetById(id); 
      if (g != null) {
        ItemStack item = g.create(type);
        Utils.addItem(p, item);
      } 
    } 
    sender.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Module_Cmd_Give_Done.toMsg()
        .replace("%player%", p.getName())
        .replace("%item%", g.getName())
        .replace("%amount%", String.valueOf(amount)));
  }
}
