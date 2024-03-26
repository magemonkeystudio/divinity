package com.promcteam.divinity.modules.list.repair.command;

import com.promcteam.codex.util.PlayerUT;
import com.promcteam.divinity.Perms;
import com.promcteam.divinity.modules.command.MCmd;
import com.promcteam.divinity.modules.list.repair.RepairManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class RepairOpenCmd extends MCmd<RepairManager> {

    public RepairOpenCmd(@NotNull RepairManager m) {
        super(m, new String[]{"open"}, Perms.REPAIR_CMD_OPEN);
    }

    @Override
    @NotNull
    public String usage() {
        return plugin.lang().Repair_Cmd_Open_Desc.getMsg();
    }

    @Override
    @NotNull
    public String description() {
        return plugin.lang().Repair_Cmd_Open_Desc.getMsg();
    }

    @Override
    public boolean playersOnly() {
        return false;
    }

    @Override
    @NotNull
    public List<String> getTab(@NotNull Player player, int i, @NotNull String[] args) {
        if (i == 1) {
            return PlayerUT.getPlayerNames();
        }
        if (i == 2) {
            return Arrays.asList("true", "false");
        }
        return super.getTab(player, i, args);
    }

    @Override
    public void perform(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (args.length < 2 && !(sender instanceof Player)) {
            this.errSender(sender);
            return;
        }

        String pName = sender.getName();
        if (args.length >= 2) {
            pName = args[1];
        }

        Player player = plugin.getServer().getPlayer(pName);
        if (player == null) {
            this.errPlayer(sender);
            return;
        }

        boolean force = args.length >= 3 ? Boolean.parseBoolean(args[2]) : (args.length < 2 ? true : false);

        ItemStack item = player.getInventory().getItemInMainHand();
        this.module.openAnvilGUI(player, item, null, null, force);

        if (!sender.equals(player)) {
            plugin.lang().Repair_Cmd_Open_Done_Others
                    .replace("%player%", player.getName())
                    .send(sender);
        }
    }
}
