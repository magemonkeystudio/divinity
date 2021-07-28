package su.nightexpress.quantumrpg.modules.drops.drops2.objects;

import java.util.List;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import su.nightexpress.quantumrpg.hooks.EHook;
import su.nightexpress.quantumrpg.hooks.external.WorldGuardHook;

public class DropGroup implements DropCalculator {
  protected String id;
  
  protected String group_name;
  
  protected boolean rollOnce = true;
  
  protected List<String> worlds_white;
  
  protected List<String> biomes_white;
  
  protected List<String> regions_black;
  
  protected List<Drop> drop;
  
  public DropGroup(String id, String name, boolean rollOnce, List<String> worlds_white, List<String> biomes_white, List<String> regions_black, List<Drop> drop) {
    this.id = id.toLowerCase();
    this.group_name = ChatColor.translateAlternateColorCodes('&', name);
    this.rollOnce = rollOnce;
    this.worlds_white = worlds_white;
    this.biomes_white = biomes_white;
    this.regions_black = regions_black;
    this.drop = drop;
  }
  
  public String getId() {
    return this.id;
  }
  
  public String getGroupName() {
    if (this.group_name == null)
      return ""; 
    return this.group_name;
  }
  
  public boolean isRollOnce() {
    return this.rollOnce;
  }
  
  public List<String> getAllowedWorlds() {
    return this.worlds_white;
  }
  
  public List<String> getAllowedBiomes() {
    return this.biomes_white;
  }
  
  public List<String> getDisallowedRegions() {
    return this.regions_black;
  }
  
  public List<Drop> getDrop() {
    return this.drop;
  }
  
  protected boolean checkForLocation(LivingEntity npc) {
    String w = npc.getWorld().getName();
    if (!this.worlds_white.contains("ALL") && 
      !this.worlds_white.contains(w))
      return false; 
    String biome = npc.getLocation().getBlock().getBiome().name();
    if (!this.biomes_white.contains("ALL") && 
      !this.biomes_white.contains(biome))
      return false; 
    if (EHook.WORLD_GUARD.isEnabled()) {
      WorldGuardHook wg = (WorldGuardHook)EHook.WORLD_GUARD.getHook();
      String region = wg.getRegion(npc);
      if (this.regions_black.contains("ALL") || 
        this.regions_black.contains(region))
        return false; 
    } 
    return true;
  }
  
  public int dropCalculator(Player killer, LivingEntity npc, Set<DropItem> result, int index, float dropModifier) {
    if (!checkForLocation(npc))
      return index; 
    if (this.rollOnce) {
      Drop d = this.drop.get(Rnd.get(0, this.drop.size() - 1));
      return d.dropCalculator(killer, npc, result, index, dropModifier);
    } 
    for (int i = 0; i < this.drop.size(); i++) {
      Drop d = this.drop.get(i);
      index = d.dropCalculator(killer, npc, result, index, dropModifier);
    } 
    return index;
  }
}
