package su.nightexpress.quantumrpg.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import su.nightexpress.quantumrpg.hooks.HookUtils;

public class EntityUtils {
  private static Set<Entity> set = new HashSet<>();
  
  public static void add(Entity e) {
    set.add(e);
  }
  
  public static void remove(Entity e) {
    set.remove(e);
  }
  
  public static Set<Entity> getSaved() {
    return set;
  }
  
  public static List<LivingEntity> getEnemies(Entity source, double range, Player p) {
    List<LivingEntity> list = new ArrayList<>();
    for (Entity e1 : source.getNearbyEntities(range, range, range)) {
      if (!(e1 instanceof LivingEntity) || 
        e1 instanceof org.bukkit.entity.ArmorStand)
        continue; 
      if (!HookUtils.canFights(source, e1));
      list.add((LivingEntity)e1);
    } 
    return list;
  }
}
