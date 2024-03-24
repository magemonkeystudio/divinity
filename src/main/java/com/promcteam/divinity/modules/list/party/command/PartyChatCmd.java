package com.promcteam.divinity.modules.list.party.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import com.promcteam.divinity.Perms;
import com.promcteam.divinity.modules.command.MCmd;
import com.promcteam.divinity.modules.list.party.PartyManager;

public class PartyChatCmd extends MCmd<PartyManager> {

    public PartyChatCmd(@NotNull PartyManager m) {
        super(m, new String[]{"chat"}, Perms.PARTY_CMD_CHAT);
    }

    @Override
    @NotNull
    public String usage() {
        return "";
    }

    @Override
    @NotNull
    public String description() {
        return plugin.lang().Party_Cmd_Chat_Desc.getMsg();
    }

    @Override
    public boolean playersOnly() {
        return true;
    }

    @Override
    public void perform(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        module.toggleChat(player);
    }
}
