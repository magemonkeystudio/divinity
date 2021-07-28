package su.nightexpress.quantumrpg.modules.party.cmds;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import su.nightexpress.quantumrpg.cmds.ICmd;
import su.nightexpress.quantumrpg.modules.QModule;
import su.nightexpress.quantumrpg.modules.party.PartyManager;

class PartyDisbandCmd extends ICmd {
  private PartyManager m;
  
  public PartyDisbandCmd(QModule m) {
    this.m = (PartyManager)m;
  }
  
  public String getLabel() {
    return "disband";
  }
  
  public String getUsage() {
    return "";
  }
  
  public String getDesc() {
    return "Disband the party.";
  }
  
  public String getPermission() {
    return "qrpg.party.cmd.disband";
  }
  
  public boolean playersOnly() {
    return true;
  }
  
  public void perform(CommandSender sender, String label, String[] args) {
    Player p = (Player)sender;
    this.m.disbandParty(p);
  }
}
