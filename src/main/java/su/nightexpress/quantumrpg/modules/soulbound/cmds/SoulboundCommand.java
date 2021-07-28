package su.nightexpress.quantumrpg.modules.soulbound.cmds;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.modules.cmds.MExecutor;
import su.nightexpress.quantumrpg.utils.Utils;

public class SoulboundCommand extends MExecutor {
  public SoulboundCommand(QuantumRPG plugin) {
    super(plugin);
  }
  
  public void setup() {
    register(new SoulboundUntradeCmd(this.plugin, this.m));
    register(new SoulboundSetCmd(this.plugin, this.m));
  }
  
  public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
    if (!(sender instanceof org.bukkit.entity.Player))
      return null; 
    if (args.length == 1) {
      List<String> sugg = Arrays.asList(new String[] { "help", "set", "untradable", "reload", "info" });
      return Utils.getSugg(args[0], sugg);
    } 
    if (args.length == 2) {
      String sub = args[0];
      if (sub.equalsIgnoreCase("set") || sub.equalsIgnoreCase("untradable")) {
        List<String> sugg = Arrays.asList(new String[] { "true", "false" });
        return Utils.getSugg(args[1], sugg);
      } 
    } 
    return Collections.emptyList();
  }
}
