package su.nightexpress.quantumrpg.modules.list.activeitems;

import mc.promcteam.engine.config.api.JYML;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.modules.UsableItem;
import su.nightexpress.quantumrpg.modules.api.QModuleUsage;

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
