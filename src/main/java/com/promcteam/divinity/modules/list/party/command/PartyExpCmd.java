package com.promcteam.divinity.modules.list.party.command;

import com.promcteam.divinity.Perms;
import com.promcteam.divinity.modules.command.MCmd;
import com.promcteam.divinity.modules.list.party.PartyManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PartyExpCmd extends MCmd<PartyManager> {

    public PartyExpCmd(@NotNull PartyManager m) {
        super(m, new String[]{"exp"}, Perms.PARTY_CMD_EXP);
    }

    @Override
    @NotNull
    public String usage() {
        return "";
    }

    @Override
    @NotNull
    public String description() {
        return plugin.lang().Party_Cmd_Exp_Desc.getMsg();
    }

    @Override
    public boolean playersOnly() {
        return true;
    }

    @Override
    public void perform(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        module.togglePartyExp(player);
    }
}
