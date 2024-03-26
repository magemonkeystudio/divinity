package com.promcteam.divinity.modules.list.party.command;

import com.promcteam.divinity.Perms;
import com.promcteam.divinity.modules.command.MCmd;
import com.promcteam.divinity.modules.list.party.PartyManager;
import com.promcteam.divinity.modules.list.party.PartyManager.PartyMember;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PartyKickCmd extends MCmd<PartyManager> {

    public PartyKickCmd(@NotNull PartyManager m) {
        super(m, new String[]{"kick"}, Perms.PARTY_CMD_KICK);
    }

    @Override
    @NotNull
    public String usage() {
        return plugin.lang().Party_Cmd_Kick_Usage.getMsg();
    }

    @Override
    @NotNull
    public String description() {
        return plugin.lang().Party_Cmd_Kick_Desc.getMsg();
    }

    @Override
    public boolean playersOnly() {
        return true;
    }

    @Override
    @NotNull
    public List<String> getTab(@NotNull Player player, int i, @NotNull String[] args) {
        if (i == 1) {
            List<String> list = new ArrayList<>();
            PartyMember  pm   = module.getPartyMember(player);
            if (pm != null && pm.isLeader()) {
                for (PartyMember mem : pm.getParty().getMembers()) {
                    list.add(mem.getName());
                }
            }
            return list;
        }
        return super.getTab(player, i, args);
    }

    @Override
    public void perform(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        if (args.length != 2) {
            this.printUsage(sender);
            return;
        }

        Player who = plugin.getServer().getPlayer(args[1]);
        module.kickFromParty(player, who);
    }
}
