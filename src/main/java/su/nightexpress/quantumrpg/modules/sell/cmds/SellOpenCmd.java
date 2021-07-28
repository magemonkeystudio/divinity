package su.nightexpress.quantumrpg.modules.sell.cmds;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import su.nightexpress.quantumrpg.cmds.ICmd;
import su.nightexpress.quantumrpg.modules.QModule;
import su.nightexpress.quantumrpg.modules.sell.SellManager;

public class SellOpenCmd extends ICmd {
  private SellManager m;
  
  public SellOpenCmd(QModule m) {
    this.m = (SellManager)m;
  }
  
  public String getLabel() {
    return "open";
  }
  
  public String getUsage() {
    return "";
  }
  
  public String getDesc() {
    return "Opens a GUI to sell items.";
  }
  
  public String getPermission() {
    return "qrpg.sell.cmd.open";
  }
  
  public boolean playersOnly() {
    return true;
  }
  
  public void perform(CommandSender sender, String label, String[] args) {
    Player p = (Player)sender;
    this.m.openSellGUI(p);
  }
}
