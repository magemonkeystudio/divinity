package su.nightexpress.quantumrpg.modules.party.cmds;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import su.nightexpress.quantumrpg.cmds.ICmd;
import su.nightexpress.quantumrpg.modules.QModule;
import su.nightexpress.quantumrpg.modules.party.PartyManager;

class PartyDropCmd extends ICmd {
  private PartyManager m;
  
  public PartyDropCmd(QModule m) {
    this.m = (PartyManager)m;
  }
  
  public String getLabel() {
    return "drop";
  }
  
  public String getUsage() {
    return "";
  }
  
  public String getDesc() {
    return "Toggle party drop mode.";
  }
  
  public String getPermission() {
    return "qrpg.party.cmd.drop";
  }
  
  public boolean playersOnly() {
    return true;
  }
  
  public void perform(CommandSender sender, String label, String[] args) {
    Player p = (Player)sender;
    this.m.togglePartyDrop(p);
  }
}
