package su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.sockets;

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
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.ItemGeneratorManager;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.AbstractEditorGUI;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.EditorGUI;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.LoreGUI;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.stats.MainStatsGUI;

import java.util.ArrayList;
import java.util.List;

public class SocketGUI extends AbstractEditorGUI {
    private final String name;
    private MainStatsGUI.ItemType listening;

    public SocketGUI(@NotNull ItemGeneratorManager itemGeneratorManager, ItemGeneratorManager.GeneratorItem itemGenerator, String name) {
        super(itemGeneratorManager, itemGenerator, 9);
        this.name = name;
        setTitle("[&d"+itemGenerator.getId()+"&r] editor/"+EditorGUI.ItemType.SOCKETS.getTitle());
    }

    @Override
    protected void onCreate(@NotNull Player player, @NotNull Inventory inventory, int i) {
        JYML cfg = this.itemGenerator.getConfig();
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
                        new MainSocketsGUI(itemGeneratorManager, itemGenerator).open(player1, 1);
                        break;
                    }
                }
                return;
            }

            if (clazz.equals(MainStatsGUI.ItemType.class)) {
                MainStatsGUI.ItemType type2 = (MainStatsGUI.ItemType) type;
                String path = EditorGUI.ItemType.SOCKETS.getPath()+'.'+this.name+'.'+type2.getPath();
                switch (type2) {
                    case MINIMUM: {
                        switch (clickEvent.getClick()) {
                            case LEFT: {
                                cfg.set(path, Math.max(0, cfg.getInt(path)-1));
                                saveAndReopen();
                                break;
                            }
                            case RIGHT: {
                                cfg.set(path, cfg.getInt(path)+1);
                                saveAndReopen();
                                break;
                            }
                            case DROP: case CONTROL_DROP: {
                                cfg.set(path, 0);
                                saveAndReopen();
                                break;
                            }
                            default: {
                                sendSetMessage(type2, String.valueOf(cfg.getInt(path)));
                                break;
                            }
                        }
                        break;
                    }
                    case MAXIMUM: {
                        switch (clickEvent.getClick()) {
                            case LEFT: {
                                cfg.set(path, Math.max(0, cfg.getInt(path)-1));
                                saveAndReopen();
                                break;
                            }
                            case RIGHT: {
                                cfg.set(path, cfg.getInt(path)+1);
                                saveAndReopen();
                                break;
                            }
                            case DROP: case CONTROL_DROP: {
                                cfg.set(path, 2);
                                saveAndReopen();
                                break;
                            }
                            default: {
                                sendSetMessage(type2, String.valueOf(cfg.getInt(path)));
                                break;
                            }
                        }
                        break;
                    }
                    case LORE: {
                        switch (clickEvent.getClick()) {
                            case DROP: case CONTROL_DROP: {
                                cfg.set(path, StringUT.replace(color(List.of("&8&m               &f  「 %current%S 」  &8&m               ",
                                        "%SOCKET_%current%_DEFAULT%")), CURRENT_PLACEHOLDER, List.of(this.name)));
                                saveAndReopen();
                                break;
                            }
                            default: {
                                new LoreGUI(this.itemGeneratorManager, this.itemGenerator, path, this.getTitle()+" lore", () -> new SocketGUI(this.itemGeneratorManager, this.itemGenerator, this.name).open(player1, 1)).open(player1, 1);
                                break;
                            }
                        }
                        break;
                    }
                    case LIST: {
                        new SocketListGUI(this.itemGeneratorManager, this.itemGenerator, this.name).open(player1, 1);
                        break;
                    }
                }
            }
        };
        String path = EditorGUI.ItemType.SOCKETS.getPath()+'.'+this.name+'.';
        this.addButton(this.createButton("minimum", MainStatsGUI.ItemType.MINIMUM, Material.BROWN_MUSHROOM,
                                         "&eMinimum "+this.name+" sockets", List.of(
                                                 "&bCurrent: &a"+cfg.getInt(path+MainStatsGUI.ItemType.MINIMUM.getPath()),
                                                 "&6Middle-Click: &eSet",
                                                 "&6Left-Click: &eDecrease",
                                                 "&6Right-Click: &eIncrease",
                                                 "&6Drop: &eSet to default value"), 0, guiClick));
        this.addButton(this.createButton("maximum", MainStatsGUI.ItemType.MAXIMUM, Material.RED_MUSHROOM,
                                         "&eMaximum "+this.name+" sockets", List.of(
                                                 "&bCurrent: &a"+cfg.getInt(path+MainStatsGUI.ItemType.MAXIMUM.getPath()),
                                                 "&6Middle-Click: &eSet",
                                                 "&6Left-Click: &eDecrease",
                                                 "&6Right-Click: &eIncrease",
                                                 "&6Drop: &eSet to default value"), 1, guiClick));
        this.addButton(this.createButton("lore", MainStatsGUI.ItemType.LORE, Material.WRITABLE_BOOK,
                                         "&eLore format", StringUT.replace(color(List.of(
                                                 "&bCurrent:",
                                                 "&a----------",
                                                 "&f%current%",
                                                 "&a----------",
                                                 "&6Left-Click: &eModify")), CURRENT_PLACEHOLDER, cfg.getStringList(path+MainStatsGUI.ItemType.LORE.getPath())), 2, guiClick));
        List<String> lore = new ArrayList<>();
        ConfigurationSection listSection = cfg.getConfigurationSection(EditorGUI.ItemType.SOCKETS.getPath()+'.'+this.name+".list");
        if (listSection != null) {
            for (String key : listSection.getKeys(false)) {
                lore.add("&a- "+key+": &f"+listSection.getDouble(key+".chance", 0));
            }
        }
        this.addButton(this.createButton("list", MainStatsGUI.ItemType.LIST, Material.IRON_SWORD,
                                         "&eList of chances per tier", StringUT.replace(color(List.of(
                                                 "&bCurrent:",
                                                 "%current%",
                                                 "&6Left-Click: &eModify",
                                                 "&6Drop: &eSet to default value")), CURRENT_PLACEHOLDER, lore) , 3, guiClick));
        this.addButton(this.createButton("return", ContentType.RETURN, Material.BARRIER, "&c&lReturn", List.of(), 8, guiClick));
    }


    private void sendSetMessage(MainStatsGUI.ItemType itemType, String currentValue) {
        this.listening = itemType;
        player.closeInventory();
        String name = itemType.name().toLowerCase()+' '+this.name+" sockets";
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
        MainStatsGUI.ItemType itemType = this.listening;
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
        this.itemGenerator.getConfig().set(EditorGUI.ItemType.SOCKETS.getPath()+'.'+this.name+'.'+itemType.getPath(), value);
        saveAndReopen();
    }
}
