package su.nightexpress.quantumrpg.cmds.list;

import org.bukkit.command.CommandSender;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.cmds.ICmd;

public class InfoCommand extends ICmd {
  private QuantumRPG plugin;
  
  public InfoCommand(QuantumRPG plugin) {
    this.plugin = plugin;
  }
  
  public String getLabel() {
    return "info";
  }
  
  public String getUsage() {
    return "";
  }
  
  public String getDesc() {
    return "About the plugin.";
  }
  
  public void perform(CommandSender sender, String label, String[] args) {
    sender.sendMessage("§8§m--------§8§l[ §fQuantum RPG - Info §8§l]§8§m--------");
    sender.sendMessage("§r");
    sender.sendMessage("§8§l[§aAbout§8§l]");
    sender.sendMessage("§a> §7Price: §a10.00 USD§7.");
    sender.sendMessage("§a> §7Created by: §aNightExpress");
    sender.sendMessage("§a> §7Version: §a" + this.plugin.getDescription().getVersion() + " (Final)");
    sender.sendMessage("§a> §7Licensed to: §a%%__USER__%%");
    sender.sendMessage("§a> §7Type §a/qprg help §7for help.");
    sender.sendMessage("§r");
    sender.sendMessage("§8§l[§eTerms of Service§8§l]");
    sender.sendMessage("§e> §7Redistributing: §c§lDisallowed§7.");
    sender.sendMessage("§e> §7Refunds: §c§lDisallowed§7.");
    sender.sendMessage("§e> §7Decompile/Modify code: §a§lAllowed§7.");
    sender.sendMessage("§r");
    sender.sendMessage("§8§l[§dSupport/Bug Report/Suggestions§8§l]");
    sender.sendMessage("§d> §7SpigotMC: §dPM §7or §dForum thread§7. §a[Fast]");
    sender.sendMessage("§r");
    sender.sendMessage("§8§m------------------------------------");
  }
  
  public String getPermission() {
    return "qrpg.admin";
  }
  
  public boolean playersOnly() {
    return false;
  }
}
