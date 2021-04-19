package su.nightexpress.quantumrpg.api.event;

import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.manager.api.event.ICancellableEvent;

public class RPGItemDamageEvent extends ICancellableEvent {

	private ItemStack item;
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
