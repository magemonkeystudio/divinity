package su.nightexpress.quantumrpg.modules.cmds;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.quantumrpg.api.ItemAPI;
import su.nightexpress.quantumrpg.cmds.ICmd;
import su.nightexpress.quantumrpg.config.Lang;
import su.nightexpress.quantumrpg.modules.QModuleDrop;
import su.nightexpress.quantumrpg.utils.Utils;

class MGetCmd extends ICmd {
  private QModuleDrop m;
  
  public MGetCmd(QModuleDrop m) {
    this.m = m;
  }
  
  public String getLabel() {
    return "get";
  }
  
  public String getUsage() {
    if (this.m instanceof su.nightexpress.quantumrpg.modules.QModuleLevel)
      return "<id> [level] [amount]"; 
    return "<id> [amount]";
  }
  
  public List<String> getTab(int i) {
    if (i == 2)
      return this.m.getItemIds(); 
    if (i == 3) {
      if (this.m instanceof su.nightexpress.quantumrpg.modules.QModuleLevel)
        return Arrays.asList(new String[] { "[level]", "-1", "1-5", "1" }); 
      return Arrays.asList(new String[] { "1", "10" });
    } 
    if (i == 4 && this.m instanceof su.nightexpress.quantumrpg.modules.QModuleLevel)
      return Arrays.asList(new String[] { "1", "10" }); 
    return Collections.emptyList();
  }
  
  public String getDesc() {
    return "Get an item module.";
  }
  
  public String getPermission() {
    return "qrpg.module.get";
  }
  
  public boolean playersOnly() {
    return true;
  }
  
  public void perform(CommandSender sender, String label, String[] args) {
    if (args.length > 4) {
      printUsage(sender, label);
      return;
    } 
    String id = "random";
    if (args.length >= 2)
      id = args[1]; 
    int level = -1;
    int amount = 1;
    if (this.m instanceof su.nightexpress.quantumrpg.modules.QModuleLevel) {
      if (args.length >= 3)
        if (args[2].contains("-")) {
          try {
            int l1 = Integer.parseInt(args[2].split("-")[0]);
            int l2 = Integer.parseInt(args[2].split("-")[1]);
            level = Utils.randInt(l1, l2);
          } catch (NumberFormatException numberFormatException) {}
        } else {
          try {
            level = Integer.parseInt(args[2]);
          } catch (NumberFormatException numberFormatException) {}
        }  
      if (args.length == 4)
        try {
          amount = Integer.parseInt(args[3]);
        } catch (NumberFormatException ex) {
          sender.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Other_InvalidNumber.toMsg().replace("%s", args[3]));
        }  
    } else if (args.length == 3) {
      try {
        amount = Integer.parseInt(args[2]);
      } catch (NumberFormatException ex) {
        sender.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Other_InvalidNumber.toMsg().replace("%s", args[3]));
      } 
    } 
    Player p = (Player)sender;
    ItemStack item = null;
    for (int i = 0; i < amount; i++) {
      item = ItemAPI.getItemByModule(this.m.type(), id, level, -1);
      if (item != null)
        Utils.addItem(p, item); 
    } 
    String name = id;
    if (item != null && 
      !id.equalsIgnoreCase("random"))
      name = Utils.getItemName(item); 
    sender.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Module_Cmd_Get_Done.toMsg()
        .replace("%item%", name)
        .replace("%amount%", String.valueOf(amount)));
  }
}
