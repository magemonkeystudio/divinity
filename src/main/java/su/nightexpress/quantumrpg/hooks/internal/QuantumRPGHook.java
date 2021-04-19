package su.nightexpress.quantumrpg.hooks.internal;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.data.api.RPGUser;
import su.nightexpress.quantumrpg.data.api.UserProfile;
import su.nightexpress.quantumrpg.hooks.HookClass;
import su.nightexpress.quantumrpg.hooks.HookLevel;
import su.nightexpress.quantumrpg.modules.list.classes.ClassManager;
import su.nightexpress.quantumrpg.modules.list.classes.api.UserClassData;

public class QuantumRPGHook implements HookLevel, HookClass {

	private QuantumRPG plugin;
	
	public QuantumRPGHook(@NotNull QuantumRPG plugin) {
		this.plugin = plugin;
	}
	
	@Override 
	@NotNull
	public String getClass(@NotNull Player player) {
		RPGUser user = plugin.getUserManager().getOrLoadUser(player);
		if (user == null) return "";
		
		UserProfile prof = user.getActiveProfile();
		UserClassData cData = prof.getClassData();
		if (cData == null) return "";
		
		return StringUT.colorOff(cData.getPlayerClass().getName());
	}

	@Override
	public int getLevel(@NotNull Player player) {
		RPGUser user = plugin.getUserManager().getOrLoadUser(player);
		if (user == null) return 0;
		
		UserProfile prof = user.getActiveProfile();
		UserClassData cData = prof.getClassData();
		return cData != null ? cData.getLevel() : 0;
	}
	
	@Override
	public void takeMana(@NotNull Player player, double amount, boolean ofMax) {
		ClassManager classManager = plugin.getModuleCache().getClassManager();
		if (classManager == null) return;
		
		classManager.consumeMana(player, amount, ofMax);
	}

}
