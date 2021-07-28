package su.nightexpress.quantumrpg.modules.party.cmds;

import java.util.Collections;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import su.nightexpress.quantumrpg.cmds.ICmd;
import su.nightexpress.quantumrpg.modules.QModule;
import su.nightexpress.quantumrpg.modules.party.PartyManager;

class PartyKickCmd extends ICmd {
  private PartyManager m;
  
  public PartyKickCmd(QModule m) {
    this.m = (PartyManager)m;
  }
  
  public String getLabel() {
    return "kick";
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
    return "Kick player from the party.";
  }
  
  public String getPermission() {
    return "qrpg.party.cmd.kick";
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
    Player who = this.m.pl().getServer().getPlayer(args[1]);
    this.m.kickFromParty(p, who);
  }
}
