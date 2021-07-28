package su.nightexpress.quantumrpg.cmds;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;
import su.nightexpress.quantumrpg.utils.logs.LogType;
import su.nightexpress.quantumrpg.utils.logs.LogUtil;

public class CommandRegister extends Command implements PluginIdentifiableCommand {
  protected Plugin plugin;
  
  protected final CommandExecutor owner;
  
  protected TabCompleter tab;
  
  protected final Object registeredWith;
  
  public CommandRegister(String[] aliases, String desc, String usage, CommandExecutor owner, Object registeredWith, Plugin plugin2) {
    super(aliases[0], desc, usage, Arrays.asList(aliases));
    this.owner = owner;
    this.plugin = plugin2;
    this.registeredWith = registeredWith;
  }
  
  public void setTabCompleter(TabCompleter tab) {
    this.tab = tab;
  }
  
  public Plugin getPlugin() {
    return this.plugin;
  }
  
  public boolean execute(CommandSender sender, String label, String[] args) {
    return this.owner.onCommand(sender, this, label, args);
  }
  
  public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
    List<String> completions = null;
    if (this.tab != null)
      completions = this.tab.onTabComplete(sender, this, alias, args); 
    if (completions == null && this.tab instanceof TabCompleter)
      completions = this.tab.onTabComplete(sender, this, alias, args); 
    return completions;
  }
  
  public Object getRegisteredWith() {
    return this.registeredWith;
  }
  
  public static void reg(Plugin plugin, CommandExecutor cxecutor, TabCompleter tab, String[] aliases, String desc, String usage) {
    try {
      CommandRegister reg = new CommandRegister(aliases, desc, usage, cxecutor, new Object(), plugin);
      reg.setTabCompleter(tab);
      Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
      field.setAccessible(true);
      CommandMap map = (CommandMap)field.get(Bukkit.getServer());
      map.register(plugin.getDescription().getName(), reg);
    } catch (Exception e) {
      e.printStackTrace();
    } 
  }
  
  private static Object getPrivateField(Object object, String field) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
    Class<?> clazz = object.getClass();
    Field objectField = clazz.getDeclaredField(field);
    objectField.setAccessible(true);
    Object result = objectField.get(object);
    objectField.setAccessible(false);
    return result;
  }
  
  public static void unreg(Plugin plugin, String[] aliases) {
    try {
      SimpleCommandMap map = (SimpleCommandMap)getPrivateField(plugin.getServer().getPluginManager(), "commandMap");
      HashMap<String, Command> knownCommands = (HashMap<String, Command>)getPrivateField(map, "knownCommands");
      byte b;
      int i;
      String[] arrayOfString;
      for (i = (arrayOfString = aliases).length, b = 0; b < i; ) {
        String al = arrayOfString[b];
        Command c = map.getCommand(al);
        if (c != null) {
          if (!c.unregister((CommandMap)map))
            LogUtil.send("Unable to unregister command &f" + al, LogType.ERROR); 
          knownCommands.remove(al);
        } 
        b++;
      } 
    } catch (Exception e) {
      e.printStackTrace();
    } 
  }
}
