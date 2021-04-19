package su.nightexpress.quantumrpg.api;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.modules.list.party.PartyManager;
import su.nightexpress.quantumrpg.modules.list.party.PartyManager.Party;
import su.nightexpress.quantumrpg.modules.list.party.PartyManager.PartyMember;

public class PartyAPI {

	private static QuantumRPG plugin = QuantumRPG.getInstance();
	
	@Nullable
	public static Party getPlayerParty(@NotNull Player player) {
		PartyManager party = plugin.getModuleCache().getPartyManager();
		if (party == null || !party.isLoaded()) return null;
		
		return party.getPlayerParty(player);
	}
	
	@Nullable
	public static PartyMember getPartyMember(@NotNull Player player) {
		PartyManager party = plugin.getModuleCache().getPartyManager();
		if (party == null || !party.isLoaded()) return null;
		
		return party.getPartyMember(player);
	}
	
	@Nullable
	public static PartyManager getPartyManager() {
		PartyManager party = plugin.getModuleCache().getPartyManager();
		if (party == null || !party.isLoaded()) return null;
		
		return party;
	}
}
