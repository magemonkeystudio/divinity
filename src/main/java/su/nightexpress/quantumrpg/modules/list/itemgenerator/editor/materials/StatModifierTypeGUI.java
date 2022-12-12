package su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.materials;

import mc.promcteam.engine.manager.api.gui.ContentType;
import mc.promcteam.engine.manager.api.gui.GuiClick;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.ItemGeneratorManager;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.AbstractEditorGUI;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.EditorGUI;

import java.util.List;

public class StatModifierTypeGUI extends AbstractEditorGUI {
    private final String group;

    public StatModifierTypeGUI(@NotNull ItemGeneratorManager itemGeneratorManager, ItemGeneratorManager.GeneratorItem itemGenerator, String group) {
        super(itemGeneratorManager, itemGenerator, 9);
        this.group = group;
        setTitle("[&d"+itemGenerator.getId()+"&r] editor/"+EditorGUI.ItemType.MATERIALS.getTitle());
    }

    @Override
    protected void onCreate(@NotNull Player player, @NotNull Inventory inventory, int i) {
        GuiClick guiClick = (player1, type, clickEvent) -> {
            if (type == null) { return; }
            Class<?> clazz = type.getClass();
            if (clazz.equals(ContentType.class)) {
                ContentType type2 = (ContentType) type;
                switch (type2) {
                    case EXIT: {
                        player.closeInventory();
                        break;
                    }
                    case RETURN: {
                        new StatModifiersGUI(itemGeneratorManager, itemGenerator, group).open(player1, 1);
                        break;
                    }
                }
                return;
            }

            if (clazz.equals(ItemType.class)) {
                new NewStatModifierGUI(itemGeneratorManager, itemGenerator, group, (ItemType) type).open(player1, 1);
            }
        };
        this.addButton(this.createButton("damage-type", ItemType.DAMAGE, Material.IRON_SWORD, "&eAdd new damage type", List.of(), 2, guiClick));
        this.addButton(this.createButton("defense-type", ItemType.DEFENSE, Material.IRON_CHESTPLATE, "&eAdd new defense type", List.of(), 4, guiClick));
        this.addButton(this.createButton("item-stat", ItemType.ITEM_STAT, Material.OAK_SIGN, "&eAdd new item stat", List.of(), 6, guiClick));
        this.addButton(this.createButton("return", ContentType.RETURN, Material.BARRIER, "&c&lReturn", List.of(), 8, guiClick));
    }

    public enum ItemType {
        DAMAGE("damage-types"),
        DEFENSE("defense-types"),
        ITEM_STAT("item-stats"),
        ;

        private final String path;

        ItemType(String path) { this.path = path; }

        public String getPath() { return path; }
    }
}
