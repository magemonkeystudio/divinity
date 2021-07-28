package su.nightexpress.quantumrpg.utils.logs;

import net.md_5.bungee.api.ChatColor;
import su.nightexpress.quantumrpg.QuantumRPG;

public class LogUtil {
  private static QuantumRPG plugin = QuantumRPG.instance;
  
  private static String pl = plugin.getDescription().getName();
  
  private static int warns = 0;
  
  private static int errs = 0;
  
  public static void send(String msg, LogType type) {
    String out = type.color() + "[" + pl + "/" + type.name() + "] " + ChatColor.GRAY + msg;
    out = ChatColor.translateAlternateColorCodes('&', out);
    plugin.getServer().getConsoleSender().sendMessage(out);
    if (type == LogType.WARN) {
      warns++;
    } else if (type == LogType.ERROR) {
      errs++;
    } 
  }
  
  public static int getWarns() {
    return warns;
  }
  
  public static int getErrors() {
    return errs;
  }
}
