package com.promcteam.divinity.modules.list.combatlog.command;

import com.promcteam.codex.utils.ClickText;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import com.promcteam.divinity.Perms;
import com.promcteam.divinity.modules.command.MCmd;
import com.promcteam.divinity.modules.list.combatlog.CombatLogManager;

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
