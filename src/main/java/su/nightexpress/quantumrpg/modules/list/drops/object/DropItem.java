package su.nightexpress.quantumrpg.modules.list.drops.object;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.utils.actions.ActionManipulator;
import su.nexmedia.engine.utils.eval.Evaluator;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.quantumrpg.config.EngineCfg;
import su.nightexpress.quantumrpg.modules.api.QModuleDrop;

public class DropItem implements DropCalculator {
	
	private float chance;
	private QModuleDrop<?> module;
	private String itemId;
	private int amountMin;
	private int amountMax;
	private String levelMin;
	private String levelMax;
	private List<String> dropCondidions;
	private ActionManipulator dropActions;
	
	protected boolean noModifier = false;
	
	public DropItem(
			float chance,
			@NotNull QModuleDrop<?> module,
			@NotNull String itemId, 
			int amountMin, 
			int amountMax, 
			@NotNull String levelMin,
			@NotNull String levelMax,
			@NotNull List<String> dropConditions,
			@NotNull ActionManipulator dropActions
			) {
		this.chance = chance;
		this.module = module;
		this.itemId = itemId.toLowerCase();
		this.amountMin = amountMin;
		this.amountMax = amountMax;
		this.levelMin = levelMin;
		this.levelMax = levelMax;
		this.dropCondidions = dropConditions;
		this.dropActions = dropActions;
		//this.noReduce = noReduce; TODO Add Config Option
	}
	
	@NotNull
	public QModuleDrop<?> getModuleId() {
		return this.module;
	}
	
	@NotNull
	public String getItemId() {
		return itemId;
	}
	
    public int getMinAmount() {
        return amountMin;
    }
	
    public int getMaxAmount() {
        return amountMax;
    }
	
    public float getChance() {
        return chance;
    }
	
	public boolean isNoModifier() {
		return noModifier;
	}
	
	public int getLvlMin(@NotNull Player player, @NotNull LivingEntity src) {
		double p_lvl = EngineCfg.HOOK_PLAYER_LEVEL_PLUGIN.getLevel(player);
		double e_lvl = EngineCfg.HOOK_MOB_LEVEL_PLUGIN.getMobLevel(src);
		
		String ex = this.levelMin.replace("%mob_lvl%", String.valueOf(e_lvl)).replace("%player_lvl%", String.valueOf(p_lvl));
		return (int) Evaluator.eval(ex, 1);
	}
	
	public int getLvlMax(@NotNull Player player, @NotNull LivingEntity src) {
		double p_lvl = EngineCfg.HOOK_PLAYER_LEVEL_PLUGIN.getLevel(player);
		double e_lvl = EngineCfg.HOOK_MOB_LEVEL_PLUGIN.getMobLevel(src);
		
		String ex = this.levelMax.replace("%mob_lvl%", String.valueOf(e_lvl)).replace("%player_lvl%", String.valueOf(p_lvl));
		return (int) Evaluator.eval(ex, 1);
	}
	
	public int getLevel(@NotNull Player player, @NotNull LivingEntity src) {
		int min = this.getLvlMin(player, src);
		int max = this.getLvlMax(player, src);
		if (min <= 0) min = 1;
		if (max <= 0) return -1;
		
		return Rnd.get(min, max);
	}
	
	@NotNull
	public List<String> getConditions() {
		return this.dropCondidions;
	}
	
	public void executeActions(@NotNull Player player, @NotNull Map<String, Set<Entity>> targetMap) {
		this.dropActions.process(player, targetMap);
	}
	
	@Override
	public int dropCalculator(
			@NotNull Player killer, 
			@NotNull LivingEntity npc, 
			@NotNull Set<Drop> result, 
			int index, 
			float dropModifier) {

		float percent = this.noModifier ? this.chance : this.chance * dropModifier;
		if (Rnd.get(true) < percent) {
			Drop dropitem = new Drop(this);
			dropitem.calculateCount();
			dropitem.setIndex(index++);
			result.add(dropitem);
		}
		return index;
	}
	
	@Override
    public String toString() {
        return "Drop [itemId=" + itemId + ", minAmount=" + amountMin + ", maxAmount=" + amountMax + ", chance=" + chance + ", noReduce=" + noModifier + "]";
    }
}