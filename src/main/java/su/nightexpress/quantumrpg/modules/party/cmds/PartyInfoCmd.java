package su.nightexpress.quantumrpg.modules.party.cmds;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import su.nightexpress.quantumrpg.cmds.ICmd;
import su.nightexpress.quantumrpg.modules.QModule;
import su.nightexpress.quantumrpg.modules.party.PartyManager;

class PartyInfoCmd extends ICmd {
  private PartyManager m;
  
  public PartyInfoCmd(QModule m) {
    this.m = (PartyManager)m;
  }
  
  public String getLabel() {
    return "info";
  }
  
  public String getUsage() {
    return "";
  }
  
  public String getDesc() {
    return "Info about the party.";
  }
  
  public String getPermission() {
    return "qrpg.party.cmd.info";
  }
  
  public boolean playersOnly() {
    return true;
  }
  
  public void perform(CommandSender sender, String label, String[] args) {
    Player p = (Player)sender;
    this.m.openPartyGUI(p);
  }
}
