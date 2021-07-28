package su.nightexpress.quantumrpg.modules.buffs.cmds;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.cmds.ICmd;
import su.nightexpress.quantumrpg.config.Lang;
import su.nightexpress.quantumrpg.modules.QModule;
import su.nightexpress.quantumrpg.modules.buffs.BuffManager;
import su.nightexpress.quantumrpg.utils.Utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class BuffsAddCmd extends ICmd {
    private final BuffManager m;

    public BuffsAddCmd(QuantumRPG plugin, QModule m) {
        this.m = (BuffManager) m;
    }

    public String getLabel() {
        return "add";
    }

    public String getUsage() {
        return "<player> <bufftype> <name> <amount> <duration>";
    }

    public List<String> getTab(int i) {
        if (i == 2)
            return null;
        if (i == 3)
            return Utils.getEnumsList(BuffManager.BuffType.class);
        if (i == 4)
            return Arrays.asList("<name>");
        if (i == 5)
            return Arrays.asList("<amount>");
        if (i == 6)
            return Arrays.asList("<duration in sec>");
        return Collections.emptyList();
    }

    public String getDesc() {
        return "Adds the buff to a player.";
    }

    public String getPermission() {
        return "qrpg.buffs.cmd.add";
    }

    public boolean playersOnly() {
        return true;
    }

    public void perform(CommandSender sender, String label, String[] args) {
        if (args.length != 6) {
            printUsage(sender, label);
            return;
        }
        Player p = Bukkit.getPlayer(args[1]);
        if (p == null) {
            sender.sendMessage(Lang.Prefix.toMsg() + Lang.Other_InvalidPlayer.toMsg());
            return;
        }
        BuffManager.BuffType type = null;
        try {
            type = BuffManager.BuffType.valueOf(args[2].toUpperCase());
        } catch (IllegalArgumentException ex) {
            sender.sendMessage(Lang.Prefix.toMsg() + Lang.Other_InvalidType.toMsg().replace("%types%", Utils.getEnums(BuffManager.BuffType.class, "§a", "§7")));
            return;
        }
        String value = args[3];
        double mod = 0.0D;
        try {
            mod = Double.parseDouble(args[4]);
        } catch (NumberFormatException ex) {
            sender.sendMessage(Lang.Prefix.toMsg() + Lang.Other_InvalidNumber.toMsg().replace("%s", args[4]));
            return;
        }
        int sec = 0;
        try {
            sec = Integer.parseInt(args[5]);
        } catch (NumberFormatException ex) {
            sender.sendMessage(Lang.Prefix.toMsg() + Lang.Other_InvalidNumber.toMsg().replace("%s", args[5]));
            return;
        }
        if (this.m.addBuff(p, type, value, mod, sec, true)) {
            sender.sendMessage(Lang.Prefix.toMsg() + Lang.Buffs_Give.toMsg()
                    .replace("%value%", args[3])
                    .replace("%mod%", args[4])
                    .replace("%time%", args[5])
                    .replace("%type%", args[2])
                    .replace("%p", args[1]));
        } else {
            sender.sendMessage(Lang.Prefix.toMsg() + Lang.Buffs_Invalid.toMsg());
        }
    }
}
