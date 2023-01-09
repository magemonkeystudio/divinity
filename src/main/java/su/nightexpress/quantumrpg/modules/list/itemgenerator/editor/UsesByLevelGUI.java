package su.nightexpress.quantumrpg.modules.list.itemgenerator.editor;

import mc.promcteam.engine.config.api.JYML;
import mc.promcteam.engine.manager.api.gui.ContentType;
import mc.promcteam.engine.manager.api.gui.GuiClick;
import mc.promcteam.engine.manager.api.gui.GuiItem;
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
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.ItemGeneratorManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class UsesByLevelGUI extends AbstractEditorGUI {
    private boolean listening = false;
    private Integer levelListening;

    public UsesByLevelGUI(@NotNull ItemGeneratorManager itemGeneratorManager, ItemGeneratorManager.GeneratorItem itemGenerator) {
        super(itemGeneratorManager, itemGenerator, 54);
        setTitle("[&d"+itemGenerator.getId()+"&r] editor/"+EditorGUI.ItemType.USES_BY_LEVEL.getTitle());
    }

    public static Map<Integer,Integer> getUsesByLevel(JYML cfg) {
        Map<Integer,Integer> map = new TreeMap<>();
        ConfigurationSection configurationSection = cfg.getConfigurationSection(EditorGUI.ItemType.USES_BY_LEVEL.getPath());
        if (configurationSection != null) {
            for (String key : configurationSection.getKeys(false)) {
                try {
                    map.put(Integer.parseInt(key), configurationSection.getInt(key));
                } catch (NumberFormatException ignored) { }
            }
        }
        return map;
    }

    @Override
    protected void onCreate(@NotNull Player player, @NotNull Inventory inventory, int page) {
        Map<Integer,Integer> map = getUsesByLevel(this.itemGenerator.getConfig());
        List<Integer> list = new ArrayList<>(map.keySet());
        list.add(null);
        int totalPages = Math.max((int) Math.ceil(list.size()*1.0/42), 1);
        final int currentPage = page < 1 ? totalPages : Math.min(page, totalPages);
        this.setUserPage(player, currentPage, totalPages);
        GuiClick guiClick = (player1, type, clickEvent) -> {
            this.setUserPage(player, currentPage, totalPages);
            if (type == null) { return; }
            Class<?> clazz = type.getClass();
            if (clazz.equals(ContentType.class)) {
                ContentType type2 = (ContentType) type;
                switch (type2) {
                    case RETURN:
                        new EditorGUI(this.itemGeneratorManager, this.itemGenerator).open(player1, 1);
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
            if (type == EditorGUI.ItemType.USES_BY_LEVEL) {
                GuiItem guiItem = this.getButton(player, clickEvent.getSlot());
                if (guiItem == null) { return; }
                int level = Integer.parseInt(guiItem.getId());
                switch (clickEvent.getClick()) {
                    case DROP: case CONTROL_DROP: {
                        map.remove(level);
                        setUsesByLevel(map);
                        saveAndReopen(currentPage);
                        break;
                    }
                    default: {
                        sendSetMessage(level, String.valueOf(map.get(level)));
                        break;
                    }
                }
            } else if (type == ItemType.NEW) {
                sendCreateMessage();
            }
        };
        for (int levelIndex = (currentPage-1)*42, last = Math.min(list.size(), levelIndex+42), invIndex = 1;
             levelIndex < last; levelIndex++, invIndex++) {
            if ((invIndex)%9 == 8) { invIndex += 2; }
            Integer level = list.get(levelIndex);
            this.addButton(level == null ?
                                   this.createButton("new", ItemType.NEW, Material.REDSTONE, "&eAdd new level", List.of(), invIndex, guiClick) :
                                   this.createButton(String.valueOf(level), EditorGUI.ItemType.USES_BY_LEVEL, Material.CAULDRON,
                                                     "&e"+level, List.of(
                                                             "&bCurrent: &a"+map.get(level),
                                                             "&6Left-Click: &eSet",
                                                             "&6Drop: &eRemove"), invIndex, guiClick));
        }
        this.addButton(this.createButton("prev-page", ContentType.BACK, Material.ENDER_PEARL, "&dPrevious Page", List.of(), 0, guiClick));
        this.addButton(this.createButton("next-page", ContentType.NEXT, Material.ENDER_PEARL, "&dNext Page", List.of(), 8, guiClick));
        this.addButton(this.createButton("return", ContentType.RETURN, Material.BARRIER, "&c&lReturn", List.of(), 53, guiClick));
    }

    private void sendSetMessage(int level, String currentValue) {
        this.listening = true;
        this.levelListening = level;
        this.player.closeInventory();
        player.sendMessage("▸ Enter the desired uses for level "+level+", or \"cancel\" to go back");
        if (currentValue != null) {
            BaseComponent component = new TextComponent("[Current uses]");
            component.setColor(ChatColor.GOLD);
            component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, currentValue));
            component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Enter the current uses to chat")));
            player.spigot().sendMessage(component);
        }
    }

    private void sendCreateMessage() {
        this.listening = true;
        this.player.closeInventory();
        player.sendMessage("▸ Enter the desired level, or \"cancel\" to go back");
    }

    @Override
    public void onChat(AsyncPlayerChatEvent event) {
        if (!this.listening) { return; }
        event.setCancelled(true);
        this.listening = false;
        Integer levelListening = this.levelListening;
        this.levelListening = null;
        String message = event.getMessage().strip();
        if (message.equalsIgnoreCase("cancel")) {
            saveAndReopen(getUserPage(this.player, 0));
            return;
        }
        int integer;
        try {
            integer = Integer.parseInt(message);
        } catch (NumberFormatException e) {
            plugin.lang().ItemGenerator_Cmd_Editor_Error_InvalidInput.replace("%input%", message).replace("%value%", "integer").send(player);
            saveAndReopen(getUserPage(this.player, 0));
            return;
        }
        if (levelListening == null) {
            // Creating new requirement
            new BukkitRunnable() {
                @Override
                public void run() { sendSetMessage(integer, null); }
            }.runTask(plugin);
        } else {
            Map<Integer,Integer> usesByLevel = getUsesByLevel(this.itemGenerator.getConfig());
            usesByLevel.put(levelListening, integer);
            setUsesByLevel(usesByLevel);
            saveAndReopen(getUserPage(this.player, 0));
        }
    }

    protected void setUsesByLevel(Map<Integer,Integer> usesByLevel)  {
        JYML cfg = this.itemGenerator.getConfig();
        String path = EditorGUI.ItemType.USES_BY_LEVEL.getPath();
        cfg.remove(EditorGUI.ItemType.USES_BY_LEVEL.getPath());
        path = path+'.';
        for (Map.Entry<Integer,Integer> entry : usesByLevel.entrySet()) {
            cfg.set(path+entry.getKey(), entry.getValue());
        }
    }
}
