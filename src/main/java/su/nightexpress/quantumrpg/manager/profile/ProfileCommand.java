package su.nightexpress.quantumrpg.manager.profile;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import mc.promcteam.engine.commands.api.IGeneralCommand;
import su.nightexpress.quantumrpg.QuantumRPG;

public class ProfileCommand extends IGeneralCommand<QuantumRPG> {

	private ProfileManager profileManager;
	
	public ProfileCommand(@NotNull ProfileManager profileManager) {
		super(profileManager.plugin, new String[] {"profile", "profiles"}, null);
		this.profileManager = profileManager;
	}

	@Override
	@NotNull
	public String usage() {
		return "";
	}

	@Override
	@NotNull
	public String description() {
		return plugin.lang().Profiles_Command_Profiles_Desc.getMsg();
	}

	@Override
	public boolean playersOnly() {
		return true;
	}

	@Override
	protected void perform(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
		Player player = (Player) sender;
		this.profileManager.getProfileGUI().open(player, 1);
	}
}
