package su.nightexpress.quantumrpg.modules.cmds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.cmds.ICmd;
import su.nightexpress.quantumrpg.config.Lang;
import su.nightexpress.quantumrpg.modules.QModule;
import su.nightexpress.quantumrpg.modules.QModuleDrop;
import su.nightexpress.quantumrpg.utils.Utils;

public abstract class MExecutor implements CommandExecutor, TabExecutor {
  protected QuantumRPG plugin;
  
  protected QModule m;
  
  protected Map<String, ICmd> commands;
  
  public MExecutor(QuantumRPG plugin) {
    this.plugin = plugin;
  }
  
  public abstract void setup();
  
  public void init(QModule m) {
    this.m = m;
    this.commands = new LinkedHashMap<>();
    register(new MHelpCmd(m, this));
    if (m instanceof QModuleDrop) {
      QModuleDrop md = (QModuleDrop)m;
      register(new MGetCmd(md));
      register(new MGiveCmd(this.plugin, md));
      register(new MDropCmd(this.plugin, md));
      register(new MListCmd(this.plugin, md));
    } 
    register(new MInfoCmd(m));
    register(new MReloadCmd(m));
    setup();
  }
  
  public void register(ICmd cmd) {
    this.commands.put(cmd.getLabel(), cmd);
  }
  
  public void shutdown() {
    this.commands = null;
  }
  
  public void printHelp(CommandSender sender) {
    for (String s : Lang.Module_Cmd_Help_List.getList()) {
      if (s.equals("%cmds%")) {
        for (ICmd cmd : this.commands.values()) {
          String f = Lang.Module_Cmd_Help_Format.toMsg()
            .replace("%cmd%", this.m.cmd())
            .replace("%label%", cmd.getLabel())
            .replace("%desc%", cmd.getDesc())
            .replace("%usage%", cmd.getUsage());
          sender.sendMessage(f);
        } 
        continue;
      } 
      sender.sendMessage(s.replace("%module%", this.m.name()));
    } 
  }
  
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    ICmd command = null;
    if (args.length > 0 && this.commands.containsKey(args[0]))
      command = this.commands.get(args[0]); 
    if (command == null) {
      printHelp(sender);
      return true;
    } 
    command.execute(sender, label, args);
    return true;
  }
  
  public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
    if (!(sender instanceof org.bukkit.entity.Player))
      return null; 
    if (args.length == 1) {
      List<String> sugg = new ArrayList<>(this.commands.keySet());
      return Utils.getSugg(args[0], sugg);
    } 
    ICmd cb = this.commands.get(args[0]);
    if (cb == null)
      return Collections.emptyList(); 
    List<String> list = cb.getTab(args.length);
    return Utils.getSugg(args[args.length - 1], list);
  }
}
