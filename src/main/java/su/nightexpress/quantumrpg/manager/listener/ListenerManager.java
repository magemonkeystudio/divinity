package su.nightexpress.quantumrpg.manager.listener;

import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.manager.api.Loadable;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.manager.listener.object.ItemDurabilityListener;
import su.nightexpress.quantumrpg.manager.listener.object.ItemHandListener;
import su.nightexpress.quantumrpg.manager.listener.object.ItemRequirementListener;
import su.nightexpress.quantumrpg.manager.listener.object.VanillaWrapperListener;
import su.nightexpress.quantumrpg.stats.items.ItemStats;
import su.nightexpress.quantumrpg.stats.items.attributes.stats.DurabilityStat;
import su.nightexpress.quantumrpg.stats.items.requirements.ItemRequirements;

public class ListenerManager implements Loadable {

	private QuantumRPG plugin;
	private ItemDurabilityListener lisDurability;
	private ItemHandListener lisHand;
	private ItemRequirementListener lisReq;
	private VanillaWrapperListener lisQuantum;
	
	public ListenerManager(@NotNull QuantumRPG plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public void setup() {
		DurabilityStat duraStat = ItemStats.getStat(DurabilityStat.class);
		if (duraStat != null) {
			this.lisDurability = new ItemDurabilityListener(this.plugin, duraStat);
			this.lisDurability.registerListeners();
		}
		
		if (!ItemStats.getHands().isEmpty()) {
			this.lisHand = new ItemHandListener(this.plugin);
			this.lisHand.registerListeners();
		}
		
		if (!ItemRequirements.getUserRequirements().isEmpty()) {
			this.lisReq = new ItemRequirementListener(this.plugin);
			this.lisReq.registerListeners();
		}
		
		this.lisQuantum = new VanillaWrapperListener(this.plugin);
		this.lisQuantum.registerListeners();
	}
	
	@Override
	public void shutdown() {
		if (this.lisDurability != null) {
			this.lisDurability.unregisterListeners();
			this.lisDurability = null;
		}
		if (this.lisHand != null) {
			this.lisHand.unregisterListeners();
			this.lisHand = null;
		}
		if (this.lisReq != null) {
			this.lisReq.unregisterListeners();
			this.lisReq = null;
		}
		if (this.lisQuantum != null) {
			this.lisQuantum.unregisterListeners();
			this.lisQuantum = null;
		}
	}
}
