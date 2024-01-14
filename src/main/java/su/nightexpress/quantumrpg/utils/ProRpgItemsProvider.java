package su.nightexpress.quantumrpg.utils;

import mc.promcteam.engine.items.providers.IProItemProvider;
import mc.promcteam.engine.modules.IModule;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.modules.ModuleItem;
import su.nightexpress.quantumrpg.modules.api.QModuleDrop;
import su.nightexpress.quantumrpg.stats.items.ItemStats;

public class ProRpgItemsProvider implements IProItemProvider {
    @Override
    public String pluginName() {
        return "ProRPGItems";
    }

    @Override
    public ItemStack getItem(String id) {
        for (IModule<?> module : QuantumRPG.getInstance().getModuleManager().getModules()) {
            if (!(module instanceof QModuleDrop)) continue;

            ModuleItem item = ((QModuleDrop<? extends ModuleItem>) module).getItemById(id);
            if (item != null) {
                return item.create();
            }
        }

        return null;
    }

    @Override
    public boolean isCustomItem(ItemStack item) {
        return ItemStats.getId(item) != null;
    }

    @Override
    public boolean isCustomItemOfId(ItemStack item, String id) {
        String itemId = ItemStats.getId(item);
        return itemId != null && itemId.equals(id);
    }
}
