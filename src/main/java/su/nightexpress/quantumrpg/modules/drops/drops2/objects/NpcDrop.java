package su.nightexpress.quantumrpg.modules.drops.drops2.objects;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class NpcDrop implements DropCalculator {
  protected float chance;
  
  protected boolean rollOnce;
  
  protected List<String> entity;
  
  protected List<String> mythic;
  
  protected List<String> reasons;
  
  protected List<DropGroup> dropGroup;
  
  public NpcDrop(float chance, boolean roll_once, List<String> entity, List<String> mythic, List<String> reasons, List<DropGroup> drop_tables) {
    this.chance = chance;
    this.rollOnce = roll_once;
    this.entity = entity;
    this.mythic = mythic;
    this.reasons = reasons;
    this.dropGroup = drop_tables;
  }
  
  public boolean isRollOnce() {
    return this.rollOnce;
  }
  
  public float getChance() {
    return this.chance;
  }
  
  public List<String> getEntities() {
    return this.entity;
  }
  
  public List<String> getMythic() {
    return this.mythic;
  }
  
  public List<String> getReasons() {
    return this.reasons;
  }
  
  public List<DropGroup> getDropGroup() {
    if (this.dropGroup == null)
      return Collections.emptyList(); 
    return this.dropGroup;
  }
  
  public int dropCalculator(Player killer, LivingEntity npc, Set<DropItem> result, int index, float dropModifier) {
    if (this.dropGroup == null || this.dropGroup.isEmpty())
      return index; 
    float percent = this.chance;
    percent *= dropModifier;
    if (Rnd.get() * 100.0F >= percent)
      return index; 
    if (this.rollOnce) {
      DropGroup dg = this.dropGroup.get(Rnd.get(0, this.dropGroup.size() - 1));
      return dg.dropCalculator(killer, npc, result, index, dropModifier);
    } 
    for (DropGroup dg : this.dropGroup)
      index = dg.dropCalculator(killer, npc, result, index, dropModifier); 
    return index;
  }
}
