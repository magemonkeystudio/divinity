package su.nightexpress.quantumrpg.modules.list.itemgenerator.editor;

import mc.promcteam.engine.config.api.JYML;
import mc.promcteam.engine.manager.api.gui.ContentType;
import mc.promcteam.engine.manager.api.gui.GuiClick;
import mc.promcteam.engine.utils.constants.JStrings;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.ItemGeneratorManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemFlagsGUI extends AbstractEditorGUI {
    private static final String PATH = EditorGUI.ItemType.ITEM_FLAGS.getPath();

    public ItemFlagsGUI(@NotNull ItemGeneratorManager itemGeneratorManager, ItemGeneratorManager.GeneratorItem itemGenerator) {
        super(itemGeneratorManager, itemGenerator, 54);
        setTitle("[&d"+itemGenerator.getId()+"&r] editor/"+EditorGUI.ItemType.ITEM_FLAGS.getTitle());
    }

    @Override
    protected void onCreate(@NotNull Player player, @NotNull Inventory inventory, int page) {
        ItemFlag[] allFlags = ItemFlag.values();
        int totalPages = Math.max((int) Math.ceil(allFlags.length*1.0/42), 1);
        final int currentPage = page < 1 ? totalPages : Math.min(page, totalPages);
        this.setUserPage(player, currentPage, totalPages);
        GuiClick guiClick = (player1, type, inventoryClickEvent) -> {
            this.setUserPage(player, currentPage, totalPages);
            if (type == null) { return; }
            Class<?> clazz = type.getClass();
            if (clazz.equals(ContentType.class)) {
                ContentType type2 = (ContentType) type;
                switch (type2) {
                    case RETURN:
                        new EditorGUI(itemGeneratorManager, itemGenerator).open(player1, 1);
                        break;
                    case EXIT: {
                        player1.closeInventory();
                        break;
                    }
                    case NEXT: {
                        saveAndReopen(currentPage+1);
                        break;
                    }
                    case BACK: {
                        saveAndReopen(currentPage-1);
                        break;
                    }
                }
                return;
            }

            if (!(type instanceof ItemFlag)) { return; }
            String flag = type.name().toLowerCase();
            JYML cfg = itemGenerator.getConfig();
            Set<String> itemFlags = new HashSet<>(cfg.getStringList(PATH));
            if (itemFlags.contains(JStrings.MASK_ANY)) {
                itemFlags.remove(JStrings.MASK_ANY);
                for (ItemFlag itemFlag : ItemFlag.values()) {
                    itemFlags.add(itemFlag.name().toLowerCase());
                }
            }
            switch (inventoryClickEvent.getClick()) {
                case DROP: case CONTROL_DROP: {
                    Set<String> defaultFlags = new HashSet<>(EditorGUI.commonItemGenerator.getStringList(PATH));
                    if (defaultFlags.contains(flag) || defaultFlags.contains(JStrings.MASK_ANY)) {
                        itemFlags.add(flag);
                    } else {
                        itemFlags.remove(flag);
                    }
                    break;
                }
                default: {
                    if (itemFlags.contains(flag)) { itemFlags.remove(flag); } else { itemFlags.add(flag); }
                    break;
                }
            }
            boolean all = true;
            for (ItemFlag itemFlag : ItemFlag.values()) {
                if (!itemFlags.contains(itemFlag.name().toLowerCase())) {
                    all = false;
                    break;
                }
            }
            if (all) {
                itemFlags.clear();
                itemFlags.add(JStrings.MASK_ANY);
            }
            cfg.set(PATH, new ArrayList<>(itemFlags));
            saveAndReopen();
        };
        Set<ItemFlag> flags = this.itemGenerator.getFlags();
        for (int tierIndex = (currentPage-1)*42, last = Math.min(allFlags.length, tierIndex+42), invIndex = 1;
             tierIndex < last; tierIndex++, invIndex++) {
            if ((invIndex)%9 == 8) { invIndex += 2; }
            ItemFlag itemFlag = allFlags[tierIndex];
            Material material;
            switch (itemFlag) {
                case HIDE_ENCHANTS: {
                    material = Material.ENCHANTED_BOOK;
                    break;
                }
                case HIDE_ATTRIBUTES: {
                    material = Material.OAK_SIGN;
                    break;
                }
                case HIDE_UNBREAKABLE: {
                    material = Material.ANVIL;
                    break;
                }
                case HIDE_DESTROYS: {
                    material = Material.DIAMOND_PICKAXE;
                    break;
                }
                case HIDE_PLACED_ON: {
                    material = Material.OAK_PLANKS;
                    break;
                }
                case HIDE_POTION_EFFECTS: {
                    material = Material.POTION;
                    break;
                }
                case HIDE_DYE: {
                    material = Material.MAGENTA_DYE;
                    break;
                }
                default: {
                    material = Material.STONE;
                    break;
                }
            }
            String name = itemFlag.name().toLowerCase();
            this.addButton(this.createButton(name, itemFlag, material,
                                             "&e"+name, color(
                                                     "&bCurrent: &a"+flags.contains(itemFlag),
                                                     "&6Left-Click: &eToggle",
                                                     "&6Drop: &eSet to default value"), invIndex, guiClick));
        }
        this.addButton(this.createButton("prev-page", ContentType.BACK, Material.ENDER_PEARL, "&dPrevious Page", List.of(), 0, guiClick));
        this.addButton(this.createButton("next-page", ContentType.NEXT, Material.ENDER_PEARL, "&dNext Page", List.of(), 8, guiClick));
        this.addButton(this.createButton("exit", ContentType.RETURN, Material.BARRIER, "&c&lReturn", List.of(), 53, guiClick));
    }
}
