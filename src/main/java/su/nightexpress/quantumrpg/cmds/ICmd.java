package su.nightexpress.quantumrpg.cmds;

import java.util.Collections;
import java.util.List;
import org.bukkit.command.CommandSender;
import su.nightexpress.quantumrpg.config.Lang;

public abstract class ICmd {
  public final void execute(CommandSender sender, String label, String[] args) {
    if (playersOnly() && !(sender instanceof org.bukkit.entity.Player))
      return; 
    if (getPermission() != null && !sender.hasPermission(getPermission())) {
      sender.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Other_NoPerm.toMsg());
      return;
    } 
    perform(sender, label, args);
  }
  
  public void printUsage(CommandSender sender, String label) {
    sender.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Module_Cmd_Usage.toMsg()
        .replace("%cmd%", label)
        .replace("%usage%", getUsage())
        .replace("%label%", getLabel()));
  }
  
  public abstract void perform(CommandSender paramCommandSender, String paramString, String[] paramArrayOfString);
  
  public abstract String getLabel();
  
  public abstract String getUsage();
  
  public List<String> getTab(int i) {
    return Collections.emptyList();
  }
  
  public abstract String getDesc();
  
  public abstract String getPermission();
  
  public abstract boolean playersOnly();
}
