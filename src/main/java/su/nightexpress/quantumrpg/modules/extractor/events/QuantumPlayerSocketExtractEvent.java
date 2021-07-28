package su.nightexpress.quantumrpg.modules.extractor.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.quantumrpg.types.QSlotType;

public class QuantumPlayerSocketExtractEvent extends Event implements Cancellable {
  private static final HandlerList handlers = new HandlerList();
  
  private boolean cancelled;
  
  private ItemStack item;
  
  private ItemStack result;
  
  private QSlotType sock;
  
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
  
  public QuantumPlayerSocketExtractEvent(ItemStack item, ItemStack result, Player p, QSlotType sock) {
    this.item = item;
    this.result = result;
    this.sock = sock;
    this.p = p;
  }
  
  public QSlotType getSocketType() {
    return this.sock;
  }
  
  public ItemStack getItem() {
    return this.item;
  }
  
  public ItemStack getResult() {
    return this.result;
  }
  
  public Player getPlayer() {
    return this.p;
  }
}
