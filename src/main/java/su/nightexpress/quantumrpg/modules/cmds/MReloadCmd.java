package su.nightexpress.quantumrpg.modules.cmds;

import org.bukkit.command.CommandSender;
import su.nightexpress.quantumrpg.cmds.ICmd;
import su.nightexpress.quantumrpg.config.Lang;
import su.nightexpress.quantumrpg.modules.QModule;

class MReloadCmd extends ICmd {
  private QModule m;
  
  public MReloadCmd(QModule m) {
    this.m = m;
  }
  
  public String getLabel() {
    return "reload";
  }
  
  public String getUsage() {
    return "";
  }
  
  public String getDesc() {
    return "Reload the module.";
  }
  
  public String getPermission() {
    return "qrpg.module.reload";
  }
  
  public boolean playersOnly() {
    return false;
  }
  
  public void perform(CommandSender sender, String label, String[] args) {
    this.m.reload();
    sender.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Module_Cmd_Reload.toMsg()
        .replace("%module%", this.m.name()));
  }
}
