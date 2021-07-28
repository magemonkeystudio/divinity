package su.nightexpress.quantumrpg.cmds.list;

import org.bukkit.command.CommandSender;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.cmds.ICmd;
import su.nightexpress.quantumrpg.config.Lang;

public class ReloadCommand extends ICmd {
  private QuantumRPG plugin;
  
  public ReloadCommand(QuantumRPG plugin) {
    this.plugin = plugin;
  }
  
  public String getLabel() {
    return "reload";
  }
  
  public String getUsage() {
    return "";
  }
  
  public String getDesc() {
    return "Reload the plugin.";
  }
  
  public void perform(CommandSender sender, String label, String[] args) {
    this.plugin.reload();
    sender.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Admin_Reload.toMsg());
  }
  
  public String getPermission() {
    return "qrpg.admin";
  }
  
  public boolean playersOnly() {
    return false;
  }
}
