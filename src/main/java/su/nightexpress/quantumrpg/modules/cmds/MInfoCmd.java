package su.nightexpress.quantumrpg.modules.cmds;

import org.bukkit.command.CommandSender;
import su.nightexpress.quantumrpg.cmds.ICmd;
import su.nightexpress.quantumrpg.modules.QModule;
import su.nightexpress.quantumrpg.utils.Utils;

class MInfoCmd extends ICmd {
  private QModule m;
  
  public MInfoCmd(QModule m) {
    this.m = m;
  }
  
  public String getLabel() {
    return "info";
  }
  
  public String getUsage() {
    return "";
  }
  
  public String getDesc() {
    return "About the module.";
  }
  
  public String getPermission() {
    return "qrpg.module.info";
  }
  
  public boolean playersOnly() {
    return false;
  }
  
  public void perform(CommandSender sender, String label, String[] args) {
    sender.sendMessage("§8§m------------§8§l[ §fModule: §a" + this.m.name() + " §8§l]§8§m------------");
    sender.sendMessage("§a> §7Status: " + Utils.getModuleStatus(this.m));
    sender.sendMessage("§a> §7Version: §a" + this.m.version());
    sender.sendMessage("§a> §7Dropable: §a" + this.m.isDropable());
    sender.sendMessage("§a> §7Resolvable: §a" + this.m.isResolvable());
    sender.sendMessage("§8§m------------§8§l[ §fEnd Module Info §8§l]§8§m------------");
  }
}
