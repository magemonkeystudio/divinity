package su.nightexpress.quantumrpg.modules.list.drops.object;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.LoadableItem;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.modules.list.drops.DropManager;

public class DropMob extends LoadableItem implements DropCalculator {

	protected float chance;
	protected boolean rollOnce;
	
	protected Set<String> entityGood;
	protected Set<String> mythicGood;
	protected Set<String> reasonsBad;
	
	protected List<DropTable> dropTables;
	
	public DropMob(@NotNull QuantumRPG plugin, @NotNull JYML cfg) {
		super(plugin, cfg);
		
		this.chance = (float) cfg.getDouble("chance");
		this.rollOnce = cfg.getBoolean("roll-once");
		
		this.entityGood = new HashSet<>(cfg.getStringList("vanilla-mobs"));
		this.mythicGood = new HashSet<>(cfg.getStringList("mythic-mobs"));
		this.reasonsBad = new HashSet<>(cfg.getStringList("prevent-from"));
		
		this.dropTables = new ArrayList<>();
		DropManager dropManager = plugin.getModuleCache().getDropManager();
		if (dropManager == null) return;
		
		for (String tableId : cfg.getStringList("drop-tables")) {
			DropTable dropTable = dropManager.getTableById(tableId);
			if (dropTable == null) {
				dropManager.error("Invalid drop table " + tableId + " in " + cfg.getFile().getName());
				continue;
			}
			this.dropTables.add(dropTable);
		}
		
		if (this.dropTables.isEmpty()) {
			throw new IllegalStateException("Empty drop tables for " + cfg.getFile().getName());
		}
	}
	
	public boolean isRollOnce() {
		return this.rollOnce;
	}
	
	public float getChance() {
		return this.chance;
	}
	
	@NotNull
	public Set<String> getEntities() {
		return this.entityGood;
	}
	
	@NotNull
	public Set<String> getMythic() {
		return this.mythicGood;
	}
	
	@NotNull
	public Set<String> getReasons() {
		return this.reasonsBad;
	}

	@NotNull
	public List<DropTable> getDropTables() {
		return this.dropTables;
	}

	@Override
	protected void save(@NotNull JYML cfg) {
		
	}

	@Override
	public int dropCalculator(
			@NotNull Player killer, 
			@NotNull LivingEntity npc, 
			@NotNull Set<Drop> result, 
			int index, 
			float dropModifier) {
		
		float percent = this.chance;
		percent *= dropModifier;
		
		if (Rnd.get(true) >= percent) {
			return index;
		}
		
		if (this.rollOnce) {
			DropTable dg = Rnd.get(this.dropTables);
			if (dg != null) {
				return dg.dropCalculator(killer, npc, result, index, dropModifier);
			}
		}
		for (DropTable dg : this.dropTables) {
			index = dg.dropCalculator(killer, npc, result, index, dropModifier);
		}
		return index;
	}
}
