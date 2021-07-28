package su.nightexpress.quantumrpg.modules.identify.cmds;

import java.util.Collections;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.modules.cmds.MExecutor;

public class IdentifyCommand extends MExecutor {
  public IdentifyCommand(QuantumRPG plugin) {
    super(plugin);
  }
  
  public void setup() {}
  
  public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
    if (!(sender instanceof org.bukkit.entity.Player))
      return null; 
    return Collections.emptyList();
  }
}
