package com.promcteam.divinity.api;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.promcteam.divinity.QuantumRPG;
import com.promcteam.divinity.modules.*;
import com.promcteam.divinity.modules.api.QModuleDrop;

public class QuantumAPI {

    private static final QuantumRPG plugin = QuantumRPG.getInstance();

    public static ModuleCache getModuleManager() {
        return plugin.getModuleCache();
    }

    @Nullable
    public static ModuleItem getModuleItem(@NotNull QModuleDrop<?> e, @NotNull String id) {
        if (!e.isEnabled() || !e.isLoaded()) return null;
        return e.getItemById(id);
    }

    @Nullable
    public static ItemStack getItemByModule(
            @NotNull QModuleDrop<?> e,
            @NotNull String id,
            int lvl, int uses, int suc) {

        ModuleItem mi = getModuleItem(e, id);
        if (mi == null) return null;

        if (mi instanceof RatedItem) {
            RatedItem si = (RatedItem) mi;
            return si.create(lvl, uses, suc);
        } else if (mi instanceof LimitedItem) {
            LimitedItem si = (LimitedItem) mi;
            return si.create(lvl, suc); // suc = uses here.
        } else if (mi instanceof LeveledItem) {
            return ((LeveledItem) mi).create(lvl);
        } else {
            return mi.create();
        }
    }
}
