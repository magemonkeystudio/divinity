package com.promcteam.divinity.modules.list.party.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import com.promcteam.divinity.Perms;
import com.promcteam.divinity.modules.command.MCmd;
import com.promcteam.divinity.modules.list.party.PartyManager;

public class PartyDropCmd extends MCmd<PartyManager> {

    public PartyDropCmd(@NotNull PartyManager m) {
        super(m, new String[]{"drop"}, Perms.PARTY_CMD_DROP);
    }

    @Override
    @NotNull
    public String usage() {
        return "";
    }

    @Override
    @NotNull
    public String description() {
        return plugin.lang().Party_Cmd_Drop_Desc.getMsg();
    }

    @Override
    public boolean playersOnly() {
        return true;
    }

    @Override
    public void perform(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        module.togglePartyDrop(player);
    }
}
