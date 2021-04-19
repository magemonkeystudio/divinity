package su.nightexpress.quantumrpg.modules.list.drops.object;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.hooks.external.WorldGuardHK;
import su.nexmedia.engine.manager.LoadableItem;
import su.nexmedia.engine.modules.IModule;
import su.nexmedia.engine.utils.StringUT;
import su.nexmedia.engine.utils.actions.ActionManipulator;
import su.nexmedia.engine.utils.constants.JStrings;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.config.EngineCfg;
import su.nightexpress.quantumrpg.modules.api.QModuleDrop;

public class DropTable extends LoadableItem implements DropCalculator {

	protected String name;
	protected boolean rollOnce = true;
	private boolean penaltyLevelEnabled;
	private int penaltyLevelVariance;
	protected Set<String> worldsGood;
	protected Set<String> biomesGood;
	protected Set<String> regionsBad;
	protected List<DropItem> dropList;
	
	public DropTable(@NotNull QuantumRPG plugin, @NotNull JYML cfg) {
		super(plugin, cfg);
		
		this.name = StringUT.color(cfg.getString("name", this.getId()));
		this.rollOnce = cfg.getBoolean("roll-once");
		
		this.penaltyLevelEnabled = cfg.getBoolean("level-penalty.enabled");
		this.penaltyLevelVariance = cfg.getInt("level-penalty.variance");
		
		this.worldsGood = new HashSet<>(cfg.getStringList("world-whitelist"));
		this.biomesGood = new HashSet<>(cfg.getStringList("biome-whitelist"));
		this.regionsBad = new HashSet<>(cfg.getStringList("region-blacklist"));
		
		this.dropList = new ArrayList<>();
		for (String id : cfg.getSection("items")) {
			String path = "items." + id + ".";
			
			float itemChance = (float) cfg.getDouble(path + "chance");
			String moduleId = cfg.getString(path + "module-id", "");
			
			IModule<?> mod = plugin.getModuleManager().getModule(moduleId);
			if (mod == null || !mod.isLoaded() || !(mod instanceof QModuleDrop<?>)) {
				plugin.error("Invalid module for item " + id + " in drop table " + cfg.getFile().getName());
				continue;
			}
			
			String itemId = cfg.getString(path + "item-id");
			if (itemId == null) {
				plugin.error("Invalid id for item " + id + " in drop table " + cfg.getFile().getName());
				continue;
			}
			
			QModuleDrop<?> itemModule = (QModuleDrop<?>) mod;
			int amountMin = cfg.getInt(path + "min-amount", 1);
			int amountMax = cfg.getInt(path + "max-amount", 1);
			
			String levelMin = cfg.getString(path + "min-level", "-1");
			String levelMax = cfg.getString(path + "max-level", "-1");
			List<String> dropConditions = cfg.getStringList(path + "additional-conditions");
			ActionManipulator dropActions = new ActionManipulator(plugin, cfg, path + "actions-on-drop");
			
			DropItem di = new DropItem(
					itemChance,
					itemModule,
					itemId, 
					amountMin, 
					amountMax, 
					levelMin,
					levelMax,
					dropConditions,
					dropActions
					);
			this.dropList.add(di);
		}
	}
	
	@Override
	protected void save(@NotNull JYML cfg) {
		
	}

	@NotNull
	public String getGroupName() {
		return name;
	}
	
    public boolean isRollOnce() {
        return rollOnce;
    }
    
    public boolean isLevelPenalty() {
		return this.penaltyLevelEnabled && this.penaltyLevelVariance > 0;
	}
	
	public int getPenaltyVariance() {
		return this.penaltyLevelVariance;
	}
	
	@NotNull
    public Set<String> getAllowedWorlds() {
    	return this.worldsGood;
    }
    
	@NotNull
    public Set<String> getAllowedBiomes() {
    	return this.biomesGood;
    }
    
	@NotNull
    public Set<String> getDisallowedRegions() {
    	return this.regionsBad;
    }
    
	@NotNull
	public List<DropItem> getDrop() {
		return this.dropList;
	}

	/**
	 * @param npc Instance of a valid entity
	 * @return True if drop is possible at this location.
	 */
	protected boolean checkForLocation(@NotNull LivingEntity npc) {
		String world = npc.getWorld().getName();
		if (!this.worldsGood.contains(JStrings.MASK_ANY) && 
				!this.worldsGood.contains(world)) return false;
		
		
		String biome = npc.getLocation().getBlock().getBiome().name();
		if (!this.biomesGood.contains(JStrings.MASK_ANY) && 
				!this.biomesGood.contains(biome)) return false;
		
		
		WorldGuardHK wg = plugin.getWorldGuard();
		if (wg != null) {
			String region = wg.getRegion(npc);
			if (this.regionsBad.contains(JStrings.MASK_ANY) || 
					this.regionsBad.contains(region)) return false;
		}
		
		return true;
	}
	
	protected boolean canDrop(@NotNull LivingEntity npc) {
		if (!this.checkForLocation(npc)) return false;
		
		if (this.isLevelPenalty()) {
			Player p = npc.getKiller();
			if (p != null) {
				int maxDiff = this.getPenaltyVariance();
				double lvlMob = EngineCfg.HOOK_MOB_LEVEL_PLUGIN.getMobLevel(npc);
				double lvlPlayer = EngineCfg.HOOK_PLAYER_LEVEL_PLUGIN.getLevel(p);
				if (lvlPlayer > lvlMob && (lvlPlayer - lvlMob) >= maxDiff) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public int dropCalculator(
			@NotNull Player killer, 
			@NotNull LivingEntity npc,
			@NotNull Set<Drop> result, 
			int index, 
			float dropModifier) {
		
		if (this.dropList.isEmpty() || !this.canDrop(npc)) {
			return index;
		}
		
		if (this.rollOnce) {
			DropItem dropItem = Rnd.get(this.dropList);
			if (dropItem != null) {
				return dropItem.dropCalculator(killer, npc, result, index, dropModifier);
			}
		}
		for (DropItem dropItem : this.dropList) {
			index = dropItem.dropCalculator(killer, npc, result, index, dropModifier);
		}

		return index;
	}
}
