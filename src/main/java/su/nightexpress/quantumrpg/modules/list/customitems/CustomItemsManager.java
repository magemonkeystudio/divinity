/**
 * Copyright 2021 Travja
 */
package su.nightexpress.quantumrpg.modules.list.customitems;

import mc.promcteam.engine.config.api.JYML;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.modules.EModule;
import su.nightexpress.quantumrpg.modules.ModuleItem;
import su.nightexpress.quantumrpg.modules.api.QModuleDrop;
import su.nightexpress.quantumrpg.modules.list.customitems.CustomItemsManager.CustomItem;

/*
name:
lore:
color: -1,-1,-1
unbreakable: true
item-flags:
- '*'
model-data:
 */

public class CustomItemsManager extends QModuleDrop<CustomItem> {

    public CustomItemsManager(@NotNull QuantumRPG plugin) {
        super(plugin, CustomItem.class);
    }

    @NotNull
    @Override
    public String getId() {
        return EModule.CUSTOM_ITEMS;
    }

    @NotNull
    @Override
    public String version() {
        return "1.0";
    }

    @Override
    public void setup() {

    }

    @Override
    public void shutdown() {

    }

    public class CustomItem extends ModuleItem {

        public CustomItem(@NotNull QuantumRPG plugin, @NotNull JYML cfg) {
            super(plugin, cfg, CustomItemsManager.this);
        }

    }

}
