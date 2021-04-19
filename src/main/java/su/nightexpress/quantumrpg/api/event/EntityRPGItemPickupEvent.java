package su.nightexpress.quantumrpg.api.event;

import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.manager.api.event.ICancellableEvent;
import su.nightexpress.quantumrpg.modules.api.QModuleDrop;

public class EntityRPGItemPickupEvent extends ICancellableEvent {

	private ItemStack item;
	private LivingEntity entity;
	private QModuleDrop<?> module;

	public EntityRPGItemPickupEvent(
			@NotNull ItemStack item,
			@NotNull LivingEntity entity,
			@NotNull QModuleDrop<?> module
			) {
	    this.item = item;
	    this.entity = entity;
	    this.module = module;
	}
	 
	@NotNull
	public ItemStack getItem() {
		return this.item;
	}
	
	@NotNull
	public LivingEntity getEntity() {
		return this.entity;
	}
	
	@NotNull
	public QModuleDrop<?> getModule() {
		return this.module;
	}
}
