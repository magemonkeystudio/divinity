package su.nightexpress.quantumrpg.modules.repair.cmds;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.quantumrpg.cmds.ICmd;
import su.nightexpress.quantumrpg.modules.QModule;
import su.nightexpress.quantumrpg.modules.repair.RepairManager;

public class RepairOpenCmd extends ICmd {
  private RepairManager m;
  
  public RepairOpenCmd(QModule m) {
    this.m = (RepairManager)m;
  }
  
  public String getLabel() {
    return "open";
  }
  
  public String getUsage() {
    return "";
  }
  
  public String getDesc() {
    return "Opens a GUI to repair items.";
  }
  
  public String getPermission() {
    return "qrpg.repair.cmd.open";
  }
  
  public boolean playersOnly() {
    return true;
  }
  
  public void perform(CommandSender sender, String label, String[] args) {
    Player p = (Player)sender;
    ItemStack item = p.getInventory().getItemInMainHand();
    this.m.openRepairGUI(p, item, null, null);
  }
}
