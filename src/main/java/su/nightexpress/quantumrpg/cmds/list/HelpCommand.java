package su.nightexpress.quantumrpg.cmds.list;

import org.bukkit.command.CommandSender;
import su.nightexpress.quantumrpg.cmds.ICmd;
import su.nightexpress.quantumrpg.config.Lang;

public class HelpCommand extends ICmd {
  public void perform(CommandSender sender, String label, String[] args) {
    if (args.length <= 1)
      for (String s : Lang.Help_Main.getList())
        sender.sendMessage(s);  
  }
  
  public String getLabel() {
    return "help";
  }
  
  public String getUsage() {
    return "";
  }
  
  public String getDesc() {
    return "List of all commands.";
  }
  
  public String getPermission() {
    return "qrpg.admin";
  }
  
  public boolean playersOnly() {
    return false;
  }
}
