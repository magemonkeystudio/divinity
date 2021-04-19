package su.nightexpress.quantumrpg.modules.list.party.compat.level;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;

import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.api.QuantumAPI;
import su.nightexpress.quantumrpg.modules.list.classes.ClassManager;
import su.nightexpress.quantumrpg.modules.list.classes.event.PlayerClassExpGainEvent;
import su.nightexpress.quantumrpg.modules.list.classes.object.ExpSource;
import su.nightexpress.quantumrpg.modules.list.party.PartyManager;
import su.nightexpress.quantumrpg.modules.list.party.PartyManager.Party;

public class IPEInternal extends IPartyLevelManager {

	public IPEInternal(@NotNull QuantumRPG plugin, @NotNull PartyManager partyManager) {
		super(plugin, partyManager);
	}

	@EventHandler
	public void onExpGain(PlayerClassExpGainEvent e) {
		if (e.getExpSource() != ExpSource.MOB_KILL) return;
		
		Player player = e.getPlayer();
		int exp = this.getBalancedExp(player, e.getExp());
		if (exp <= 0) return;
		
		e.setExp(exp);
	}
	
	@Override
	public void giveExp(@NotNull Player player, int amount) {
		Party party = this.partyManager.getPlayerParty(player);
		if (party == null) return;
		
		ClassManager classManager = QuantumAPI.getModuleManager().getClassManager();
		if (classManager == null) return;
		
		classManager.getLevelingManager().addExp(player, amount, party.getId(), ExpSource.NONE);
	}

}
