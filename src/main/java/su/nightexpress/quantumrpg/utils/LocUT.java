package su.nightexpress.quantumrpg.utils;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import su.nightexpress.quantumrpg.utils.logs.LogType;
import su.nightexpress.quantumrpg.utils.logs.LogUtil;

public class LocUT {
  public static String serialize(Location loc) {
    if (loc == null)
      return null; 
    return String.valueOf(loc.getX()) + 
      "," + 
      loc.getY() + 
      "," + 
      loc.getZ() + 
      "," + 
      loc.getPitch() + 
      "," + 
      loc.getYaw() + 
      "," + 
      loc.getWorld().getName();
  }
  
  public static List<String> serialize(List<Location> list) {
    List<String> ser = new ArrayList<>();
    for (Location loc : list)
      ser.add(serialize(loc)); 
    return ser;
  }
  
  public static Location deserialize(String s) {
    if (s == null)
      return null; 
    String[] s1 = s.split(",");
    try {
      double x = Double.parseDouble(s1[0]);
      double y = Double.parseDouble(s1[1]);
      double z = Double.parseDouble(s1[2]);
      double pitch = Double.parseDouble(s1[3]);
      double yaw = Double.parseDouble(s1[4]);
      String wn = s1[5];
      World w = Bukkit.getWorld(wn);
      if (w == null) {
        LogUtil.send("Deserialization error: Invalid location or world: &f" + s, LogType.ERROR);
        return null;
      } 
      return new Location(w, x, y, z, (float)yaw, (float)pitch);
    } catch (ArrayIndexOutOfBoundsException|NumberFormatException ex) {
      LogUtil.send("Deserialization error: Invalid string location: &f" + s, LogType.ERROR);
      return null;
    } 
  }
  
  public static List<Location> deserialize(List<String> list) {
    List<Location> ser = new ArrayList<>();
    for (String s : list) {
      Location loc = deserialize(s);
      if (loc == null)
        continue; 
      ser.add(deserialize(s));
    } 
    return ser;
  }
  
  public static Location getCenter(Location loc) {
    if (loc == null)
      return null; 
    return new Location(loc.getWorld(), 
        getRelativeCoord(loc.getBlockX()), 
        getRelativeCoord(loc.getBlockY()), 
        getRelativeCoord(loc.getBlockZ()));
  }
  
  private static double getRelativeCoord(int i) {
    double d = i;
    d = (d < 0.0D) ? (d + 0.5D) : (d + 0.5D);
    return d;
  }
}
