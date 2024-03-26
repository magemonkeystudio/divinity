package com.promcteam.divinity.modules.list.fortify.command;

import com.promcteam.codex.util.ItemUT;
import com.promcteam.divinity.Perms;
import com.promcteam.divinity.modules.command.MCmd;
import com.promcteam.divinity.modules.list.fortify.FortifyManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class UnfortifyCmd extends MCmd<FortifyManager> {

    public UnfortifyCmd(@NotNull FortifyManager module) {
        super(module, new String[]{"unfortify"}, Perms.FORTIFY_CMD_UNFORTIFY);
    }

    @Override
    @NotNull
    public String usage() {
        return "";
    }

    @Override
    @NotNull
    public String description() {
        return plugin.lang().Fortify_Cmd_Unfortify_Desc.getMsg();
    }

    @Override
    public boolean playersOnly() {
        return true;
    }

    @Override
    public void perform(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        Player    p    = (Player) sender;
        ItemStack item = p.getInventory().getItemInMainHand();
        if (ItemUT.isAir(item)) {
            plugin.lang().Error_NoItem.send(p);
            return;
        }

        module.unfortifyItem(item);
        p.getInventory().setItemInMainHand(item);

        plugin.lang().Command_Modify_Done.send(p);
    }

}
