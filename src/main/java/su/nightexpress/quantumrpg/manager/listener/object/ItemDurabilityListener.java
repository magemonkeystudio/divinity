package su.nightexpress.quantumrpg.manager.listener.object;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.manager.IListener;
import su.nexmedia.engine.utils.ItemUT;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.api.event.QuantumProjectileLaunchEvent;
import su.nightexpress.quantumrpg.api.event.RPGDamageEvent;
import su.nightexpress.quantumrpg.stats.items.ItemStats;
import su.nightexpress.quantumrpg.stats.items.attributes.api.AbstractStat;
import su.nightexpress.quantumrpg.stats.items.attributes.stats.DurabilityStat;

public class ItemDurabilityListener extends IListener<QuantumRPG> {

	private final DurabilityStat duraStat;
	
	public ItemDurabilityListener(@NotNull QuantumRPG plugin, @NotNull DurabilityStat duraStat) {
		super(plugin);
		this.duraStat = duraStat;
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onDuraItemDamage(PlayerItemDamageEvent e) {
		ItemStack item = e.getItem();
		if (ItemStats.hasStat(item, AbstractStat.Type.DURABILITY) || this.duraStat.isUnbreakable(item)) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onDuraBreak(BlockBreakEvent e) {
		Player player = e.getPlayer();
		ItemStack item = player.getInventory().getItemInMainHand();
		if (ItemUT.isAir(item)) return;
		
		this.duraStat.reduceDurability(player, item, 1);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onDuraFish(PlayerFishEvent e) {
		Player player = e.getPlayer();
		ItemStack item = player.getInventory().getItemInMainHand();
		if (ItemUT.isAir(item)) return;
		
		this.duraStat.reduceDurability(player, item, 1);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onDuraDamageWeapon(RPGDamageEvent.Exit e) {
		// Reduce durability of victim's armor
		LivingEntity victim = e.getVictim();
		EntityEquipment equipVictim = victim.getEquipment();
		if (equipVictim != null) {
			ItemStack[] armor = equipVictim.getArmorContents();
		    for (ItemStack armorItem : armor) {
		    	if (ItemUT.isAir(armorItem)) continue;
		    	this.duraStat.reduceDurability(victim, armorItem, 1);
		    }
		    equipVictim.setArmorContents(armor);
		}
		
		// Reduce durability of attacker's weapon
		LivingEntity damager = e.getDamager();
		if (damager == null || e.isProjectile()) return;
		
		EntityEquipment equipDamager = damager.getEquipment();
		if (equipDamager != null) {
			ItemStack weapon = equipDamager.getItemInMainHand();
			this.duraStat.reduceDurability(damager, weapon, 1);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onDurabilityBow(QuantumProjectileLaunchEvent e) {
		ItemStack bow = e.getWeapon();
		if (bow == null) return;
		
		this.duraStat.reduceDurability(e.getShooter(), bow, 1);
		e.setWeapon(bow);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onDuraHoe(PlayerInteractEvent e) {
		Block block = e.getClickedBlock();
		if (block == null) return;
		
		Material bType = block.getType();
		if (bType == Material.GRASS_BLOCK || bType == Material.DIRT || bType == Material.MYCELIUM) {
			Player player = e.getPlayer();
			ItemStack hoe = player.getInventory().getItemInMainHand();
			
			if (!ItemUT.isAir(hoe) && hoe.getType().name().endsWith("_HOE")) {
				this.duraStat.reduceDurability(player, hoe, 1);
			}
		}
	}
}
