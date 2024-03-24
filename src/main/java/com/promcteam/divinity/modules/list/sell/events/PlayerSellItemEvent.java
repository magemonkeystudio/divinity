package com.promcteam.divinity.modules.list.sell.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class PlayerSellItemEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

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

    public PlayerSellItemEvent(@NotNull Player p, @NotNull Map<ItemStack, Double> priceMap) {
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
        return new HashMap<>(this.priceMap);
    }

    public double getPrice() {
        return this.price;
    }
}
