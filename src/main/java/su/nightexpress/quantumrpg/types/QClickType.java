package su.nightexpress.quantumrpg.types;

import org.bukkit.event.block.Action;

public enum QClickType {
  RIGHT, LEFT, SHIFT_RIGHT, SHIFT_LEFT;
  
  public static QClickType getFromAction(Action a, boolean shift) {
    QClickType type;
    String tt = "";
    if (shift)
      tt = "SHIFT_"; 
    if (a.name().startsWith("RIGHT")) {
      tt = String.valueOf(tt) + "RIGHT";
    } else if (a.name().startsWith("LEFT")) {
      tt = String.valueOf(tt) + "LEFT";
    } 
    try {
      type = valueOf(tt);
    } catch (IllegalArgumentException ex) {
      return null;
    } 
    return type;
  }
}
