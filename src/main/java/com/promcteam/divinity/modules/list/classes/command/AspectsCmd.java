package com.promcteam.divinity.modules.list.classes.command;

import com.promcteam.divinity.Perms;
import com.promcteam.divinity.modules.command.MCmd;
import com.promcteam.divinity.modules.list.classes.ClassManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AspectsCmd extends MCmd<ClassManager> {

    public AspectsCmd(@NotNull ClassManager module) {
        super(module, new String[]{"aspects"}, Perms.CLASS_CMD_ASPECTS);
    }

    @Override
    @NotNull
    public String description() {
        return plugin.lang().Classes_Cmd_Aspects_Desc.getMsg();
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
        module.getAspectManager().openGUI(player);
    }
}
