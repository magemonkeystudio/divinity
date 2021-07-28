package su.nightexpress.quantumrpg.modules.consumes.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.quantumrpg.modules.consumes.ConsumeManager;

public class QuantumPlayerConsumeItemEvent extends Event implements Cancellable {
  private static final HandlerList handlers = new HandlerList();
  
  private boolean cancelled;
  
  private ItemStack item;
  
  private ConsumeManager.Consume ci;
  
  private Player p;
  
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
  
  public QuantumPlayerConsumeItemEvent(ItemStack item, Player p, ConsumeManager.Consume ci) {
    this.item = item;
    this.ci = ci;
    this.p = p;
  }
  
  public ConsumeManager.Consume getConsumeItem() {
    return this.ci;
  }
  
  public ItemStack getItem() {
    return this.item;
  }
  
  public Player getPlayer() {
    return this.p;
  }
}
