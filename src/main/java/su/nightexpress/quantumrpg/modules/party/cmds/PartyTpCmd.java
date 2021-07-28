package su.nightexpress.quantumrpg.modules.party.cmds;

import java.util.Collections;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import su.nightexpress.quantumrpg.cmds.ICmd;
import su.nightexpress.quantumrpg.modules.QModule;
import su.nightexpress.quantumrpg.modules.party.PartyManager;

class PartyTpCmd extends ICmd {
  private PartyManager m;
  
  public PartyTpCmd(QModule m) {
    this.m = (PartyManager)m;
  }
  
  public String getLabel() {
    return "tp";
  }
  
  public String getUsage() {
    return "<player>";
  }
  
  public List<String> getTab(int i) {
    if (i == 2)
      return null; 
    return Collections.emptyList();
  }
  
  public String getDesc() {
    return "Teleport to party member.";
  }
  
  public String getPermission() {
    return "qrpg.party.cmd.tp";
  }
  
  public boolean playersOnly() {
    return true;
  }
  
  public void perform(CommandSender sender, String label, String[] args) {
    Player p = (Player)sender;
    if (args.length != 2) {
      printUsage(sender, label);
      return;
    } 
    Player p2 = this.m.pl().getServer().getPlayer(args[1]);
    this.m.teleport(p, p2);
  }
}
