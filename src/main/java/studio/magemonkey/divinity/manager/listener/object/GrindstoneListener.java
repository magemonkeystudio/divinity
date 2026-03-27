package studio.magemonkey.divinity.manager.listener.object;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.PrepareGrindstoneEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import studio.magemonkey.codex.manager.IListener;
import studio.magemonkey.divinity.Divinity;
import studio.magemonkey.divinity.modules.list.itemgenerator.ItemGeneratorManager;

public class GrindstoneListener extends IListener<Divinity> {
    public GrindstoneListener(@NonNull Divinity plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGrindStone(PrepareGrindstoneEvent e) {
        ItemStack result = e.getResult();
        if (result == null) return;
        ItemGeneratorManager.updateGeneratorItemLore(result);
    }
}
