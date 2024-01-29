package su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.bonuses;

import mc.promcteam.engine.manager.api.menu.Slot;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.AbstractEditorGUI;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.EditorGUI;

public class MainBonusesGUI extends AbstractEditorGUI {

    public MainBonusesGUI(Player player, ItemGeneratorReference itemGenerator) {
        super(player, 1, "[&d" + itemGenerator.getId() + "&r] editor/" + EditorGUI.ItemType.BONUSES.getTitle(), itemGenerator);
    }

    @Override
    public void setContents() {
        setSlot(0, new Slot(createItem(Material.IRON_INGOT,
                "&eMaterial bonuses",
                "&6Left-Click: &eModify")) {
            @Override
            public void onLeftClick() {
                openSubMenu(new MaterialBonusesGUI(player, itemGenerator));
            }
        });
    }

    public enum ItemType {
        MATERIAL,
        CLASS, // TODO
        ;

        public String getPath() {return "generator.bonuses." + name().toLowerCase();}
    }
}
