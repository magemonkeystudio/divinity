package su.nightexpress.quantumrpg.manager;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;

import mc.promcteam.engine.manager.IListener;
import mc.promcteam.engine.manager.api.task.ITask;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.api.event.EntityEquipmentChangeEvent;
import su.nightexpress.quantumrpg.api.event.EntityRPGItemPickupEvent;
import su.nightexpress.quantumrpg.api.event.RPGDamageEvent;
import su.nightexpress.quantumrpg.modules.api.QModuleDrop;
import su.nightexpress.quantumrpg.stats.EntityStats;
import su.nightexpress.quantumrpg.stats.EntityStatsTask;
import su.nightexpress.quantumrpg.stats.ProjectileStats;
import su.nightexpress.quantumrpg.stats.items.ItemStats;
import su.nightexpress.quantumrpg.stats.items.attributes.api.AbstractStat;

public class EntityManager extends IListener<QuantumRPG> {

	private Map<LivingEntity, Long> updateQueue;
	private StatsUpdater statsUpdater;
	private EntityStatsTask entityStatsTask;
	
	private static final String PACKET_DUPLICATOR_FIXER = "PACKET_DUPLICATOR_FIXER";
	
	public EntityManager(@NotNull QuantumRPG plugin) {
		super(plugin);
	}

	public void setup() {
		this.updateQueue = new HashMap<>();
		
		this.statsUpdater = new StatsUpdater(plugin);
		this.statsUpdater.start();
		
		this.entityStatsTask = new EntityStatsTask(plugin);
		this.entityStatsTask.start();
		
		this.registerListeners();
	}
	
	public void shutdown() {
		this.unregisterListeners();
		
		if (this.statsUpdater != null) {
			this.statsUpdater.stop();
			this.statsUpdater = null;
		}
		if (this.entityStatsTask != null) {
			this.entityStatsTask.stop();
			this.entityStatsTask = null;
		}
		if (this.updateQueue != null) {
			this.updateQueue.clear();
			this.updateQueue = null;
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onStatsDeath(EntityDeathEvent e) {
		LivingEntity entity = e.getEntity();
		EntityStats.get(entity).handleDeath();
	}
	
	// Clear stats on player exit
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onStatsQuit(PlayerQuitEvent e) {
		EntityStats.purge(e.getPlayer());
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onStatsJoin(PlayerJoinEvent e) {
		EntityStats.get(e.getPlayer());
		this.pushToUpdate(e.getPlayer(), 1D);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onStatsRegen(EntityRegainHealthEvent e) {
		Entity e1 = e.getEntity();
		if (!(e1 instanceof LivingEntity)) return;
		
		LivingEntity entity = (LivingEntity) e1;
		double regen = 1D + EntityStats.get(entity).getItemStat(AbstractStat.Type.HEALTH_REGEN, false) / 100D;
		e.setAmount(e.getAmount() * regen);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onPickup(EntityPickupItemEvent e) {
		if (!ProjectileStats.isPickable(e.getItem())) {
			e.setCancelled(true);
		}
		
		ItemStack item = e.getItem().getItemStack();
		QModuleDrop<?> moduleDrop = ItemStats.getModule(item);
		if (moduleDrop == null) return;
		
		EntityRPGItemPickupEvent ev = new EntityRPGItemPickupEvent(item, e.getEntity(), moduleDrop);
		plugin.getPluginManager().callEvent(ev);
		e.setCancelled(ev.isCancelled());
	}
	
	
	
	
	
	private final void pushToUpdate(@NotNull LivingEntity entity, double time) {
		if (time <= 0D) {
			plugin.getServer().getScheduler().runTask(plugin, () -> {
				EntityStats.get(entity).updateAll();
			});
			return;
		}
		this.updateQueue.put(entity, System.currentTimeMillis() + (long)(1000D * time));
	}
	
	private final void addDuplicatorFixer(@NotNull Entity entity) {
		entity.setMetadata(PACKET_DUPLICATOR_FIXER, new FixedMetadataValue(plugin, "fixed"));
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onStatsSprintPacketDuplicator(PlayerToggleSprintEvent e) {
		this.addDuplicatorFixer(e.getPlayer());
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onStatsDamagePacketDuplicator(RPGDamageEvent.Exit e) {
		LivingEntity victim = e.getVictim();
		this.addDuplicatorFixer(victim);
		this.pushToUpdate(victim, 1.5D);
		
		LivingEntity damager = e.getDamager();
		if (damager != null) {
			this.addDuplicatorFixer(damager);
			this.pushToUpdate(damager, 1.5D);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onStatsRegenPacketDuplicator(EntityRegainHealthEvent e) {
		this.addDuplicatorFixer(e.getEntity());
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onStatsUpdateMobSpawn(CreatureSpawnEvent e) {
		this.pushToUpdate(e.getEntity(), 1D);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onStatsUpdatePlayerRespawn(PlayerRespawnEvent e) {
		this.pushToUpdate(e.getPlayer(), 1D);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onStatsUpdatePlayerHeld(PlayerItemHeldEvent e) {
		this.addDuplicatorFixer(e.getPlayer());
		this.pushToUpdate(e.getPlayer(), 0D);
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityUpdateEquipmentChange(EntityEquipmentChangeEvent e) {
		this.pushToUpdate(e.getEntity(), 0.5D);
	}
	
	public static boolean isPacketDuplicatorFixed(@NotNull Entity entity) {
		if (entity.hasMetadata(PACKET_DUPLICATOR_FIXER)) {
			entity.removeMetadata(PACKET_DUPLICATOR_FIXER, QuantumRPG.getInstance());
			return true;
		}
		return false;
	}
	
	class StatsUpdater extends ITask<QuantumRPG> {

		public StatsUpdater(@NotNull QuantumRPG plugin) {
			super(plugin, 10L, false);
		}

		@Override
		public void action() {
			updateQueue.entrySet().removeIf(en -> {
				if (System.currentTimeMillis() >= en.getValue()) {
					EntityStats.get(en.getKey()).updateAll();
					return true;
				}
				return false;
			});
		}
	}
}
