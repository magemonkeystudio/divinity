package su.nightexpress.quantumrpg.modules.extractor.cmds;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.quantumrpg.cmds.ICmd;
import su.nightexpress.quantumrpg.modules.QModule;
import su.nightexpress.quantumrpg.modules.extractor.ExtractorManager;

public class ExtractorOpenCmd extends ICmd {
  private ExtractorManager m;
  
  public ExtractorOpenCmd(QModule m) {
    this.m = (ExtractorManager)m;
  }
  
  public String getLabel() {
    return "open";
  }
  
  public String getUsage() {
    return "";
  }
  
  public String getDesc() {
    return "Opens a GUI to extract.";
  }
  
  public String getPermission() {
    return "qrpg.extractor.cmd.open";
  }
  
  public boolean playersOnly() {
    return true;
  }
  
  public void perform(CommandSender sender, String label, String[] args) {
    Player p = (Player)sender;
    ItemStack item = p.getInventory().getItemInMainHand();
    this.m.openExtractGUI(p, item, null, null, -1);
  }
}
