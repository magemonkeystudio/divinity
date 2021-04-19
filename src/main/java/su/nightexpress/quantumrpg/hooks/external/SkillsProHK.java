/*
package su.nightexpress.quantumrpg.hooks.external;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;
import org.skills.api.SkillsAPI;
import org.skills.api.events.SkillEnergyChangeEvent;
import org.skills.types.Skill;

import su.nexmedia.engine.hooks.HookState;
import su.nexmedia.engine.hooks.NHook;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.hooks.HookClass;
import su.nightexpress.quantumrpg.hooks.HookLevel;
import su.nightexpress.quantumrpg.stats.EntityStats;
import su.nightexpress.quantumrpg.stats.items.attributes.api.AbstractStat;

public class SkillsProHK extends NHook<QuantumRPG> implements HookLevel, HookClass {
	
	public SkillsProHK(@NotNull QuantumRPG plugin) {
		super(plugin);
	}

	@Override @NotNull
	public HookState setup() {
		this.registerListeners();
		
		return HookState.SUCCESS;
	}
	
	@Override
	public void shutdown() {
		this.unregisterListeners();
	}
	
	@Override
	public int getLevel(@NotNull Player player) {
		return SkillsAPI.getSkilledPlayer(player).getLevel();
	}
	
	@Override @NotNull
	public String getClass(@NotNull Player player) {
		Skill cls = SkillsAPI.getSkilledPlayer(player).getSkill();
		if (cls == null) return "";
		
		return StringUT.colorOff(cls.getDisplayName());
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onManaRegen(SkillEnergyChangeEvent e) {
		Player li = e.getPlayer();
		double regen = 1D + EntityStats.get(li).getItemStat(AbstractStat.Type.MANA_REGEN, false) / 100D;
		if (regen > 0) {
			e.setAmount(e.getAmount() * regen);
		}
	}
	
	@Override
	public void takeMana(@NotNull Player player, double amount, boolean ofMax) {
		//PlayerInfo data = sk.getPlayerDataManager().getOrLoadPlayerInfo(p);
		//if (data == null) return;
		
		// TODO
	}
}
*/
