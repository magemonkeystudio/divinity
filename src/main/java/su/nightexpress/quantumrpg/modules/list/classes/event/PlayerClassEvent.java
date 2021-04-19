package su.nightexpress.quantumrpg.modules.list.classes.event;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.manager.api.event.IEvent;
import su.nightexpress.quantumrpg.modules.list.classes.api.UserClassData;

public abstract class PlayerClassEvent extends IEvent {
	
	private Player player;
	private UserClassData classData;
	
	public PlayerClassEvent(@NotNull Player player, @NotNull UserClassData classData) {
		this.player = player;
		this.classData = classData;
	}
	
	@NotNull
	public Player getPlayer() {
		return player;
	}
	
	@NotNull
	public UserClassData getClassData() {
		return classData;
	}
}
