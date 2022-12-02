package su.nightexpress.quantumrpg.modules.list.combatlog.command;

import mc.promcteam.engine.utils.ClickText;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.Perms;
import su.nightexpress.quantumrpg.modules.command.MCmd;
import su.nightexpress.quantumrpg.modules.list.combatlog.CombatLogManager;

public class LogCommand extends MCmd<CombatLogManager> {

    public LogCommand(@NotNull CombatLogManager module) {
        super(module, new String[]{"log"}, Perms.COMBAT_LOG_CMD_LOG);
    }

    @Override
    @NotNull
    public String description() {
        return plugin.lang().CombatLog_Cmd_Log_Desc.getMsg();
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
        for (ClickText text : module.getCombatLog(player)) {
            text.send(player);
        }
    }

}
