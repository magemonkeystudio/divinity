package com.promcteam.divinity.api.event;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class QuantumItemDamageEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private boolean cancelled;

    private ItemStack item;

    private LivingEntity li;

    public QuantumItemDamageEvent(@NotNull ItemStack item, @NotNull LivingEntity li) {
        this.item = item;
        this.li = li;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @NotNull
    public ItemStack getItem() {
        return this.item;
    }

    @NotNull
    public LivingEntity getEntity() {
        return this.li;
    }
}
