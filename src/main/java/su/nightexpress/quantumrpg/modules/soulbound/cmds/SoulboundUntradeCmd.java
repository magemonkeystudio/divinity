package su.nightexpress.quantumrpg.modules.soulbound.cmds;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.api.ItemAPI;
import su.nightexpress.quantumrpg.cmds.ICmd;
import su.nightexpress.quantumrpg.config.Lang;
import su.nightexpress.quantumrpg.modules.QModule;

class SoulboundUntradeCmd extends ICmd {
  public SoulboundUntradeCmd(QuantumRPG plugin, QModule m) {}
  
  public String getLabel() {
    return "untradable";
  }
  
  public String getUsage() {
    return "<true/false> [pos]";
  }
  
  public String getDesc() {
    return "(un)Make item untradable.";
  }
  
  public List<String> getTab(int i) {
    if (i == 2)
      return Arrays.asList(new String[] { "true", "false" }); 
    if (i == 3)
      return Arrays.asList(new String[] { "[pos]" }); 
    return Collections.emptyList();
  }
  
  public String getPermission() {
    return "qrpg.soulbound.cmd.untradeable";
  }
  
  public boolean playersOnly() {
    return true;
  }
  
  public void perform(CommandSender sender, String label, String[] args) {
    if (args.length < 2 || args.length > 3) {
      printUsage(sender, label);
      return;
    } 
    int pos = -1;
    if (args.length == 3)
      try {
        pos = Integer.parseInt(args[2]);
      } catch (NumberFormatException ex) {
        sender.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Other_InvalidNumber.toMsg().replace("%s", args[2]));
      }  
    Player p = (Player)sender;
    ItemStack item = p.getInventory().getItemInMainHand();
    if (item == null) {
      sender.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Other_InvalidItem.toMsg());
      return;
    } 
    boolean b = Boolean.valueOf(args[1]).booleanValue();
    ItemAPI.setUntradeable(item, b, pos);
    p.getInventory().setItemInMainHand(item);
    sender.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Soulbound_Cmd_Untradable_Done.toMsg()
        .replace("%state%", Lang.getBool(b)));
  }
}
