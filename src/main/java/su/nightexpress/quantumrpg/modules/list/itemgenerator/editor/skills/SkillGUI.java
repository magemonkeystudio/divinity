package su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.skills;

import mc.promcteam.engine.config.api.JYML;
import mc.promcteam.engine.manager.api.gui.ContentType;
import mc.promcteam.engine.manager.api.gui.GuiClick;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.ItemGeneratorManager;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.AbstractEditorGUI;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.EditorGUI;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.LoreGUI;

import java.util.List;

public class SkillGUI extends AbstractEditorGUI {
    private final String path;
    private ItemType listening;

    public SkillGUI(@NotNull ItemGeneratorManager itemGeneratorManager, ItemGeneratorManager.GeneratorItem itemGenerator, String path) {
        super(itemGeneratorManager, itemGenerator, 9);
        setTitle("[&d"+itemGenerator.getId()+"&r] editor/"+EditorGUI.ItemType.SKILLS.getTitle());
        this.path = path;
    }

    @Override
    protected void onCreate(@NotNull Player player, @NotNull Inventory inventory, int page) {
        JYML cfg = this.itemGenerator.getConfig();
        GuiClick guiClick = (player1, type, clickEvent) -> {
            if (type == null) { return; }
            Class<?> clazz = type.getClass();
            if (clazz.equals(ContentType.class)) {
                ContentType type2 = (ContentType) type;
                switch (type2) {
                    case RETURN: {
                        new SkillListGUI(this.itemGeneratorManager, this.itemGenerator).open(player1, 1);
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
                String path = type2.getPath(this.path);
                switch (type2) {
                    case CHANCE: {
                        switch (clickEvent.getClick()) {
                            case DROP: case CONTROL_DROP: {
                                cfg.set(path, 0);
                                break;
                            }
                            default: {
                                sendSetMessage(type2, String.valueOf(cfg.getDouble(path)));
                                break;
                            }
                        }
                        break;
                    }
                    case MIN: case MAX: {
                        switch (clickEvent.getClick()) {
                            case LEFT: {
                                cfg.set(path, Math.max(1, cfg.getInt(path)-1));
                                saveAndReopen();
                                break;
                            }
                            case RIGHT: {
                                cfg.set(path, cfg.getInt(path)+1);
                                saveAndReopen();
                                break;
                            }
                            case DROP: case CONTROL_DROP: {
                                cfg.set(path, 1);
                                saveAndReopen();
                                break;
                            }
                            default: {
                                sendSetMessage(type2, String.valueOf(cfg.getInt(path)));
                            }
                        }
                        break;
                    }
                    case LORE: {
                        switch (clickEvent.getClick()) {
                            case DROP: case CONTROL_DROP: {
                                cfg.set(path, List.of("&b"+this.path.substring(this.path.lastIndexOf('.')+1)+" &7Lvl. &f%level%"));
                                saveAndReopen();
                                break;
                            }
                            default: {
                                new LoreGUI(this.itemGeneratorManager, this.itemGenerator, path, getTitle(), () -> new SkillGUI(this.itemGeneratorManager, this.itemGenerator, this.path).open(player1, 1)).open(player1, 1);
                            }
                        }
                        break;
                    }
                }
            }
        };
        this.addButton(this.createButton(ItemType.CHANCE.name(), ItemType.CHANCE, Material.DROPPER,
                                         "&eChance", List.of(
                                                 "&bCurrent: &a"+cfg.getDouble(ItemType.CHANCE.getPath(this.path)),
                                                 "&6Left-Click: &eSet",
                                                 "&6Drop: &eSet to default value"), 0, guiClick));
        this.addButton(this.createButton(ItemType.MIN.name(), ItemType.MIN, Material.BROWN_MUSHROOM,
                                         "&eMinimum Level", List.of(
                                                 "&bCurrent: &a"+cfg.getInt(ItemType.MIN.getPath(this.path)),
                                                 "&6Middle-Click: &eSet",
                                                 "&6Left-Click: &eDecrease",
                                                 "&6Right-Click: &eIncrease",
                                                 "&6Drop: &eSet to default value"), 1, guiClick));
        this.addButton(this.createButton(ItemType.MAX.name(), ItemType.MAX, Material.RED_MUSHROOM,
                                         "&eMaximum Level", List.of(
                                                 "&bCurrent: &a"+cfg.getInt(ItemType.MAX.getPath(this.path)),
                                                 "&6Middle-Click: &eSet",
                                                 "&6Left-Click: &eDecrease",
                                                 "&6Right-Click: &eIncrease",
                                                 "&6Drop: &eSet to default value"), 2, guiClick));
        this.addButton(this.createButton(ItemType.LORE.name(), ItemType.LORE, Material.WRITABLE_BOOK,
                                         "&eLore format", replaceLore(List.of(
                                                 "&bCurrent:",
                                                 "&a----------",
                                                 "&f%current%",
                                                 "&a----------",
                                                 "&6Left-Click: &eModify",
                                                 "&6Drop: &eSet to default value"), cfg.getStringList(ItemType.LORE.getPath(this.path))), 3, guiClick));
        this.addButton(this.createButton("return", ContentType.RETURN, Material.BARRIER, "&c&lReturn", List.of(), 8, guiClick));
    }

    private void sendSetMessage(ItemType itemType, String currentValue) {
        this.listening = itemType;
        player.closeInventory();
        String name;
        switch (itemType) {
            case CHANCE: {
                name = "Chance";
                break;
            }
            case MIN: {
                name = "Minimum Level";
                break;
            }
            case MAX: {
                name = "Maximum Level";
                break;
            }
            default: {
                throw new IllegalArgumentException();
            }
        }
        player.sendMessage("▸ Enter the desired "+name+", or \"cancel\" to go back");
        BaseComponent component = new TextComponent("[Current "+name+"]");
        component.setColor(ChatColor.GOLD);
        component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, currentValue));
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Enter the current "+name+" to chat")));
        player.spigot().sendMessage(component);
    }

    @Override
    public void onChat(AsyncPlayerChatEvent event) {
        if (this.listening == null) { return; }
        event.setCancelled(true);
        ItemType itemType = this.listening;
        this.listening = null;
        String message = event.getMessage().strip();
        switch (itemType) {
            case CHANCE: {
                double value;
                try {
                    value = Double.parseDouble(message);
                } catch (NumberFormatException e) {
                    plugin.lang().ItemGenerator_Cmd_Editor_Error_InvalidInput.replace("%input%", message).replace("%value%", "number").send(player);
                    saveAndReopen();
                    return;
                }
                this.itemGenerator.getConfig().set(itemType.getPath(this.path), value);
                saveAndReopen();
                break;
            }
            case MIN: case MAX: {
                int value;
                try {
                    value = Integer.parseInt(message);
                } catch (NumberFormatException e) {
                    plugin.lang().ItemGenerator_Cmd_Editor_Error_InvalidInput.replace("%input%", message).replace("%value%", "integer").send(player);
                    saveAndReopen();
                    return;
                }
                this.itemGenerator.getConfig().set(itemType.getPath(this.path), value);
                saveAndReopen();
                break;
            }
        }
    }

    public enum ItemType {
        CHANCE("chance"),
        MIN("min-level"),
        MAX("max-level"),
        LORE("lore-format"),
        ;

        private final String path;

        ItemType(String path) { this.path = path; }

        public String getPath(String rootPath) { return rootPath + '.' + path; }
    }
}
