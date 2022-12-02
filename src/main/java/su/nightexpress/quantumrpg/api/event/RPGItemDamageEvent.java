package su.nightexpress.quantumrpg.api.event;

import mc.promcteam.engine.manager.api.event.ICancellableEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class RPGItemDamageEvent extends ICancellableEvent {

    private ItemStack    item;
    private LivingEntity entity;

    public RPGItemDamageEvent(
            @NotNull ItemStack item,
            @NotNull LivingEntity entity
    ) {
        this.item = item;
        this.entity = entity;
    }

    @NotNull
    public ItemStack getItem() {
        return this.item;
    }

    @NotNull
    public LivingEntity getEntity() {
        return this.entity;
    }
}
