package studio.magemonkey.divinity.modules.list.sell.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class PlayerPreSellItemEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private boolean isCancelled;

    private Player p;

    private double price;

    private Map<ItemStack, Double> priceMap;

    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }

    public boolean isCancelled() {
        return this.isCancelled;
    }

    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    public PlayerPreSellItemEvent(@NotNull Player p, @NotNull Map<ItemStack, Double> priceMap) {
        this.p = p;
        this.priceMap = priceMap;
        this.priceMap.forEach((item, itemCost) -> this.price += itemCost.doubleValue());
    }

    @NotNull
    public Player getPlayer() {
        return this.p;
    }

    @NotNull
    public Map<ItemStack, Double> getPriceMap() {
        return this.priceMap;
    }

    public double getPrice() {
        return this.price;
    }
}
