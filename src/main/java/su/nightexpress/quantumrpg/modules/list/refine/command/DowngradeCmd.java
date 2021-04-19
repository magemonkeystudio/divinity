package su.nightexpress.quantumrpg.modules.list.refine.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.utils.ItemUT;
import su.nightexpress.quantumrpg.Perms;
import su.nightexpress.quantumrpg.modules.command.MCmd;
import su.nightexpress.quantumrpg.modules.list.refine.RefineManager;

public class DowngradeCmd extends MCmd<RefineManager> {

	public DowngradeCmd(@NotNull RefineManager refineManager) {
		super(refineManager, new String[] {"downgrade"}, Perms.REFINE_CMD_DOWNGRADE);
	}

	@Override
	@NotNull
	public String usage() {
		return "";
	}

	@Override
	@NotNull
	public String description() {
		return plugin.lang().Refine_Cmd_Downgrade_Desc.getMsg();
	}

	@Override
	public boolean playersOnly() {
		return true;
	}
	
	@Override
	public void perform(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
		Player player = (Player) sender;
		ItemStack item = player.getInventory().getItemInMainHand();
		if (ItemUT.isAir(item)) {
			this.errItem(sender);
			return;
		}
		
		module.downgradeItem(item, true);
		player.getInventory().setItemInMainHand(item);
		plugin.lang().Refine_Cmd_Downgrade_Done.send(sender);
	}
}
