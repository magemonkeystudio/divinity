package com.promcteam.divinity.manager.profile;

import com.promcteam.codex.config.api.JYML;
import com.promcteam.codex.manager.api.gui.ContentType;
import com.promcteam.codex.manager.api.gui.GuiClick;
import com.promcteam.codex.manager.api.gui.GuiItem;
import com.promcteam.codex.manager.api.gui.NGUI;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import com.promcteam.divinity.QuantumRPG;

public class ProfileGUI extends NGUI<QuantumRPG> {

    private ProfileManager profileManager;

    ProfileGUI(@NotNull ProfileManager profileManager, @NotNull JYML cfg) {
        super(profileManager.plugin, cfg, "");
        this.profileManager = profileManager;

        GuiClick click = (p, type, e) -> {
            if (type == null) return;

            Class<?> clazz = type.getClass();
            if (clazz.equals(ContentType.class)) {
                ContentType type2 = (ContentType) type;
                if (type2 == ContentType.EXIT) {
                    p.closeInventory();
                }
                return;
            }

            if (clazz.equals(ItemType.class)) {
                ItemType type2 = (ItemType) type;
                switch (type2) {
                    case PROFILE_CHANGE: {
                        this.profileManager.getProfilesGUI().open(p, 1);
                        break;
                    }
                    case PROFILE_SETTINGS: {
                        this.profileManager.getSettingsGUI().open(p, 1);
                        break;
                    }
                }
                return;
            }
        };

        for (String id : cfg.getSection("content")) {
            GuiItem guiItem = cfg.getGuiItem("content." + id, ItemType.class);
            if (guiItem == null) continue;

            if (guiItem.getType() != null) {
                guiItem.setClick(click);
            }
            this.addButton(guiItem);
        }
    }

    @Override
    protected void onCreate(@NotNull Player player, @NotNull Inventory inv, int page) {

    }

    @Override
    protected boolean ignoreNullClick() {
        return true;
    }

    @Override
    protected boolean cancelClick(int slot) {
        return true;
    }

    @Override
    protected boolean cancelPlayerClick() {
        return true;
    }

    private static enum ItemType {
        PROFILE_CHANGE,
        PROFILE_SETTINGS,
        ;
    }
}
