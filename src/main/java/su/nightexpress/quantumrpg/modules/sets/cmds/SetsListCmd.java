package su.nightexpress.quantumrpg.modules.sets.cmds;

import org.bukkit.command.CommandSender;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.cmds.ICmd;
import su.nightexpress.quantumrpg.modules.QModule;
import su.nightexpress.quantumrpg.modules.sets.SetManager;
import su.nightexpress.quantumrpg.utils.Utils;

class SetsListCmd extends ICmd {
  private SetManager m;
  
  public SetsListCmd(QuantumRPG plugin, QModule m) {
    this.m = (SetManager)m;
  }
  
  public String getLabel() {
    return "list";
  }
  
  public String getUsage() {
    return "[page]";
  }
  
  public String getDesc() {
    return "List of all sets.";
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
    Utils.interactiveList(sender, page, this.m.getSetNames(), (QModule)this.m, "random 1");
  }
}
