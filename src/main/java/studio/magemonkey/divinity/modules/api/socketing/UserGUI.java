package studio.magemonkey.divinity.modules.api.socketing;

import studio.magemonkey.codex.manager.api.gui.JIcon;
import studio.magemonkey.divinity.stats.items.ItemStats;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class UserGUI extends ISocketGUI {

    UserGUI(@NotNull ModuleSocket<?> module) {
        super(module, module.getJYML());
    }

    @Override
    protected void onCreate(@NotNull Player player, @NotNull Inventory inv, int page) {}

    @Override
    protected int getChance(@NotNull Player player, @NotNull Inventory inv) {
        ItemStack socket = this.getItem(inv, this.getSourceSlot());
        return ItemStats.getSocketRate(socket) + this.module.getSilentRateBonus(player);
    }
}
