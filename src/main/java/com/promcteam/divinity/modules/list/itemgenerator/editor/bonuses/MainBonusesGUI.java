package com.promcteam.divinity.modules.list.itemgenerator.editor.bonuses;

import com.promcteam.codex.manager.api.menu.Slot;
import com.promcteam.divinity.modules.list.itemgenerator.editor.AbstractEditorGUI;
import com.promcteam.divinity.modules.list.itemgenerator.editor.EditorGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class MainBonusesGUI extends AbstractEditorGUI {

    public MainBonusesGUI(Player player, ItemGeneratorReference itemGenerator) {
        super(player,
                1,
                "[&d" + itemGenerator.getId() + "&r] editor/" + EditorGUI.ItemType.BONUSES.getTitle(),
                itemGenerator);
    }

    @Override
    public void setContents() {
        setSlot(0, new Slot(createItem(Material.IRON_INGOT,
                "&eMaterial bonuses",
                "&6Left-Click: &eModify")) {
            @Override
            public void onLeftClick() {
                openSubMenu(new BonusCategoryGUI(player, itemGenerator, ItemType.MATERIAL));
            }
        });
        setSlot(1, new Slot(createItem(Material.JACK_O_LANTERN,
                "&eClass bonuses",
                "&6Left-Click: &eModify")) {
            @Override
            public void onLeftClick() {
                openSubMenu(new BonusCategoryGUI(player, itemGenerator, ItemType.CLASS));
            }
        });
    }

    public enum ItemType {
        MATERIAL("material or group"),
        CLASS("class"),
        ;

        private final String description;

        ItemType(String description) {
            this.description = description;
        }

        public String getPath() {return "generator.bonuses." + name().toLowerCase();}

        public String getDescription() {return description;}
    }
}
