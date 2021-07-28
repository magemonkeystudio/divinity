package su.nightexpress.quantumrpg.api.events;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class QuantumItemDamageEvent extends Event implements Cancellable {
  private static final HandlerList handlers = new HandlerList();
  
  private boolean cancelled;
  
  private ItemStack item;
  
  private LivingEntity li;
  
  public HandlerList getHandlers() {
    return handlers;
  }
  
  public static HandlerList getHandlerList() {
    return handlers;
  }
  
  public boolean isCancelled() {
    return this.cancelled;
  }
  
  public void setCancelled(boolean cancelled) {
    this.cancelled = cancelled;
  }
  
  public QuantumItemDamageEvent(ItemStack item, LivingEntity li) {
    this.item = item;
    this.li = li;
  }
  
  public ItemStack getItem() {
    return this.item;
  }
  
  public LivingEntity getEntity() {
    return this.li;
  }
}
