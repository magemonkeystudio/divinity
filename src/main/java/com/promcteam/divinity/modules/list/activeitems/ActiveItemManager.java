package com.promcteam.divinity.modules.list.activeitems;

import com.promcteam.codex.config.api.JYML;
import org.jetbrains.annotations.NotNull;
import com.promcteam.divinity.QuantumRPG;
import com.promcteam.divinity.modules.UsableItem;
import com.promcteam.divinity.modules.api.QModuleUsage;

public class ActiveItemManager extends QModuleUsage<ActiveItemManager.ActiveItem> {
    public ActiveItemManager(@NotNull QuantumRPG plugin) {
        super(plugin, ActiveItem.class);
    }

    @NotNull
    public String getId() {
        return "active_items";
    }

    @NotNull
    public String version() {
        return "1.3.0";
    }

    public void setup() {
    }

    public void shutdown() {
    }

    public class ActiveItem extends UsableItem {
        public ActiveItem(@NotNull QuantumRPG plugin, JYML cfg) {
            super(plugin, cfg, ActiveItemManager.this);
        }
    }
}
