package su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.requirements;

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

public class MainRequirementsGUI extends AbstractEditorGUI {

    public MainRequirementsGUI(@NotNull ItemGeneratorManager itemGeneratorManager, ItemGeneratorManager.GeneratorItem itemGenerator) {
        super(itemGeneratorManager, itemGenerator, 9);
        setTitle("[&d"+itemGenerator.getId()+"&r] editor/"+EditorGUI.ItemType.REQUIREMENTS.getTitle());
    }

    @Override
    protected void onCreate(@NotNull Player player, @NotNull Inventory inventory, int page) {
        GuiClick guiClick = (player1, type, clickEvent) -> {
            if (type == null) { return; }
            Class<?> clazz = type.getClass();
            if (clazz.equals(ContentType.class)) {
                ContentType type2 = (ContentType) type;
                switch (type2) {
                    case RETURN: {
                        new EditorGUI(itemGeneratorManager, itemGenerator).open(player1, 1);
                        break;
                    }
                    case EXIT: {
                        player1.closeInventory();
                        break;
                    }
                }
                return;
            }

            if (clazz.equals(ItemType.class)) {
                ItemType type2 = (ItemType) type;
                switch (type2) {
                    case LEVEL: {
                        new RequirementsGUI(itemGeneratorManager, itemGenerator, MainRequirementsGUI.ItemType.LEVEL.getPath(), Material.EXPERIENCE_BOTTLE).open(player1, 1);
                        break;
                    }
                    case CLASS: {
                        new RequirementsGUI(itemGeneratorManager, itemGenerator, MainRequirementsGUI.ItemType.CLASS.getPath(), Material.BOW).open(player1, 1);
                        break;
                    }
                    case BANNED_CLASS: {
                        new RequirementsGUI(itemGeneratorManager, itemGenerator, MainRequirementsGUI.ItemType.BANNED_CLASS.getPath(), Material.BOW).open(player1, 1);
                        break;
                    }
                }
            }
        };

        this.addButton(this.createButton("level", ItemType.LEVEL, Material.EXPERIENCE_BOTTLE,
                                         "&eLevel requirements", List.of(
                                                "&6Left-Click: &eSet"), 2, guiClick));
        this.addButton(this.createButton("class", ItemType.CLASS, Material.BOW,
                                         "&eClass requirements", List.of(
                                                "&6Left-Click: &eSet"), 4, guiClick));
        this.addButton(this.createButton("banned-class", ItemType.BANNED_CLASS, Material.BARRIER,
                                         "&eBanned Class requirements", List.of(
                                                "&6Left-Click: &eSet"), 6, guiClick));
        this.addButton(this.createButton("exit", ContentType.RETURN, Material.BARRIER, "&c&lReturn", List.of(), 8, guiClick));
    }

    public enum ItemType {
        LEVEL("level"),
        CLASS("class"),
        BANNED_CLASS("banned-class"),
        ;

        private final String path;

        ItemType(String path) { this.path = "generator.user-requirements-by-level."+path; }

        public String getPath() { return path; }
    }
}
