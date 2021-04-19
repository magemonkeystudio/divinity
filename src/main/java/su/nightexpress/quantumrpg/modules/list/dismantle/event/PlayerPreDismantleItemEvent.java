package su.nightexpress.quantumrpg.modules.list.dismantle.event;

import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.manager.api.event.ICancellableEvent;
import su.nightexpress.quantumrpg.modules.list.dismantle.DismantleManager.OutputContainer;

public class PlayerPreDismantleItemEvent extends ICancellableEvent {

	private Player player;
	private double cost;
	private Map<ItemStack, OutputContainer> result;

	public PlayerPreDismantleItemEvent(
			@NotNull Player player,
			double cost,
			@NotNull Map<ItemStack, OutputContainer> result
			) {
		this.player = player;
		this.setCost(cost);
	    this.result = result;
	}
	
	public double getCost() {
		return this.cost;
	}
	
	public void setCost(double cost) {
		this.cost = cost;
	}
	
	@NotNull
	public Player getPlayer() {
		return this.player;
	}
	
	@NotNull
	public Map<ItemStack, OutputContainer> getResult() {
		return this.result;
	}
}
