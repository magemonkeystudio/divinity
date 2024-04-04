package studio.magemonkey.divinity.modules.list.classes.command;

import studio.magemonkey.divinity.Perms;
import studio.magemonkey.divinity.modules.command.MCmd;
import studio.magemonkey.divinity.modules.list.classes.ClassManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class StatsCmd extends MCmd<ClassManager> {

    public StatsCmd(@NotNull ClassManager module) {
        super(module, new String[]{"stats"}, Perms.CLASS_CMD_STATS);
    }

    @Override
    @NotNull
    public String description() {
        return plugin.lang().Classes_Cmd_Stats_Desc.getMsg();
    }

    @Override
    @NotNull
    public String usage() {
        return "";
    }

    @Override
    public boolean playersOnly() {
        return true;
    }

    @Override
    public void perform(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        module.openStatsGUI(player);
    }

}
