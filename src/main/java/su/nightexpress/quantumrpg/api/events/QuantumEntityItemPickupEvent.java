package su.nightexpress.quantumrpg.api.events;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.quantumrpg.modules.EModule;

public class QuantumEntityItemPickupEvent extends Event implements Cancellable {
  private static final HandlerList handlers = new HandlerList();
  
  private boolean cancelled;
  
  private ItemStack item;
  
  private LivingEntity li;
  
  private EModule module;
  
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
  
  public QuantumEntityItemPickupEvent(ItemStack item, LivingEntity li, EModule module) {
    this.item = item;
    this.li = li;
    this.module = module;
  }
  
  public ItemStack getItem() {
    return this.item;
  }
  
  public LivingEntity getEntity() {
    return this.li;
  }
  
  public EModule getModule() {
    return this.module;
  }
}
