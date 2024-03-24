/**
 * Copyright 2024 ProMCTeam
 */
package com.promcteam.divinity.modules.list.customitems;

import com.promcteam.codex.config.api.JYML;
import org.jetbrains.annotations.NotNull;
import com.promcteam.divinity.QuantumRPG;
import com.promcteam.divinity.modules.EModule;
import com.promcteam.divinity.modules.ModuleItem;
import com.promcteam.divinity.modules.api.QModuleDrop;
import com.promcteam.divinity.modules.list.customitems.CustomItemsManager.CustomItem;

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
