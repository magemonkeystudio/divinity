package su.nightexpress.quantumrpg.modules.list.dismantle.command;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import mc.promcteam.engine.utils.PlayerUT;
import su.nightexpress.quantumrpg.Perms;
import su.nightexpress.quantumrpg.modules.command.MCmd;
import su.nightexpress.quantumrpg.modules.list.dismantle.DismantleManager;

public class DismantleOpenCmd extends MCmd<DismantleManager> {
	
	public DismantleOpenCmd(@NotNull DismantleManager m) {
		super(m, new String[] {"open"}, Perms.DISMANTLE_CMD_OPEN);
	}

	@Override
	@NotNull
	public String usage() {
		return plugin.lang().Dismantle_Cmd_Open_Usage.getMsg();
	}

	@Override
	@NotNull
	public String description() {
		return plugin.lang().Dismantle_Cmd_Open_Desc.getMsg();
	}

	@Override
	public boolean playersOnly() {
		return false;
	}
	
	@Override
	@NotNull
	public List<String> getTab(@NotNull Player player, int i, @NotNull String[] args) {
		if (i == 1) {
			return PlayerUT.getPlayerNames();
		}
		if (i == 2) {
			return Arrays.asList("true", "false");
		}
		return super.getTab(player, i, args);
	}

	@Override
	public void perform(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
		if (args.length < 2 && !(sender instanceof Player)) {
			this.errSender(sender);
			return;
		}
		
		String pName = sender.getName();
		if (args.length >= 2) {
			pName = args[1];
		}
		
		Player player = plugin.getServer().getPlayer(pName);
		if (player == null) {
			this.errPlayer(sender);
			return;
		}
		
		boolean force = args.length >= 3 ? Boolean.parseBoolean(args[2]) : (args.length < 2 ? true : false);

		this.module.openDismantleGUI(player, force);
		
		if (!sender.equals(player)) {
			plugin.lang().Dismantle_Cmd_Open_Done_Others
				.replace("%player%", player.getName())
				.send(sender);
		}
	}
}
