package su.nightexpress.quantumrpg.hooks.external;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.event.PlayerCastSkillEvent;
import com.sucy.skill.api.event.PlayerManaGainEvent;
import com.sucy.skill.api.player.PlayerData;

import mc.promcteam.engine.hooks.HookState;
import mc.promcteam.engine.hooks.NHook;
import mc.promcteam.engine.utils.StringUT;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.config.EngineCfg;
import su.nightexpress.quantumrpg.hooks.HookClass;
import su.nightexpress.quantumrpg.hooks.HookLevel;
import su.nightexpress.quantumrpg.stats.EntityStats;
import su.nightexpress.quantumrpg.stats.items.ItemStats;
import su.nightexpress.quantumrpg.stats.items.attributes.api.AbstractStat;
import su.nightexpress.quantumrpg.stats.items.attributes.stats.DurabilityStat;

public class SkillAPIHK extends NHook<QuantumRPG> implements HookLevel, HookClass {
	
	public SkillAPIHK(@NotNull QuantumRPG plugin) {
		super(plugin);
	}
	
	@Override @NotNull
	public HookState setup() {
		this.registerListeners();
		return  HookState.SUCCESS;
	}
	
	@Override
	public void shutdown() {
		this.unregisterListeners();
	}
	
	@Override
	public int getLevel(Player player) {
		PlayerData playerData = SkillAPI.getPlayerData(player);
		return playerData.hasClass() ? playerData.getMainClass().getLevel() : 0;
	}
	
	@Override @NotNull
	public String getClass(Player player) {
		PlayerData data = SkillAPI.getPlayerData(player);
		if (data.hasClass()) {
			return StringUT.colorOff(data.getMainClass().getData().getName());
		}
		else {
			return "";
		}
	}
	
	@EventHandler
	public void onSkillCast(PlayerCastSkillEvent e) {
		if (!EngineCfg.ATTRIBUTES_DURABILITY_REDUCE_FOR_SKILL_API) return;
		Player p = (Player) e.getPlayer();
		ItemStack item = p.getInventory().getItemInMainHand();
		
		DurabilityStat duraStat = ItemStats.getStat(DurabilityStat.class);
		if (duraStat != null) {
			duraStat.reduceDurability(p, item, 1);
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onRegen(PlayerManaGainEvent e) {
		if (e.getPlayerData() == null) return;
		Player player = e.getPlayerData().getPlayer();
		if (player == null) return;
		
		double regen = 1D + EntityStats.get(player).getItemStat(AbstractStat.Type.MANA_REGEN, false) / 100D;
		if (regen > 0) {
			e.setAmount(e.getAmount() * regen);
		}
	}

	@Override
	public void takeMana(@NotNull Player player, double amount, boolean ofMax) {
		PlayerData data = SkillAPI.getPlayerData(player);
		if (data == null) return;
		
		double cur = data.getMana();
		
		if (ofMax) {
			double max = data.getMaxMana();
			amount = max / 100D * amount;
		}
		
		data.setMana(Math.max(0, cur - amount));
	}
}
