/*
package com.promcteam.divinity.hooks.external;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;
import org.skills.api.SkillsAPI;
import org.skills.api.events.SkillEnergyChangeEvent;
import org.skills.types.Skill;

import su.nexmedia.engine.hooks.HookState;
import su.nexmedia.engine.hooks.NHook;
import su.nexmedia.engine.util.StringUT;
import com.promcteam.divinity.Divinity;
import com.promcteam.divinity.hooks.HookClass;
import com.promcteam.divinity.hooks.HookLevel;
import com.promcteam.divinity.stats.EntityStats;
import com.promcteam.divinity.stats.items.attributes.api.AbstractStat;

public class SkillsProHK extends NHook<Divinity> implements HookLevel, HookClass {
	
	public SkillsProHK(@NotNull Divinity plugin) {
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
