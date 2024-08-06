package studio.magemonkey.divinity.api.event;

import studio.magemonkey.codex.manager.api.event.ICancellableEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

@Getter
@RequiredArgsConstructor
public class DivinityItemDamageEvent extends ICancellableEvent {
    private final ItemStack    item;
    private final LivingEntity entity;
}
