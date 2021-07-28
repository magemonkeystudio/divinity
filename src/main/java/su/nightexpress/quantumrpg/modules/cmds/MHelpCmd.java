package su.nightexpress.quantumrpg.modules.cmds;

import org.bukkit.command.CommandSender;
import su.nightexpress.quantumrpg.cmds.ICmd;
import su.nightexpress.quantumrpg.modules.QModule;

class MHelpCmd extends ICmd {
  private MExecutor exe;
  
  public MHelpCmd(QModule m, MExecutor exe) {
    this.exe = exe;
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
    return "qrpg.module.reload";
  }
  
  public boolean playersOnly() {
    return false;
  }
  
  public void perform(CommandSender sender, String label, String[] args) {
    this.exe.printHelp(sender);
  }
}
