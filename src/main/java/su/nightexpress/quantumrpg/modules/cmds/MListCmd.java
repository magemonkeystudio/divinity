package su.nightexpress.quantumrpg.modules.cmds;

import org.bukkit.command.CommandSender;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.cmds.ICmd;
import su.nightexpress.quantumrpg.modules.QModule;
import su.nightexpress.quantumrpg.modules.QModuleDrop;
import su.nightexpress.quantumrpg.utils.Utils;

class MListCmd extends ICmd {
  private QModuleDrop m;
  
  public MListCmd(QuantumRPG plugin, QModuleDrop m) {
    this.m = m;
  }
  
  public String getLabel() {
    return "list";
  }
  
  public String getUsage() {
    return "[page]";
  }
  
  public String getDesc() {
    return "List of all items.";
  }
  
  public String getPermission() {
    return "qrpg.module.list";
  }
  
  public boolean playersOnly() {
    return false;
  }
  
  public void perform(CommandSender sender, String label, String[] args) {
    if (args.length > 2) {
      printUsage(sender, label);
      return;
    } 
    int page = 1;
    if (args.length == 2)
      try {
        page = Integer.parseInt(args[1]);
      } catch (NumberFormatException numberFormatException) {} 
    Utils.interactiveList(sender, page, this.m.getItemIds(), (QModule)this.m, "-1 1");
  }
}
