package su.nightexpress.quantumrpg.modules.list.extractor.event;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.manager.api.event.ICancellableEvent;
import su.nightexpress.quantumrpg.stats.items.attributes.SocketAttribute;

public class PlayerExtractSocketEvent extends ICancellableEvent {

	private Player player;
	private ItemStack item;
	private ItemStack result;
	private SocketAttribute.Type sock;
	private boolean isError;

	public PlayerExtractSocketEvent(
			@NotNull Player player,
			@NotNull ItemStack item,
			@NotNull ItemStack result,
			@NotNull SocketAttribute.Type sock
			) {
	    this.item = item;
	    this.result = result;
	    this.sock = sock;
	    this.player = player;
	    this.setFailed(false);
	}
	
	@NotNull
	public SocketAttribute.Type getSocketType() {
		return this.sock;
	}
	
	@NotNull
	public Player getPlayer() {
		return this.player;
	}

	@NotNull
	public ItemStack getItem() {
		return this.item;
	}
	
	@NotNull
	public ItemStack getResult() {
		return this.result;
	}
	
	public boolean isFailed() {
		return this.isError;
	}
	
	public void setFailed(boolean isFailed) {
		this.isError = isFailed;
	}
}
