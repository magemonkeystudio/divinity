package su.nightexpress.quantumrpg.modules.list.party.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import su.nightexpress.quantumrpg.Perms;
import su.nightexpress.quantumrpg.modules.command.MCmd;
import su.nightexpress.quantumrpg.modules.list.party.PartyManager;

public class PartyMenuCmd extends MCmd<PartyManager> {

	public PartyMenuCmd(@NotNull PartyManager m) {
		super(m, new String[] {"menu"}, Perms.PARTY_CMD_MENU);
	}

	@Override
	@NotNull
	public String usage() {
		return "";
	}

	@Override
	@NotNull
	public String description() {
		return plugin.lang().Party_Cmd_Menu_Desc.getMsg();
	}

	@Override
	public boolean playersOnly() {
		return true;
	}
	
	@Override
	public void perform(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
		Player player = (Player) sender;
		module.openPartyGUI(player);
	}
}
