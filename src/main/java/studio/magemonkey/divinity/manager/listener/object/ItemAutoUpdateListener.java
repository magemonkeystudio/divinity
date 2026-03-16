package studio.magemonkey.divinity.manager.listener.object;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.magemonkey.codex.manager.IListener;
import studio.magemonkey.divinity.Divinity;
import studio.magemonkey.divinity.modules.ModuleItem;
import studio.magemonkey.divinity.modules.api.QModuleDrop;
import studio.magemonkey.divinity.stats.items.ItemStats;

public class ItemAutoUpdateListener extends IListener<Divinity> {

    public ItemAutoUpdateListener(@NotNull Divinity plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player    player = event.getPlayer();
        ItemStack item   = player.getInventory().getItem(event.getNewSlot());
        ItemStack updated = updateItem(item);
        if (updated != null) {
            player.getInventory().setItem(event.getNewSlot(), updated);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        updateInventory(event.getPlayer());
    }

    private void updateInventory(@NotNull Player player) {
        PlayerInventory inv      = player.getInventory();
        ItemStack[]     contents = inv.getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack updated = updateItem(contents[i]);
            if (updated != null) {
                inv.setItem(i, updated);
            }
        }
    }

    @Nullable
    private ItemStack updateItem(@Nullable ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return null;

        QModuleDrop<?> module = ItemStats.getModule(item);
        if (module == null || !module.isAutoUpdate()) return null;

        String itemId = ItemStats.getId(item);
        if (itemId == null) return null;

        ModuleItem moduleItem = module.getItemById(itemId);
        if (moduleItem == null) return null;

        return moduleItem.update(item);
    }
}
