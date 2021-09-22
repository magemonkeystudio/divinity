package su.nightexpress.quantumrpg.modules.list.arrows;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import mc.promcteam.engine.config.api.JYML;
import mc.promcteam.engine.manager.api.task.ITask;
import mc.promcteam.engine.utils.ItemUT;
import mc.promcteam.engine.utils.StringUT;
import mc.promcteam.engine.utils.actions.ActionManipulator;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.api.event.QuantumProjectileLaunchEvent;
import su.nightexpress.quantumrpg.modules.EModule;
import su.nightexpress.quantumrpg.modules.LeveledItem;
import su.nightexpress.quantumrpg.modules.api.QModuleDrop;
import su.nightexpress.quantumrpg.modules.list.arrows.ArrowManager.QArrow;
import su.nightexpress.quantumrpg.stats.bonus.BonusMap;
import su.nightexpress.quantumrpg.stats.items.ItemStats;
import su.nightexpress.quantumrpg.stats.items.attributes.DefenseAttribute;

public class ArrowManager extends QModuleDrop<QArrow> {

	private static final String META_ARROW_ID = "QRPG_ARROW_ID";
	private static final String META_ARROW_LEVEL = "QRPG_ARROW_LEVEL";
	private Set<Map.Entry<Projectile, QArrow>> flyList;
	private ArrowTask taskFly;
	private boolean generalAllowInfinity;
	
	public ArrowManager(@NotNull QuantumRPG plugin) {
		super(plugin, QArrow.class);
	}
	
	@Override
	@NotNull
	public String getId() {
		return EModule.ARROWS;
	}

	@Override
	@NotNull
	public String version() {
		return "2.0.0";
	}
	
	@Override
	public void setup() {
		this.flyList = new HashSet<>();
		
		this.cfg.addMissing("settings.allow-infinity-enchant", true);
		this.cfg.saveChanges();
		
		this.generalAllowInfinity = this.cfg.getBoolean("settings.allow-infinity-enchant");
		
		this.taskFly = new ArrowTask(plugin);
		this.taskFly.start();
	}

	@Override
	public void shutdown() {
		if (this.taskFly != null) {
			this.taskFly.stop();
			this.taskFly = null;
		}
		if (this.flyList != null) {
			this.flyList.clear();
			this.flyList = null;
		}
	}
    
    // ---------------------------------------------------------------
	
    @Nullable
    public QArrow getArrow(@NotNull Projectile pj) {
    	if (pj.hasMetadata(META_ARROW_ID)) {
    		String id = pj.getMetadata(META_ARROW_ID).get(0).asString();
    		return this.getItemById(id);
    	}
    	return null;
    }
	
    public int getArrowLevel(@NotNull Projectile pj) {
    	if (!pj.hasMetadata(META_ARROW_LEVEL)) return 0;
    	
    	return pj.getMetadata(META_ARROW_LEVEL).get(0).asInt();
    }
    
    /**
     * @param p Player instance
     * @return First ItemStack with ARROW type in player's inventory.
     */
    @Nullable
	public final ItemStack getFirstArrow(@NotNull Player p, boolean checkAll) {
		ItemStack off = p.getInventory().getItemInOffHand();
		if (!ItemUT.isAir(off) && this.isItemOfThisModule(off)) {
			return off;
		}
		
		ItemStack main = p.getInventory().getItemInMainHand();
		if (!ItemUT.isAir(main) && this.isItemOfThisModule(main)) {
			return main;
		}
		
		if (checkAll) {
			int i = p.getInventory().first(Material.ARROW);
			if (i >= 0) {
				return p.getInventory().getItem(i);
			}
		}
		return null;
	}
	
	// -------------------------------------------------------------------- //
	// EVENTS
	
    // TODO Allow mobs shoot custom arrows
    
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onQuantumArrowLaunch(QuantumProjectileLaunchEvent e) {
		if (!(e.getProjectile() instanceof Projectile)) return;
		if (!(e.getShooter() instanceof Player)) return;
		
		Player p = (Player) e.getShooter();
		ItemStack arrow = this.getFirstArrow(p, e.isBowEvent());
		if (arrow == null) return;
		
		// Check if ItemStack is valid QArrow
		QArrow qarrow = this.getModuleItem(arrow);
		if (qarrow == null) return;
		
		ItemStack bow = e.getWeapon();
		if (e.isBowEvent() && !this.generalAllowInfinity && bow != null) {
			if (bow.containsEnchantment(Enchantment.ARROW_INFINITE)) {
				arrow.setAmount(arrow.getAmount() - 1);
			}
		}
		
		String id = qarrow.getId();
		Projectile pj = (Projectile) e.getProjectile();
		int level = ItemStats.getLevel(arrow);
		
		this.markArrow(pj, id, level);
		this.flyList.add(new AbstractMap.SimpleEntry<>(pj, qarrow));
	}
	
	public void markArrow(@NotNull Projectile pj, @NotNull String id, int lvl) {
		pj.setMetadata(META_ARROW_ID, new FixedMetadataValue(plugin, id));
		pj.setMetadata(META_ARROW_LEVEL, new FixedMetadataValue(plugin, lvl));
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onQuantumArrowHit(ProjectileHitEvent e) {
		Projectile pj = e.getEntity();
		
		QArrow arrow = this.getArrow(pj);
		if (arrow == null) return;
		
		// Fix arrow position for
		// correct target selector
		// from look of arrow
		Entity ee = e.getHitEntity();
		if (ee != null) {
		    Location eLoc = ee.getLocation().clone();
			
			Vector from = eLoc.toVector();
			Vector to = pj.getLocation().toVector();
			
			Vector direction = to.subtract(from);
			direction.normalize();
			
			direction.multiply(2);
			eLoc.setDirection(direction);
			pj.teleport(eLoc);
			pj.setVelocity(direction);
		}
		
		arrow.executeHitActions(pj);
	}
	
	// -------------------------------------------------------------------- //
	// CLASSES
	
	public class QArrow extends LeveledItem {
		
		private TreeMap<Integer, BonusMap> bonusMap;
		private ActionManipulator fly;
		private ActionManipulator hit;
		
		public QArrow(@NotNull QuantumRPG plugin, @NotNull JYML cfg) {
			super(plugin, cfg, ArrowManager.this);
			
			cfg.addMissing("material", "ARROW");
			cfg.saveChanges();
			
			this.bonusMap = new TreeMap<>();
			for (String sLvl : cfg.getSection("bonuses-by-level")) {
				int itemLvl = StringUT.getInteger(sLvl, -1);
				if (itemLvl <= 0) continue;
				
				String path = "bonuses-by-level." + sLvl + ".";
				BonusMap bMap = new BonusMap();
				bMap.loadStats(cfg, path + "additional-stats");
				bMap.loadDamages(cfg, path + "additional-damage");
				bMap.loadDefenses(cfg, path + "defense-ignoring");
				
				// Here we adjust the defense bonus function to negative value.
				// So it will reduce the victim's defense on hit.
				bMap.getBonuses().forEach((stat, func) -> {
					if (stat instanceof DefenseAttribute) {
						bMap.getBonuses().compute(stat, (kStat, vFunc) -> vFunc.andThen(result -> -result));
					}
				});
				
				this.bonusMap.put(itemLvl, bMap);
			}
			this.fly = new ActionManipulator(plugin, cfg, "on-fly-actions");
			this.hit = new ActionManipulator(plugin, cfg, "on-hit-actions");
		}
		
		@Nullable
		public BonusMap getBonusMap(int lvl) {
			Map.Entry<Integer, BonusMap> e = this.bonusMap.floorEntry(lvl);
			if (e == null) return null;
			
			return e.getValue();
		}
		
		public void executeFlyActions(@NotNull Projectile arrow) {
			this.fly.process(arrow);
		}
		
		public void executeHitActions(@NotNull Projectile arrow) {
			this.hit.process(arrow);
		}
	}
	
	class ArrowTask extends ITask<QuantumRPG> {

		public ArrowTask(@NotNull QuantumRPG plugin) {
			super(plugin, 1L, false);
		}

		@Override
		public void action() {
			for (Map.Entry<Projectile, QArrow> e : new HashSet<>(ArrowManager.this.flyList)) {
				Projectile pj = e.getKey();
				if (pj.isOnGround() || !pj.isValid()) {
					ArrowManager.this.flyList.remove(e);
					continue;
				}
				QArrow arrow = e.getValue();
				arrow.executeFlyActions(pj);
			}
		}
	}
}
