package su.nightexpress.quantumrpg.modules.list.party.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.quantumrpg.Perms;
import su.nightexpress.quantumrpg.modules.command.MCmd;
import su.nightexpress.quantumrpg.modules.list.loot.LootHolder.RollTask;
import su.nightexpress.quantumrpg.modules.list.loot.LootManager;
import su.nightexpress.quantumrpg.modules.list.party.PartyManager;

public class PartyRollCmd extends MCmd<PartyManager> {

	public PartyRollCmd(@NotNull PartyManager module) {
		super(module, new String[] {"roll"}, Perms.PARTY_CMD_ROLL);
	}

	@Override
	@NotNull
	public String description() {
		return plugin.lang().Party_Cmd_Roll_Desc.getMsg();
	}

	@Override
	public boolean playersOnly() {
		return true;
	}

	@Override
	@NotNull
	public String usage() {
		return "";
	}

	@Override
	public void perform(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
		LootManager lootManager = plugin.getModuleCache().getLootManager();
		if (lootManager == null) return;
		
		Player player = (Player) sender;
		RollTask roll = lootManager.getPartyRollTask(player);
		if (roll == null) {
			plugin.lang().Party_Cmd_Roll_Error_Nothing.send(player);
			return;
		}
		
		int r = (int) Rnd.get(true);
		roll.addRoll(player, r);
	}

}
