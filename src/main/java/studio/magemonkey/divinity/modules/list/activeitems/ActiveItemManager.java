package studio.magemonkey.divinity.modules.list.activeitems;

import studio.magemonkey.codex.config.api.JYML;
import studio.magemonkey.divinity.Divinity;
import studio.magemonkey.divinity.modules.UsableItem;
import studio.magemonkey.divinity.modules.api.QModuleUsage;
import org.jetbrains.annotations.NotNull;

public class ActiveItemManager extends QModuleUsage<ActiveItemManager.ActiveItem> {
    public ActiveItemManager(@NotNull Divinity plugin) {
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
        public ActiveItem(@NotNull Divinity plugin, JYML cfg) {
            super(plugin, cfg, ActiveItemManager.this);
        }
    }
}
