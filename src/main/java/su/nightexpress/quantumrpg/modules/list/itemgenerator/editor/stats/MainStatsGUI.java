package su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.stats;

import mc.promcteam.engine.config.api.JYML;
import mc.promcteam.engine.manager.api.gui.ContentType;
import mc.promcteam.engine.manager.api.gui.GuiClick;
import mc.promcteam.engine.utils.StringUT;
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

public class MainStatsGUI extends AbstractEditorGUI {
    private final EditorGUI.ItemType itemType;
    private ItemType listening;

    public MainStatsGUI(@NotNull ItemGeneratorManager itemGeneratorManager, ItemGeneratorManager.GeneratorItem itemGenerator, EditorGUI.ItemType itemType) {
        super(itemGeneratorManager, itemGenerator, 9);
        this.itemType = itemType;
        setTitle("[&d"+itemGenerator.getId()+"&r] editor/"+this.itemType.getTitle());
    }

    @Override
    protected void onCreate(@NotNull Player player, @NotNull Inventory inventory, int page) {
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
                        new EditorGUI(itemGeneratorManager, itemGenerator).open(player1, 1);
                        break;
                    }
                }
                return;
            }

            if (clazz.equals(ItemType.class)) {
                ItemType type2 = (ItemType) type;
                JYML cfg = this.itemGenerator.getConfig();
                switch (type2) {
                    case MINIMUM: {
                        switch (clickEvent.getClick()) {
                            case LEFT: {
                                String path = type2.getPath(this.itemType);
                                cfg.set(path, Math.max(0, cfg.getInt(path)-1));
                                saveAndReopen();
                                break;
                            }
                            case RIGHT: {
                                String path = type2.getPath(this.itemType);
                                cfg.set(path, cfg.getInt(path)+1);
                                saveAndReopen();
                                break;
                            }
                            case DROP: case CONTROL_DROP: {
                                cfg.set(type2.getPath(this.itemType), 0);
                                saveAndReopen();
                                break;
                            }
                            default: {
                                sendSetMessage(type2, String.valueOf(cfg.getInt(type2.getPath(this.itemType))));
                                break;
                            }
                        }
                        break;
                    }
                    case MAXIMUM: {
                        switch (clickEvent.getClick()) {
                            case LEFT: {
                                String path = type2.getPath(this.itemType);
                                cfg.set(path, Math.max(0, cfg.getInt(path)-1));
                                saveAndReopen();
                                break;
                            }
                            case RIGHT: {
                                String path = type2.getPath(this.itemType);
                                cfg.set(path, cfg.getInt(path)+1);
                                saveAndReopen();
                                break;
                            }
                            case DROP: case CONTROL_DROP: {
                                cfg.set(type2.getPath(this.itemType), 2);
                                saveAndReopen();
                                break;
                            }
                            default: {
                                sendSetMessage(type2, String.valueOf(cfg.getInt(type2.getPath(this.itemType))));
                            }
                        }
                        break;
                    }
                    case LORE: {
                        switch (clickEvent.getClick()) {
                            case DROP: case CONTROL_DROP: {
                                setDefault(type2.getPath(this.itemType));
                                break;
                            }
                            default: {
                                new LoreGUI(itemGeneratorManager, itemGenerator, type2.getPath(this.itemType), this.getTitle()+" lore", () -> new MainStatsGUI(this.itemGeneratorManager, this.itemGenerator, this.itemType).open(player1, 1)).open(player1, 1);
                                break;
                            }
                        }
                        break;
                    }
                    case LIST: {
                        new StatListGUI(itemGeneratorManager, itemGenerator, itemType, () -> new MainStatsGUI(itemGeneratorManager, itemGenerator, itemType).open(player, page)).open(player1, 1);
                        break;
                    }
                }
            }
        };
        JYML cfg = this.itemGenerator.getConfig();
        this.addButton(this.createButton("minimum", ItemType.MINIMUM, Material.BROWN_MUSHROOM,
                                         "&eMinimum "+this.itemType.getTitle(), List.of(
                                                 "&bCurrent: &a"+cfg.getInt(ItemType.MINIMUM.getPath(this.itemType)),
                                                 "&6Middle-Click: &eSet",
                                                 "&6Left-Click: &eDecrease",
                                                 "&6Right-Click: &eIncrease",
                                                 "&6Drop: &eSet to default value"), 0, guiClick));
        this.addButton(this.createButton("maximum", ItemType.MAXIMUM, Material.RED_MUSHROOM,
                                         "&eMaximum "+this.itemType.getTitle(), List.of(
                                                 "&bCurrent: &a"+cfg.getInt(ItemType.MAXIMUM.getPath(this.itemType)),
                                                 "&6Middle-Click: &eSet",
                                                 "&6Left-Click: &eDecrease",
                                                 "&6Right-Click: &eIncrease",
                                                 "&6Drop: &eSet to default value"), 1, guiClick));
        this.addButton(this.createButton("lore", ItemType.LORE, Material.WRITABLE_BOOK,
                                         "&eLore format", StringUT.replace(color(List.of(
                                                 "&bCurrent:",
                                                 "&a----------",
                                                 "&f%current%",
                                                 "&a----------",
                                                 "&6Left-Click: &eModify")), CURRENT_PLACEHOLDER, cfg.getStringList(ItemType.LORE.getPath(this.itemType))), 2, guiClick));
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
        this.addButton(this.createButton("list", ItemType.LIST, material,
                                         "&eList of "+this.itemType.getTitle(), List.of(
                                                 "&6Left-Click: &eModify"), 3, guiClick));
        this.addButton(this.createButton("return", ContentType.RETURN, Material.BARRIER, "&c&lReturn", List.of(), 8, guiClick));
    }

    private void sendSetMessage(ItemType itemType, String currentValue) {
        this.listening = itemType;
        player.closeInventory();
        String name;
        switch (itemType) {
            case MINIMUM: {
                name = "minimum "+this.itemType.getTitle();
                break;
            }
            case MAXIMUM: {
                name = "maximum "+this.itemType.getTitle();
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
        int value;
        try {
            value = Integer.parseInt(message);
        } catch (NumberFormatException e) {
            plugin.lang().ItemGenerator_Cmd_Editor_Error_InvalidInput.replace("%input%", message).replace("%value%", "integer").send(player);
            saveAndReopen();
            return;
        }
        this.itemGenerator.getConfig().set(itemType.getPath(this.itemType), value);
        saveAndReopen();
    }

    public enum ItemType {
        MINIMUM("minimum"),
        MAXIMUM("maximum"),
        LORE("lore-format"),
        LIST("list"),
        ;

        private final String path;

        ItemType(String path) { this.path = path; }

        public String getPath() { return path; }

        public String getPath(EditorGUI.ItemType itemType) { return itemType.getPath() + '.' +path; }
    }
}
