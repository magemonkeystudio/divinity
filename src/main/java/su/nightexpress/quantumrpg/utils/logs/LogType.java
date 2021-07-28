package su.nightexpress.quantumrpg.utils.logs;

import org.bukkit.ChatColor;

public enum LogType {
  INFO(ChatColor.DARK_GREEN),
  WARN(ChatColor.YELLOW),
  ERROR(ChatColor.RED),
  DEBUG(ChatColor.AQUA);
  
  private ChatColor c;
  
  LogType(ChatColor c) {
    this.c = c;
  }
  
  public ChatColor color() {
    return this.c;
  }
}
