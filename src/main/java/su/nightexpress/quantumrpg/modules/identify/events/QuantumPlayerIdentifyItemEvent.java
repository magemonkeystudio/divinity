package su.nightexpress.quantumrpg.modules.identify.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.quantumrpg.modules.identify.IdentifyManager;

public class QuantumPlayerIdentifyItemEvent extends Event implements Cancellable {
  private static final HandlerList handlers = new HandlerList();
  
  private boolean cancelled;
  
  private IdentifyManager.UnidentifiedItem target;
  
  private IdentifyManager.IdentifyTome tome;
  
  private ItemStack result;
  
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
  
  public QuantumPlayerIdentifyItemEvent(IdentifyManager.UnidentifiedItem target, IdentifyManager.IdentifyTome tome, ItemStack result, Player p) {
    this.target = target;
    this.tome = tome;
    this.result = result;
    this.p = p;
  }
  
  public IdentifyManager.IdentifyTome getTome() {
    return this.tome;
  }
  
  public IdentifyManager.UnidentifiedItem getTargetItem() {
    return this.target;
  }
  
  public ItemStack getResult() {
    return this.result;
  }
  
  public Player getPlayer() {
    return this.p;
  }
}
