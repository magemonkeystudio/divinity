package su.nightexpress.quantumrpg.modules.api.socketing;

import mc.promcteam.engine.manager.api.gui.JIcon;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.stats.items.ItemStats;

class UserGUI extends ISocketGUI {

    UserGUI(@NotNull ModuleSocket<?> module) {
        super(module, module.getJYML());
    }

    @Override
    public void open(@NotNull Player player, @NotNull ItemStack target, @NotNull ItemStack src) {
        ItemStack result = new ItemStack(module.insertSocket(new ItemStack(target), new ItemStack(src)));

        this.addButton(player, new JIcon(target), this.itemSlot);
        this.addButton(player, new JIcon(src), this.sourceSlot);
        this.addButton(player, new JIcon(result), this.resultSlot);

        super.open(player, 1);
    }

    @Override
    protected void onCreate(@NotNull Player player, @NotNull Inventory inv, int page) {

    }

    @Override
    protected int getChance(@NotNull Player player, @NotNull Inventory inv) {
        ItemStack socket = this.getItem(inv, this.getSourceSlot());
        return ItemStats.getSocketRate(socket) + this.module.getSilentRateBonus(player);
    }
}
