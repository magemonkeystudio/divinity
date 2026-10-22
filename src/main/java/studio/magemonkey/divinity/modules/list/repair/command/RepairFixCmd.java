package studio.magemonkey.divinity.modules.list.repair.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.util.ItemUT;
import studio.magemonkey.codex.util.PlayerUT;
import studio.magemonkey.divinity.Perms;
import studio.magemonkey.divinity.modules.command.MCmd;
import studio.magemonkey.divinity.modules.list.repair.RepairManager;
import studio.magemonkey.divinity.stats.items.ItemStats;
import studio.magemonkey.divinity.stats.items.attributes.api.TypedStat;

import java.util.Arrays;
import java.util.List;

public class RepairFixCmd extends MCmd<RepairManager> {

    private static final String MODE_HAND = "hand";
    private static final String MODE_ALL  = "all";

    public RepairFixCmd(@NotNull RepairManager m) {
        super(m, new String[]{"fix"}, Perms.REPAIR_CMD_FIX);
    }

    @Override
    @NotNull
    public String usage() {
        return plugin.lang().Repair_Cmd_Fix_Usage.getMsg();
    }

    @Override
    @NotNull
    public String description() {
        return plugin.lang().Repair_Cmd_Fix_Desc.getMsg();
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
            return Arrays.asList(MODE_HAND, MODE_ALL);
        }
        return super.getTab(player, i, args);
    }

    @Override
    public void perform(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (args.length < 2 && !(sender instanceof Player)) {
            this.errSender(sender);
            return;
        }

        String pName = args.length >= 2 ? args[1] : sender.getName();
        Player player = plugin.getServer().getPlayer(pName);
        if (player == null) {
            this.errPlayer(sender);
            return;
        }

        String mode = args.length >= 3 ? args[2].toLowerCase() : MODE_HAND;
        if (!mode.equals(MODE_HAND) && !mode.equals(MODE_ALL)) {
            this.printUsage(sender);
            return;
        }

        PlayerInventory inventory = player.getInventory();
        if (mode.equals(MODE_HAND)) {
            ItemStack item = inventory.getItemInMainHand();
            if (ItemUT.isAir(item)) {
                this.errItem(sender);
                return;
            }
            if (!ItemStats.hasStat(item, null, TypedStat.Type.DURABILITY)) {
                plugin.lang().Repair_Error_NoDurability
                        .replace("%item%", ItemUT.getItemName(item))
                        .send(sender);
                return;
            }
            if (!this.module.repairItem(item)) {
                plugin.lang().Repair_Error_NotDamaged
                        .replace("%item%", ItemUT.getItemName(item))
                        .send(sender);
                return;
            }
            inventory.setItemInMainHand(item);
            plugin.lang().Repair_Done.replace("%item%", ItemUT.getItemName(item)).send(player);
            if (!sender.equals(player)) {
                plugin.lang().Repair_Cmd_Fix_Done_Others
                        .replace("%player%", player.getName())
                        .replace("%amount%", 1)
                        .send(sender);
            }
            return;
        }

        int repaired = 0;

        // Hand items
        ItemStack mainHand = inventory.getItemInMainHand();
        if (this.module.repairItem(mainHand)) {
            inventory.setItemInMainHand(mainHand);
            repaired++;
        }
        ItemStack offHand = inventory.getItemInOffHand();
        if (this.module.repairItem(offHand)) {
            inventory.setItemInOffHand(offHand);
            repaired++;
        }

        // Armor
        ItemStack[] armor        = inventory.getArmorContents();
        boolean     armorChanged = false;
        for (ItemStack item : armor) {
            if (item != null && this.module.repairItem(item)) {
                armorChanged = true;
                repaired++;
            }
        }
        if (armorChanged) inventory.setArmorContents(armor);

        if (repaired == 0) {
            plugin.lang().Repair_Cmd_Fix_Error_Nothing.send(sender);
            return;
        }

        plugin.lang().Repair_Cmd_Fix_Done_All.replace("%amount%", repaired).send(player);
        if (!sender.equals(player)) {
            plugin.lang().Repair_Cmd_Fix_Done_Others
                    .replace("%player%", player.getName())
                    .replace("%amount%", repaired)
                    .send(sender);
        }
    }
}
