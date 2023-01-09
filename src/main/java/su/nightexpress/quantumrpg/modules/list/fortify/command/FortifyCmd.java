package su.nightexpress.quantumrpg.modules.list.fortify.command;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import mc.promcteam.engine.utils.ItemUT;
import mc.promcteam.engine.utils.random.Rnd;
import su.nightexpress.quantumrpg.Perms;
import su.nightexpress.quantumrpg.modules.command.MCmd;
import su.nightexpress.quantumrpg.modules.list.fortify.FortifyManager;
import su.nightexpress.quantumrpg.modules.list.fortify.FortifyManager.FortifyItem;

public class FortifyCmd extends MCmd<FortifyManager> {

	public FortifyCmd(@NotNull FortifyManager module) {
		super(module, new String[] {"fortify"}, Perms.FORTIFY_CMD_FORTIFY);
	}

	@Override
	@NotNull
	public String usage() {
		return plugin.lang().Fortify_Cmd_Fortify_Usage.getMsg();
	}

	@Override
	@NotNull
	public String description() {
		return plugin.lang().Fortify_Cmd_Fortify_Desc.getMsg();
	}

	@Override
	public boolean playersOnly() {
		return true;
	}

	@Override
	@NotNull
	public List<String> getTab(@NotNull Player player, int i, @NotNull String[] args) {
		if (i == 1) {
			return module.getItemIds();
		}
		if (i == 2) {
			return Arrays.asList("-1", "1", "2", "3", "4", "5");
		}
		
		return super.getTab(player, i, args);
	}
	
	@Override
	public void perform(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
		if (args.length != 3) {
			this.printUsage(sender);
			return;
		}
		
		Player player = (Player) sender;
		ItemStack item = player.getInventory().getItemInMainHand();
		if (ItemUT.isAir(item)) {
			plugin.lang().Error_NoItem.send(player);
			return;
		}
		
		FortifyItem stone = module.getItemById(args[1]);
		if (stone == null) {
			plugin.lang().Fortify_Cmd_Fortify_Error_Stone.send(player);
			return;
		}
		
		int lvl = this.getNumI(sender, args[2], -1);
		if (lvl < 1) {
			lvl = Rnd.get(stone.getMinLevel(), stone.getMaxLevel());
		}
		
		module.fortifyItem(item, stone, lvl);
		player.getInventory().setItemInMainHand(item);
		
		plugin.lang().Fortify_Fortify_Done.send(sender);
	}
}
