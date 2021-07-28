package su.nightexpress.quantumrpg.modules.resolve.cmds;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.quantumrpg.cmds.ICmd;
import su.nightexpress.quantumrpg.modules.QModule;
import su.nightexpress.quantumrpg.modules.resolve.ResolveManager;

class ResolveOpenCmd extends ICmd {
  private ResolveManager m;
  
  public ResolveOpenCmd(QModule m) {
    this.m = (ResolveManager)m;
  }
  
  public String getLabel() {
    return "open";
  }
  
  public String getUsage() {
    return "";
  }
  
  public String getDesc() {
    return "Opens a GUI to resolve items.";
  }
  
  public String getPermission() {
    return "qrpg.resolve.cmd.open";
  }
  
  public boolean playersOnly() {
    return true;
  }
  
  public void perform(CommandSender sender, String label, String[] args) {
    Player p = (Player)sender;
    ItemStack item = p.getInventory().getItemInMainHand();
    this.m.openResolveGUI(p, item, null);
  }
}
