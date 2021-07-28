package su.nightexpress.quantumrpg.modules.refine.cmds;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.quantumrpg.cmds.ICmd;
import su.nightexpress.quantumrpg.config.Lang;
import su.nightexpress.quantumrpg.modules.QModule;
import su.nightexpress.quantumrpg.modules.refine.RefineManager;

class RefineCmd extends ICmd {
  private RefineManager m;
  
  public RefineCmd(QModule m) {
    this.m = (RefineManager)m;
  }
  
  public String getLabel() {
    return "refine";
  }
  
  public String getUsage() {
    return "";
  }
  
  public String getDesc() {
    return "Refines item in hand.";
  }
  
  public String getPermission() {
    return "qrpg.refine.cmd.refine";
  }
  
  public boolean playersOnly() {
    return true;
  }
  
  public void perform(CommandSender sender, String label, String[] args) {
    Player p = (Player)sender;
    ItemStack item = p.getInventory().getItemInMainHand();
    if (item == null) {
      p.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Other_InvalidItem.toMsg());
      return;
    } 
    item = this.m.refineItem(item);
    p.getInventory().setItemInMainHand(item);
    p.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Refine_Enchanting_Success.toMsg());
  }
}
