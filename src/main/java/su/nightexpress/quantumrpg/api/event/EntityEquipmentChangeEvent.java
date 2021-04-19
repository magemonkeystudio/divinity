package su.nightexpress.quantumrpg.api.event;

import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.manager.api.event.IEvent;

public class EntityEquipmentChangeEvent extends IEvent {
	
	private LivingEntity entity;

	public EntityEquipmentChangeEvent(@NotNull LivingEntity entity) {
	    this.entity = entity;
	}
	
	@NotNull
	public LivingEntity getEntity() {
		return this.entity;
	}
}
