package su.nightexpress.quantumrpg.modules.list.party.compat.level;

import java.util.Set;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import mc.promcteam.engine.manager.IListener;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.modules.list.party.PartyManager;
import su.nightexpress.quantumrpg.modules.list.party.PartyManager.Party;
import su.nightexpress.quantumrpg.modules.list.party.PartyManager.PartyExpMode;
import su.nightexpress.quantumrpg.modules.list.party.PartyManager.PartyMember;

public abstract class IPartyLevelManager extends IListener<QuantumRPG> {

	protected PartyManager partyManager;
	
	public IPartyLevelManager(@NotNull QuantumRPG plugin, @NotNull PartyManager partyManager) {
		super(plugin);
		this.partyManager = partyManager;
	}

	public void setup() {
		this.registerListeners();
	}
	
	public void shutdown() {
		this.unregisterListeners();
	}
	
	protected final int getBalancedExp(@NotNull Player player, int amount) {
		PartyMember member = this.partyManager.getPartyMember(player);
		if (member == null || amount <= 0) return 0;
		
		Party party = member.getParty();
		if (party.getExpMode() != PartyExpMode.SHARED) return 0;
		
		int expDistance = this.partyManager.getSettings().getMaxLevelExpBalanceDistance();
		Set<PartyMember> expGeters = party.getMembersByDistance(member, expDistance);
		
		int size = expGeters.size() + 1; // +1 For Executor
		int[] each = splitIntoParts(amount, size);
		
		int i = 0;
		for (PartyMember friend : expGeters) {
			Player pFriend = friend.getPlayer(); 
			if (pFriend == null) continue;
			
			this.giveExp(pFriend, each[i++]);
		}
		
		return each[i];
	}

	private static int[] splitIntoParts(int whole, int parts) {
	    int[] arr = new int[parts];
	    int remain = whole;
	    int partsLeft = parts;
	    for (int i = 0; partsLeft > 0; i++) {
	        int size = (remain + partsLeft - 1) / partsLeft; // rounded up, aka ceiling
	        arr[i] = size;
	        remain -= size;
	        partsLeft--;
	    }
	    return arr;
	}
	
	public abstract void giveExp(@NotNull Player player, int amount);
}
