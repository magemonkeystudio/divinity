package su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.skills;

import mc.promcteam.engine.manager.api.menu.Slot;
import mc.promcteam.engine.utils.StringUT;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.AbstractEditorGUI;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.EditorGUI;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.LoreGUI;

import java.util.List;

public class SkillGUI extends AbstractEditorGUI {
    private final String path;

    public SkillGUI(Player player, ItemGeneratorReference itemGenerator, String path) {
        super(player, 1, "[&d" + itemGenerator.getId() + "&r] editor/" + EditorGUI.ItemType.SKILLS.getTitle(), itemGenerator);
        this.path = path;
    }

    @Override
    public void setContents() {
        setSlot(0, new Slot(createItem(Material.DROPPER,
                "&eChance",
                "&bCurrent: &a" + itemGenerator.getConfig().getDouble(ItemType.CHANCE.getPath(path)),
                "&6Left-Click: &eSet",
                "&6Drop: &eSet to default value")) {
            @Override
            public void onLeftClick() {
                sendSetMessage(ItemType.CHANCE.getTitle(),
                        String.valueOf(itemGenerator.getConfig().getDouble(ItemType.CHANCE.getPath(path))),
                        s -> {
                            itemGenerator.getConfig().set(ItemType.CHANCE.getPath(path), Double.parseDouble(s));
                            saveAndReopen();
                        });
            }

            @Override
            public void onDrop() {
                itemGenerator.getConfig().set(ItemType.CHANCE.getPath(path), 0);
                saveAndReopen();
            }
        });
        setSlot(1, new Slot(createItem(Material.BROWN_MUSHROOM,
                "&eMinimum Level",
                "&bCurrent: &a" + itemGenerator.getConfig().getInt(ItemType.MIN.getPath(this.path)),
                "&6Shift-Left-Click: &eSet",
                "&6Left-Click: &eDecrease",
                "&6Right-Click: &eIncrease",
                "&6Drop: &eSet to default value")) {
            @Override
            public void onLeftClick() {
                itemGenerator.getConfig().set(ItemType.MIN.getPath(path), Math.max(1, itemGenerator.getConfig().getInt(ItemType.MIN.getPath(path)) - 1));
                saveAndReopen();
            }

            @Override
            public void onRightClick() {
                itemGenerator.getConfig().set(ItemType.MIN.getPath(path), itemGenerator.getConfig().getInt(ItemType.MIN.getPath(path)) + 1);
                saveAndReopen();
            }

            @Override
            public void onShiftLeftClick() {
                sendSetMessage(ItemType.MIN.getTitle(),
                        String.valueOf(itemGenerator.getConfig().getInt(ItemType.MIN.getPath(path))),
                        s -> {
                            itemGenerator.getConfig().set(ItemType.MIN.getPath(path), Double.parseDouble(s));
                            saveAndReopen();
                        });
            }

            @Override
            public void onDrop() {
                itemGenerator.getConfig().set(ItemType.MIN.getPath(path), 1);
                saveAndReopen();
            }
        });
        setSlot(2, new Slot(createItem(Material.RED_MUSHROOM,
                "&eMaximum Level",
                "&bCurrent: &a" + itemGenerator.getConfig().getInt(ItemType.MAX.getPath(path)),
                "&6Shift-Left-Click: &eSet",
                "&6Left-Click: &eDecrease",
                "&6Right-Click: &eIncrease",
                "&6Drop: &eSet to default value")) {
            @Override
            public void onLeftClick() {
                itemGenerator.getConfig().set(ItemType.MAX.getPath(path), Math.max(1, itemGenerator.getConfig().getInt(ItemType.MAX.getPath(path)) - 1));
                saveAndReopen();
            }

            @Override
            public void onRightClick() {
                itemGenerator.getConfig().set(ItemType.MAX.getPath(path), itemGenerator.getConfig().getInt(ItemType.MAX.getPath(path)) + 1);
                saveAndReopen();
            }

            @Override
            public void onShiftLeftClick() {
                sendSetMessage(ItemType.MAX.getTitle(),
                        String.valueOf(itemGenerator.getConfig().getInt(ItemType.MAX.getPath(path))),
                        s -> {
                            itemGenerator.getConfig().set(ItemType.MAX.getPath(path), Double.parseDouble(s));
                            saveAndReopen();
                        });
            }

            @Override
            public void onDrop() {
                itemGenerator.getConfig().set(ItemType.MAX.getPath(path), 1);
                saveAndReopen();
            }
        });
        setSlot(3, new Slot(createItem(Material.WRITABLE_BOOK,
                "&eLore format", StringUT.replace(CURRENT_PLACEHOLDER, itemGenerator.getConfig().getStringList(ItemType.LORE.getPath(path)),
                        "&bCurrent:",
                        "&a----------",
                        "&f%current%",
                        "&a----------",
                        "&6Left-Click: &eModify",
                        "&6Drop: &eSet to default value"))) {
            @Override
            public void onLeftClick() {
                openSubMenu(new LoreGUI(player, title, itemGenerator, ItemType.LORE.getPath(path)));
            }

            @Override
            public void onDrop() {
                itemGenerator.getConfig().set(ItemType.LORE.getPath(path), List.of("&b" + path.substring(path.lastIndexOf('.') + 1) + " &7Lvl. &f%level%"));
                saveAndReopen();
            }
        });
    }

    public enum ItemType {
        CHANCE("chance"),
        MIN("min-level"),
        MAX("max-level"),
        LORE("lore-format"),
        ;

        private final String path;

        ItemType(String path) {this.path = path;}

        public String getPath(String rootPath) {return rootPath + '.' + path;}

        public String getTitle() {return path.replace('-', ' ');}
    }
}
