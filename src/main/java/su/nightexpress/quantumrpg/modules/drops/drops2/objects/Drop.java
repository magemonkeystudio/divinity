package su.nightexpress.quantumrpg.modules.drops.drops2.objects;

import java.util.Set;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class Drop implements DropCalculator {
  protected String itemId;
  
  protected int minAmount;
  
  protected int maxAmount;
  
  protected float chance;
  
  protected boolean noReduce = false;
  
  public Drop(String itemId, int minAmount, int maxAmount, float chance) {
    this.itemId = itemId;
    this.minAmount = minAmount;
    this.maxAmount = maxAmount;
    this.chance = chance;
  }
  
  public String getItemId() {
    return this.itemId;
  }
  
  public int getMinAmount() {
    return this.minAmount;
  }
  
  public int getMaxAmount() {
    return this.maxAmount;
  }
  
  public float getChance() {
    return this.chance;
  }
  
  public boolean isNoReduction() {
    return this.noReduce;
  }
  
  public int dropCalculator(Player killer, LivingEntity npc, Set<DropItem> result, int index, float dropModifier) {
    if (this.itemId == null || this.itemId.isEmpty() || this.itemId.equalsIgnoreCase("null"))
      return index; 
    float percent = this.chance;
    if (!this.noReduce)
      percent *= dropModifier; 
    if (Rnd.get() * 100.0F < percent) {
      DropItem dropitem = new DropItem(this);
      dropitem.calculateCount();
      dropitem.setIndex(index++);
      result.add(dropitem);
    } 
    return index;
  }
  
  public String toString() {
    return "Drop [itemId=" + this.itemId + ", minAmount=" + this.minAmount + ", maxAmount=" + this.maxAmount + ", chance=" + this.chance + ", noReduce=" + this.noReduce + "]";
  }
}
