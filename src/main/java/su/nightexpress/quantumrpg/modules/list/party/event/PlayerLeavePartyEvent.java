package su.nightexpress.quantumrpg.modules.list.party.event;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import mc.promcteam.engine.manager.api.event.IEvent;
import su.nightexpress.quantumrpg.modules.list.party.PartyManager.PartyMember;

public class PlayerLeavePartyEvent extends IEvent {

	private Player player;
	private PartyMember member;

	public PlayerLeavePartyEvent(
			@Nullable Player player,
			@NotNull PartyMember member
			) {
	    this.player = player;
	    this.member = member;
	}
	
	@Nullable
	public Player getPlayer() {
		return this.player;
	}
	
	@NotNull
	public PartyMember getPartyMember() {
		return this.member;
	}
}
