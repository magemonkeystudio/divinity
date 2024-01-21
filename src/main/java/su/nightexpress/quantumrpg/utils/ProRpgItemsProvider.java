package su.nightexpress.quantumrpg.utils;

import mc.promcteam.engine.items.ItemType;
import mc.promcteam.engine.items.providers.IProItemProvider;
import mc.promcteam.engine.modules.IModule;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.modules.ModuleItem;
import su.nightexpress.quantumrpg.modules.api.QModuleDrop;
import su.nightexpress.quantumrpg.stats.items.ItemStats;

import java.util.Objects;

public class ProRpgItemsProvider implements IProItemProvider<ProRpgItemsProvider.ProRPGItemsItemType> {
    public static final String NAMESPACE = "PRORPGITEMS";

    @Override
    public String pluginName() {
        return "ProRPGItems";
    }

    @Override
    public Category getCategory() {
        return Category.PRO;
    }

    @Override
    public ProRPGItemsItemType getItem(String id) {
        for (IModule<?> module : QuantumRPG.getInstance().getModuleManager().getModules()) {
            if (!(module instanceof QModuleDrop)) continue;

            ModuleItem item = ((QModuleDrop<? extends ModuleItem>) module).getItemById(id);
            if (item != null) {
                return new ProRPGItemsItemType(item);
            }
        }

        return null;
    }

    @Override
    @Nullable
    public ProRpgItemsProvider.ProRPGItemsItemType getItem(ItemStack itemStack) {
        String id = ItemStats.getId(itemStack);
        if (id == null) return null;
        return getItem(id);
    }

    @Override
    public boolean isCustomItem(ItemStack item) {
        return ItemStats.getId(item) != null;
    }

    @Override
    public boolean isCustomItemOfId(ItemStack item, String id) {
        String[] split = id.split("_", 2);
        id = split.length == 2 && split[0].equalsIgnoreCase(NAMESPACE) ? split[1] : id;

        String itemId = ItemStats.getId(item);
        return itemId != null && itemId.equals(id);
    }

    public static class ProRPGItemsItemType extends ItemType {
        private final ModuleItem moduleItem;

        public ProRPGItemsItemType(ModuleItem moduleItem) {
            this.moduleItem = moduleItem;
        }

        @Override
        public String getNamespace() {
            return NAMESPACE;
        }

        @Override
        public String getID() {
            return this.moduleItem.getId();
        }

        @Override
        public Category getCategory() {
            return Category.PRO;
        }

        @Override
        public ItemStack create() {
            return this.moduleItem.create();
        }

        @Override
        public boolean isInstance(@Nullable ItemStack itemStack) {
            if (itemStack == null) return false;
            String id = ItemStats.getId(itemStack);
            return id != null && id.equals(this.moduleItem.getId())
                    && Objects.equals(ItemStats.getModule(itemStack), this.moduleItem.getModule());
        }
    }
}
