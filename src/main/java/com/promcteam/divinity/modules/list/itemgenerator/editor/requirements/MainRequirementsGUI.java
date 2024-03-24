package com.promcteam.divinity.modules.list.itemgenerator.editor.requirements;

import com.promcteam.codex.manager.api.menu.Slot;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import com.promcteam.divinity.modules.list.itemgenerator.editor.AbstractEditorGUI;
import com.promcteam.divinity.modules.list.itemgenerator.editor.EditorGUI;

public class MainRequirementsGUI extends AbstractEditorGUI {

    public MainRequirementsGUI(Player player, ItemGeneratorReference itemGenerator) {
        super(player,
                1,
                "[&d" + itemGenerator.getId() + "&r] editor/" + EditorGUI.ItemType.REQUIREMENTS.getTitle(),
                itemGenerator);
    }

    @Override
    public void setContents() {
        setSlot(0, new Slot(createItem(Material.EXPERIENCE_BOTTLE,
                "&eLevel requirements",
                "&6Left-Click: &eModify")) {
            @Override
            public void onLeftClick() {
                openSubMenu(new RequirementsGUI(player,
                        itemGenerator,
                        MainRequirementsGUI.ItemType.LEVEL.getPath(),
                        Material.EXPERIENCE_BOTTLE));
            }
        });
        setSlot(1, new Slot(createItem(Material.BOW,
                "&eClass requirements",
                "&6Left-Click: &eModify")) {
            @Override
            public void onLeftClick() {
                openSubMenu(new RequirementsGUI(player,
                        itemGenerator,
                        MainRequirementsGUI.ItemType.CLASS.getPath(),
                        Material.BOW));
            }
        });
        setSlot(2, new Slot(createItem(Material.BARRIER,
                "&eBanned Class requirements",
                "&6Left-Click: &eModify")) {
            @Override
            public void onLeftClick() {
                openSubMenu(new RequirementsGUI(player,
                        itemGenerator,
                        MainRequirementsGUI.ItemType.BANNED_CLASS.getPath(),
                        Material.BARRIER));
            }
        });
    }

    public enum ItemType {
        LEVEL("level"),
        CLASS("class"),
        BANNED_CLASS("banned-class"),
        ;

        private final String path;

        ItemType(String path) {this.path = "generator.user-requirements-by-level." + path;}

        public String getPath() {return path;}
    }
}
