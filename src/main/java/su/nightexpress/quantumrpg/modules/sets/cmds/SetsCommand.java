package su.nightexpress.quantumrpg.modules.sets.cmds;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.modules.cmds.MExecutor;
import su.nightexpress.quantumrpg.modules.sets.SetManager;
import su.nightexpress.quantumrpg.utils.Utils;

public class SetsCommand extends MExecutor {
  public SetsCommand(QuantumRPG plugin) {
    super(plugin);
  }
  
  public void setup() {
    register(new SetsGetCmd(this.plugin, this.m));
    register(new SetsGiveCmd(this.plugin, this.m));
    register(new SetsDropCmd(this.plugin, this.m));
    register(new SetsListCmd(this.plugin, this.m));
  }
  
  public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
    if (!(sender instanceof org.bukkit.entity.Player))
      return null; 
    if (args.length == 1) {
      List<String> sugg = Arrays.asList(new String[] { "help", "get", "give", "drop", "list", "reload", "info" });
      return Utils.getSugg(args[0], sugg);
    } 
    if (args.length == 2) {
      String sub = args[0];
      if (sub.equalsIgnoreCase("get")) {
        List<String> sugg = ((SetManager)this.m).getSetNames();
        return Utils.getSugg(args[1], sugg);
      } 
      if (sub.equalsIgnoreCase("give"))
        return null; 
      if (sub.equalsIgnoreCase("drop")) {
        List<String> sugg = Utils.getWorldNames();
        return Utils.getSugg(args[1], sugg);
      } 
    } else if (args.length == 3) {
      String sub = args[0];
      if (sub.equalsIgnoreCase("get")) {
        List<String> sugg = Utils.getEnumsList(SetManager.PartType.class);
        return sugg;
      } 
      if (sub.equalsIgnoreCase("give")) {
        List<String> sugg = ((SetManager)this.m).getSetNames();
        return Utils.getSugg(args[2], sugg);
      } 
      if (sub.equalsIgnoreCase("drop")) {
        List<String> sugg = Arrays.asList(new String[] { "<x>" });
        return sugg;
      } 
    } else if (args.length == 4) {
      String sub = args[0];
      if (sub.equalsIgnoreCase("get")) {
        List<String> sugg = Arrays.asList(new String[] { "<amount>" });
        return sugg;
      } 
      if (sub.equalsIgnoreCase("give")) {
        List<String> sugg = Utils.getEnumsList(SetManager.PartType.class);
        return sugg;
      } 
      if (sub.equalsIgnoreCase("drop")) {
        List<String> sugg = Arrays.asList(new String[] { "<y>" });
        return sugg;
      } 
    } else if (args.length == 5) {
      String sub = args[0];
      if (sub.equalsIgnoreCase("give")) {
        List<String> sugg = Arrays.asList(new String[] { "<amount>" });
        return sugg;
      } 
      if (sub.equalsIgnoreCase("drop")) {
        List<String> sugg = Arrays.asList(new String[] { "<z>" });
        return sugg;
      } 
    } else if (args.length == 6) {
      String sub = args[0];
      if (sub.equalsIgnoreCase("drop")) {
        List<String> sugg = ((SetManager)this.m).getSetNames();
        return Utils.getSugg(args[5], sugg);
      } 
    } else if (args.length == 6) {
      String sub = args[0];
      if (sub.equalsIgnoreCase("drop")) {
        List<String> sugg = Utils.getEnumsList(SetManager.PartType.class);
        return sugg;
      } 
    } else if (args.length == 7) {
      String sub = args[0];
      if (sub.equalsIgnoreCase("drop")) {
        List<String> sugg = Arrays.asList(new String[] { "<amount>" });
        return sugg;
      } 
    } 
    return Collections.emptyList();
  }
}
