package su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.stats;

import mc.promcteam.engine.manager.api.menu.Slot;
import mc.promcteam.engine.utils.StringUT;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.AbstractEditorGUI;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.EditorGUI;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.LoreGUI;

public class MainStatsGUI extends AbstractEditorGUI {
    private final EditorGUI.ItemType itemType;

    public MainStatsGUI(Player player, ItemGeneratorReference itemGenerator, EditorGUI.ItemType itemType) {
        super(player, 1, "[&d" + itemGenerator.getId() + "&r] editor/" + itemType.getTitle(), itemGenerator);
        this.itemType = itemType;
    }

    @Override
    public void setContents() {
        setSlot(0, new Slot(createItem(Material.BROWN_MUSHROOM,
                "&eMinimum " + this.itemType.getTitle(),
                "&bCurrent: &a" + itemGenerator.getConfig().getInt(ItemType.MINIMUM.getPath(this.itemType)),
                "&6Middle-Click: &eSet",
                "&6Left-Click: &eDecrease",
                "&6Right-Click: &eIncrease",
                "&6Drop: &eSet to default value")) {
            @Override
            public void onLeftClick() {
                String path = ItemType.MINIMUM.getPath(itemType);
                itemGenerator.getConfig().set(path, Math.max(0, itemGenerator.getConfig().getInt(path) - 1));
                saveAndReopen();
            }

            @Override
            public void onRightClick() {
                String path = ItemType.MINIMUM.getPath(itemType);
                itemGenerator.getConfig().set(path, Math.max(0, itemGenerator.getConfig().getInt(path) + 1));
                saveAndReopen();
            }

            @Override
            public void onMiddleClick() {
                sendSetMessage(ItemType.MINIMUM.getPath() + " " + itemType.getTitle(),
                        String.valueOf(itemGenerator.getConfig().getInt(ItemType.MINIMUM.getPath(itemType))),
                        s -> {
                    int minimum = Integer.parseInt(s);
                    if (minimum >= 0) {
                        itemGenerator.getConfig().set(ItemType.MINIMUM.getPath(itemType), minimum);
                    } else {
                        throw new IllegalArgumentException();
                    }
                    saveAndReopen();
                });
            }

            @Override
            public void onDrop() {
                itemGenerator.getConfig().set(ItemType.MINIMUM.getPath(itemType), 0);
                saveAndReopen();
            }
        });
        setSlot(1, new Slot(createItem(Material.RED_MUSHROOM,
                "&eMaximum " + this.itemType.getTitle(),
                "&bCurrent: &a" + itemGenerator.getConfig().getInt(ItemType.MAXIMUM.getPath(this.itemType)),
                "&6Middle-Click: &eSet",
                "&6Left-Click: &eDecrease",
                "&6Right-Click: &eIncrease",
                "&6Drop: &eSet to default value")) {
            @Override
            public void onLeftClick() {
                String path = ItemType.MAXIMUM.getPath(itemType);
                itemGenerator.getConfig().set(path, Math.max(0, itemGenerator.getConfig().getInt(path) - 1));
                saveAndReopen();
            }

            @Override
            public void onRightClick() {
                String path = ItemType.MAXIMUM.getPath(itemType);
                itemGenerator.getConfig().set(path, Math.max(0, itemGenerator.getConfig().getInt(path) + 1));
                saveAndReopen();
            }

            @Override
            public void onMiddleClick() {
                sendSetMessage(ItemType.MAXIMUM.getPath() + " " + itemType.getTitle(),
                        String.valueOf(itemGenerator.getConfig().getInt(ItemType.MAXIMUM.getPath(itemType))),
                        s -> {
                    int maximum = Integer.parseInt(s);
                    if (maximum >= 0) {
                        itemGenerator.getConfig().set(ItemType.MAXIMUM.getPath(itemType), maximum);
                    } else {
                        throw new IllegalArgumentException();
                    }
                    saveAndReopen();
                        });
            }

            @Override
            public void onDrop() {
                itemGenerator.getConfig().set(ItemType.MAXIMUM.getPath(itemType), 0);
                saveAndReopen();
            }
        });
        setSlot(2, new Slot(createItem(Material.WRITABLE_BOOK,
                "&eLore format", StringUT.replace(CURRENT_PLACEHOLDER, itemGenerator.getConfig().getStringList(ItemType.LORE.getPath(this.itemType)),
                        "&bCurrent:",
                        "&a----------",
                        "&f%current%",
                        "&a----------",
                        "&6Left-Click: &eModify"))) {
            @Override
            public void onLeftClick() {
                openSubMenu(new LoreGUI(player, title + " lore", itemGenerator, ItemType.LORE.getPath(itemType)));
            }

            @Override
            public void onDrop() {
                setDefault(ItemType.LORE.getPath(itemType));
                saveAndReopen();
            }
        });
        Material material;
        switch (this.itemType) {
            case DAMAGE_TYPES: {
                material = Material.IRON_SWORD;
                break;
            }
            case DEFENSE_TYPES: {
                material = Material.IRON_CHESTPLATE;
                break;
            }
            case SKILLAPI_ATTRIBUTES: {
                material = Material.BOOK;
                break;
            }
            default: {
                material = Material.PAPER;
                break;
            }
        }
        setSlot(3, new Slot(createItem(material,
                "&eList of " + this.itemType.getTitle(),
                "&6Left-Click: &eModify")) {
            @Override
            public void onLeftClick() {
                openSubMenu(new StatListGUI(player, itemGenerator, itemType));
            }
        });
    }

    public enum ItemType {
        MINIMUM("minimum"),
        MAXIMUM("maximum"),
        LORE("lore-format"),
        LIST("list"),
        ;

        private final String path;

        ItemType(String path) {this.path = path;}

        public String getPath() {return path;}

        public String getPath(EditorGUI.ItemType itemType) {return itemType.getPath() + '.' + path;}
    }
}
