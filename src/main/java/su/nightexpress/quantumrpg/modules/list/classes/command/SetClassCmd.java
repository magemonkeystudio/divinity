package su.nightexpress.quantumrpg.modules.list.classes.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import mc.promcteam.engine.utils.PlayerUT;
import su.nightexpress.quantumrpg.Perms;
import su.nightexpress.quantumrpg.modules.command.MCmd;
import su.nightexpress.quantumrpg.modules.list.classes.ClassManager;
import su.nightexpress.quantumrpg.modules.list.classes.api.RPGClass;

public class SetClassCmd extends MCmd<ClassManager> {

	public SetClassCmd(@NotNull ClassManager module) {
		super(module, new String[] {"setclass"}, Perms.CLASS_CMD_SETCLASS);
	}

	@Override
	@NotNull
	public String description() {
		return plugin.lang().Classes_Cmd_SetClass_Desc.getMsg();
	}

	@Override
	@NotNull
	public String usage() {
		return plugin.lang().Classes_Cmd_SetClass_Usage.getMsg();
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
			return new ArrayList<>(this.module.getClassIds());
		}
		if (i == 3) {
			return Arrays.asList("true", "false");
		}
		return super.getTab(player, i, args);
	}
	
	@Override
	public void perform(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
		if (args.length < 3) {
			this.printUsage(sender);
			return;
		}
		
		Player player = plugin.getServer().getPlayer(args[1]);
		if (player == null) {
			this.errPlayer(sender);
			return;
		}
		
		RPGClass rpgClass = this.module.getClassById(args[2]);
		if (rpgClass == null) {
			plugin.lang().Classes_Cmd_SetClass_Error_NoClass.send(sender);
			return;
		}
		
		boolean force = true;
		if (args.length >= 4) {
			force = Boolean.parseBoolean(args[3]);
		}
		
		this.module.setPlayerClass(player, rpgClass, force);
		
		plugin.lang().Classes_Cmd_SetClass_Done
			.replace("%class%", rpgClass.getName())
			.replace("%name%", player.getName())
			.send(sender);
	}

}
