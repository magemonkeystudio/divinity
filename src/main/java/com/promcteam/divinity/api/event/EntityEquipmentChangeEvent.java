package com.promcteam.divinity.api.event;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class EntityEquipmentChangeEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final LivingEntity entity;

    public EntityEquipmentChangeEvent(@NotNull LivingEntity entity) {
        this.entity = entity;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @NotNull
    public final HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public LivingEntity getEntity() {
        return this.entity;
    }
}
