/**
 * Copyright 2024 MageMonkeyStudios
 */
package studio.magemonkey.divinity.modules.list.customitems;

import studio.magemonkey.codex.config.api.JYML;
import studio.magemonkey.divinity.Divinity;
import studio.magemonkey.divinity.modules.EModule;
import studio.magemonkey.divinity.modules.ModuleItem;
import studio.magemonkey.divinity.modules.api.QModuleDrop;
import studio.magemonkey.divinity.modules.list.customitems.CustomItemsManager.CustomItem;
import org.jetbrains.annotations.NotNull;

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

    public CustomItemsManager(@NotNull Divinity plugin) {
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

        public CustomItem(@NotNull Divinity plugin, @NotNull JYML cfg) {
            super(plugin, cfg, CustomItemsManager.this);
        }

    }

}
