package su.nightexpress.quantumrpg.modules.customitems.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.quantumrpg.modules.customitems.CustomItemsManager;
import su.nightexpress.quantumrpg.types.QClickType;

public class QuantumPlayerCustomItemUseEvent extends Event implements Cancellable {
  private static final HandlerList handlers = new HandlerList();
  
  private boolean cancelled;
  
  private ItemStack item;
  
  private CustomItemsManager.CustomItem ci;
  
  private Player p;
  
  private QClickType click;
  
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
  
  public QuantumPlayerCustomItemUseEvent(ItemStack item, Player p, CustomItemsManager.CustomItem ci, QClickType click) {
    this.item = item;
    this.ci = ci;
    this.p = p;
    this.click = click;
  }
  
  public CustomItemsManager.CustomItem getCustomItem() {
    return this.ci;
  }
  
  public ItemStack getItem() {
    return this.item;
  }
  
  public Player getPlayer() {
    return this.p;
  }
  
  public QClickType getClickType() {
    return this.click;
  }
}
