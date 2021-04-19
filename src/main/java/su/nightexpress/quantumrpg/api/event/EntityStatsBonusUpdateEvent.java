package su.nightexpress.quantumrpg.api.event;

import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.manager.api.event.IEvent;
import su.nightexpress.quantumrpg.stats.EntityStats;

public class EntityStatsBonusUpdateEvent extends IEvent {
	
	protected LivingEntity entity;
	protected EntityStats stats;

	public EntityStatsBonusUpdateEvent(
			@NotNull LivingEntity entity,
			@NotNull EntityStats stats
			) {
	    this.entity = entity;
	    this.stats = stats;
	}
	
	@NotNull
	public LivingEntity getEntity() {
		return this.entity;
	}
	
	@NotNull
	public final EntityStats getStats() {
		return this.stats;
	}
}
