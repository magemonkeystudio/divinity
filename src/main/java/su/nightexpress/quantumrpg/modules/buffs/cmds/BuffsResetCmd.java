package su.nightexpress.quantumrpg.modules.buffs.cmds;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.cmds.ICmd;
import su.nightexpress.quantumrpg.config.Lang;
import su.nightexpress.quantumrpg.modules.QModule;
import su.nightexpress.quantumrpg.modules.buffs.BuffManager;
import su.nightexpress.quantumrpg.utils.Utils;

class BuffsResetCmd extends ICmd {
  private BuffManager m;
  
  public BuffsResetCmd(QuantumRPG plugin, QModule m) {
    this.m = (BuffManager)m;
  }
  
  public String getLabel() {
    return "reset";
  }
  
  public String getUsage() {
    return "<player> <bufftype> <name>";
  }
  
  public List<String> getTab(int i) {
    if (i == 2)
      return null; 
    if (i == 3)
      return Utils.getEnumsList(BuffManager.BuffType.class); 
    if (i == 4)
      return Arrays.asList(new String[] { "<name>" }); 
    return Collections.emptyList();
  }
  
  public String getDesc() {
    return "Reset the buff from a player.";
  }
  
  public String getPermission() {
    return "qrpg.buffs.cmd.reset";
  }
  
  public boolean playersOnly() {
    return false;
  }
  
  public void perform(CommandSender sender, String label, String[] args) {
    if (args.length != 4) {
      printUsage(sender, label);
      return;
    } 
    Player p = Bukkit.getPlayer(args[1]);
    if (p == null) {
      sender.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Other_InvalidPlayer.toMsg());
      return;
    } 
    BuffManager.BuffType type = null;
    try {
      type = BuffManager.BuffType.valueOf(args[2].toUpperCase());
    } catch (IllegalArgumentException ex) {
      sender.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Other_InvalidType.toMsg().replace("%types%", Utils.getEnums(BuffManager.BuffType.class, "§a", "§7")));
      return;
    } 
    String value = args[3];
    this.m.resetBuff((Entity)p, type, value);
    sender.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + "Done!");
  }
}
