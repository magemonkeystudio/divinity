package su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.stats;

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

import java.util.List;

public class StatGUI extends AbstractEditorGUI {
    private final String path;
    private final Runnable onReturn;
    private ItemType listening;

    public StatGUI(@NotNull ItemGeneratorManager itemGeneratorManager, ItemGeneratorManager.GeneratorItem itemGenerator, EditorGUI.ItemType itemType, String path, Runnable onReturn) {
        super(itemGeneratorManager, itemGenerator, 9);
        this.path = path;
        this.onReturn = onReturn;
        setTitle("[&d"+itemGenerator.getId()+"&r] editor/"+itemType.getTitle());
    }

    @Override
    protected void onCreate(@NotNull Player player, @NotNull Inventory inventory, int i) {
        JYML cfg = this.itemGenerator.getConfig();
        boolean flatRange = cfg.getBoolean(ItemType.FLAT_RANGE.getPath(this.path));
        GuiClick guiClick = (player1, type, clickEvent) -> {
            if (type == null) { return; }
            Class<?> clazz = type.getClass();
            if (clazz.equals(ContentType.class)) {
                ContentType type2 = (ContentType) type;
                switch (type2) {
                    case RETURN: {
                        if (this.onReturn == null) {
                            player1.closeInventory();
                        } else {
                            this.onReturn.run();
                        }
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
                    case CHANCE: case MIN: case MAX: {
                        switch (clickEvent.getClick()) {
                            case DROP: case CONTROL_DROP: {
                                cfg.set(path, 0);
                                saveAndReopen();
                                break;
                            }
                            default: {
                                sendSetMessage(type2, String.valueOf(cfg.getDouble(path)));
                            }
                        }
                        break;
                    }
                    case SCALE_BY_LEVEL: {
                        switch (clickEvent.getClick()) {
                            case DROP: case CONTROL_DROP: {
                                cfg.set(path, 1);
                                saveAndReopen();
                                break;
                            }
                            default: {
                                sendSetMessage(type2, String.valueOf(cfg.getDouble(path)));
                            }
                        }
                        break;
                    }
                    case FLAT_RANGE: {
                        switch (clickEvent.getClick()) {
                            case DROP: case CONTROL_DROP: {
                                cfg.set(path, false);
                                saveAndReopen();
                                break;
                            }
                            default: {
                                cfg.set(path, !flatRange);
                                saveAndReopen();
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
                                                 "&6Left-Click: &eModify",
                                                 "&6Drop: &eSet to default value"), 0, guiClick));
        this.addButton(this.createButton(ItemType.SCALE_BY_LEVEL.name(), ItemType.SCALE_BY_LEVEL, Material.EXPERIENCE_BOTTLE,
                                         "&eScale by Level", List.of(
                                                 "&bCurrent: &a"+cfg.getDouble(ItemType.SCALE_BY_LEVEL.getPath(this.path)),
                                                 "&6Left-Click: &eModify",
                                                 "&6Drop: &eSet to default value"), 1, guiClick));
        this.addButton(this.createButton(ItemType.MIN.name(), ItemType.MIN, Material.BROWN_MUSHROOM,
                                         "&eMinimum Value", List.of(
                                                 "&bCurrent: &a"+cfg.getDouble(ItemType.MIN.getPath(this.path)),
                                                 "&6Left-Click: &eModify",
                                                 "&6Drop: &eSet to default value"), 2, guiClick));
        this.addButton(this.createButton(ItemType.MAX.name(), ItemType.MAX, Material.RED_MUSHROOM,
                                         "&eMaximum Value", List.of(
                                                 "&bCurrent: &a"+cfg.getDouble(ItemType.MAX.getPath(this.path)),
                                                 "&6Left-Click: &eModify",
                                                 "&6Drop: &eSet to default value"), 3, guiClick));
        this.addButton(this.createButton(ItemType.FLAT_RANGE.name(), ItemType.FLAT_RANGE, flatRange ? Material.STRUCTURE_VOID : Material.BARRIER,
                                         "&eFlat Range", List.of(
                                                 "&bCurrent: &a"+flatRange,
                                                 "&6Left-Click: &eToggle",
                                                 "&6Drop: &eSet to default value"), 4, guiClick));
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
            case SCALE_BY_LEVEL: {
                name = "Level Scale";
                break;
            }
            case MIN: {
                name = "Minimum Value";
                break;
            }
            case MAX: {
                name = "Maximum Value";
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
    }

    public enum ItemType {
        CHANCE("chance"),
        SCALE_BY_LEVEL("scale-by-level"),
        MIN("min"),
        MAX("max"),
        FLAT_RANGE("flat-range"),
        ;

        private final String path;

        ItemType(String path) { this.path = path; }

        public String getPath(String rootPath) { return rootPath + '.' + path; }
    }
}
