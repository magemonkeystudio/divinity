package su.nightexpress.quantumrpg.modules.party.cmds;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import su.nightexpress.quantumrpg.cmds.ICmd;
import su.nightexpress.quantumrpg.modules.QModule;
import su.nightexpress.quantumrpg.modules.party.PartyManager;

class PartyCreateCmd extends ICmd {
  private PartyManager m;
  
  public PartyCreateCmd(QModule m) {
    this.m = (PartyManager)m;
  }
  
  public String getLabel() {
    return "create";
  }
  
  public String getUsage() {
    return "[name]";
  }
  
  public List<String> getTab(int i) {
    if (i == 2)
      return Arrays.asList(new String[] { "[name]" }); 
    return Collections.emptyList();
  }
  
  public String getDesc() {
    return "Create a new party.";
  }
  
  public String getPermission() {
    return "qrpg.party.cmd.create";
  }
  
  public boolean playersOnly() {
    return true;
  }
  
  public void perform(CommandSender sender, String label, String[] args) {
    Player p = (Player)sender;
    String id = p.getName();
    if (args.length == 2)
      id = args[1]; 
    this.m.createParty(p, id);
  }
}
