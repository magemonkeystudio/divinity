package su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.bonuses;

import mc.promcteam.engine.manager.api.menu.Slot;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.AbstractEditorGUI;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.EditorGUI;

public class BonusStatTypeGUI extends AbstractEditorGUI {
    private final String path;

    public BonusStatTypeGUI(Player player, ItemGeneratorReference itemGenerator, String path) {
        super(player, 1, "[&d" + itemGenerator.getId() + "&r] editor/" + EditorGUI.ItemType.BONUSES.getTitle(), itemGenerator);
        this.path = path;
    }

    @Override
    public void setContents() {
        setSlot(0, new Slot(createItem(Material.IRON_SWORD, "&eAdd new damage type")) {
            @Override
            public void onLeftClick() {
                openSubMenu(new NewBonusStatGUI(player, itemGenerator, path, ItemType.DAMAGE));
            }
        });
        setSlot(1, new Slot(createItem(Material.IRON_CHESTPLATE, "&eAdd new defense type")) {
            @Override
            public void onLeftClick() {
                openSubMenu(new NewBonusStatGUI(player, itemGenerator, path, ItemType.DEFENSE));
            }
        });
        setSlot(2, new Slot(createItem(Material.OAK_SIGN, "&eAdd new item stat")) {
            @Override
            public void onLeftClick() {
                openSubMenu(new NewBonusStatGUI(player, itemGenerator, path, ItemType.ITEM_STAT));
            }
        });
    }

    public enum ItemType {
        DAMAGE("damage-types"),
        DEFENSE("defense-types"),
        ITEM_STAT("item-stats"),
        ;

        private final String path;

        ItemType(String path) {this.path = path;}

        public String getPath() {return path;}
    }
}
