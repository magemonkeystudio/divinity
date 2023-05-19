package su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.materials;

import mc.promcteam.engine.manager.api.gui.ContentType;
import mc.promcteam.engine.manager.api.gui.GuiClick;
import mc.promcteam.engine.utils.StringUT;
import mc.promcteam.engine.utils.constants.JStrings;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.config.Config;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.ItemGeneratorManager;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.AbstractEditorGUI;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.EditorGUI;
import su.nightexpress.quantumrpg.types.ItemGroup;
import su.nightexpress.quantumrpg.types.ItemSubType;

import java.util.List;

public class MainMaterialsGUI extends AbstractEditorGUI {
    public MainMaterialsGUI(@NotNull ItemGeneratorManager itemGeneratorManager, ItemGeneratorManager.GeneratorItem itemGenerator) {
        super(itemGeneratorManager, itemGenerator, 9);
        setTitle("[&d"+itemGenerator.getId()+"&r] editor/"+EditorGUI.ItemType.MATERIALS.getTitle());
    }

    static Material getMaterial(String string) {
        try {
            return Material.valueOf(string.toUpperCase());
        } catch (IllegalArgumentException ignored) { }

        boolean isWildCard = string.startsWith(JStrings.MASK_ANY) || string.endsWith(JStrings.MASK_ANY);
        if (isWildCard) { string = string.replace(JStrings.MASK_ANY, ""); }
        try {
            return Material.valueOf(string.toUpperCase());
        } catch (IllegalArgumentException ignored) { }

        if (isWildCard) {
            for (Material material : Config.getAllRegisteredMaterials()) {
                String materialName = material.name();
                if (materialName.startsWith(string) || materialName.endsWith(string)) {
                    return material;
                }
            }
        }
        return Material.STONE;
    }

    static Material getMaterialGroup(String materialGroup) {
        ItemSubType subType = Config.getSubTypeById(materialGroup);
        if (subType != null) {
            return getMaterial(subType.getMaterials().stream().findAny().orElse("STONE"));
        }

        try {
            ItemGroup itemGroup = ItemGroup.valueOf(materialGroup.toUpperCase());
            return getMaterial(itemGroup.getMaterials().stream().findAny().orElse("STONE"));
        } catch (IllegalArgumentException ignored) { }

        return getMaterial(materialGroup.toUpperCase());
    }

    @Override
    protected void onCreate(@NotNull Player player, @NotNull Inventory inventory, int i) {
        boolean reversed = this.itemGenerator.isMaterialReversed();
        GuiClick guiClick = (player1, type, inventoryClickEvent) -> {
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
                        new EditorGUI(itemGeneratorManager, itemGenerator).open(player1, 1);
                        break;
                    }
                }
                return;
            }

            if (clazz.equals(ItemType.class)) {
                ItemType type2 = (ItemType) type;
                switch (type2) {
                    case REVERSE: {
                        this.itemGenerator.getConfig().set(type2.getPath(), !reversed);
                        saveAndReopen();
                        break;
                    }
                    case LIST: {
                        new MaterialListGUI(this.itemGeneratorManager, this.itemGenerator).open(player, 1);
                        break;
                    }
                    case MODEL_DATA: {
                        new MainModelDataGUI(this.itemGeneratorManager, this.itemGenerator).open(player, 1);
                        break;
                    }
                    case STAT_MODIFIERS: {
                        new MainStatModifiersGUI(this.itemGeneratorManager, this.itemGenerator).open(player, 1);
                        break;
                    }
                }
            }
        };
        this.addButton(this.createButton("reverse", ItemType.REVERSE, reversed ? Material.STRUCTURE_VOID : Material.BARRIER,
                                         "&eIs whitelist/reversed", List.of(
                                                 "&bCurrent: &a"+reversed,
                                                 "&6Left-Click: &eToggle"), 0, guiClick));
        this.addButton(this.createButton("materials", ItemType.LIST, Material.BOOK,
                                         "&e"+(reversed ? "Whitelist" : "Blacklist"), StringUT.replace(color(List.of(
                                                 "&bCurrent:",
                                                 "&a%current%",
                                                 "&6Left-Click: &eModify")), CURRENT_PLACEHOLDER, this.itemGenerator.getConfig().getStringList(ItemType.LIST.getPath())), 2, guiClick));
        this.addButton(this.createButton("model-data", ItemType.MODEL_DATA, Material.END_CRYSTAL,
                                         "&eModel Data", List.of(
                                                 "&6Left-Click: &eModify"), 4, guiClick));
        this.addButton(this.createButton("stat-modifiers", ItemType.STAT_MODIFIERS, Material.OAK_SIGN,
                                         "&eStat Modifiers", List.of(
                                                 "&6Left-Click: &eModify"), 6, guiClick));
        this.addButton(this.createButton("return", ContentType.RETURN, Material.BARRIER, "&c&lReturn", List.of(), 8, guiClick));
    }

    public enum ItemType {
        REVERSE("reverse"),
        LIST("black-list"),
        MODEL_DATA("model-data"),
        STAT_MODIFIERS("stat-modifiers"),
        ;

        private final String path;

        ItemType(String path) {
            this.path = "generator.materials."+path;
        }

        public String getPath() { return path; }
    }
}
