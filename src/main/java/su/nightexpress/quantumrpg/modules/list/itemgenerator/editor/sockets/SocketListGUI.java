package su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.sockets;

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
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.ItemGeneratorManager;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.AbstractEditorGUI;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.EditorGUI;
import su.nightexpress.quantumrpg.stats.items.ItemStats;
import su.nightexpress.quantumrpg.stats.items.attributes.SocketAttribute;

import java.util.ArrayList;
import java.util.List;

public class SocketListGUI extends AbstractEditorGUI {
    private final String name;
    private final String path;
    private String listening;

    public SocketListGUI(@NotNull ItemGeneratorManager itemGeneratorManager, ItemGeneratorManager.GeneratorItem itemGenerator, String name) {
        super(itemGeneratorManager, itemGenerator, 54);
        this.name = name;
        this.path = EditorGUI.ItemType.SOCKETS.getPath()+'.'+this.name+".list.";
        setTitle("[&d"+itemGenerator.getId()+"&r] editor/"+EditorGUI.ItemType.SOCKETS.getTitle());
    }

    @Override
    protected void onCreate(@NotNull Player player, @NotNull Inventory inventory, int page) {
        List<String> list = new ArrayList<>();
        for (SocketAttribute socketAttribute : ItemStats.getSockets(SocketAttribute.Type.valueOf(this.name))) {
            list.add(socketAttribute.getId());
        }
        int totalPages = Math.max((int) Math.ceil(list.size()*1.0/42), 1);
        final int currentPage = page < 1 ? totalPages : Math.min(page, totalPages);
        this.setUserPage(player, currentPage, totalPages);
        JYML cfg = this.itemGenerator.getConfig();
        GuiClick guiClick = (player1, type, clickEvent) -> {
            this.setUserPage(player1, currentPage, totalPages);
            if (type == null) { return; }
            Class<?> clazz = type.getClass();
            if (clazz.equals(ContentType.class)) {
                ContentType type2 = (ContentType) type;
                switch (type2) {
                    case RETURN:
                        new SocketGUI(this.itemGeneratorManager, this.itemGenerator, this.name).open(player1, 1);
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
            if (type == EditorGUI.ItemType.SOCKETS) {
                GuiItem guiItem = this.getButton(player, clickEvent.getSlot());
                if (guiItem == null) { return; }
                String entry = guiItem.getId();
                sendSetMessage(entry, String.valueOf(cfg.getDouble(this.path+entry+".chance")));
            }
        };
        for (int socketIndex = (currentPage-1)*42, last = Math.min(list.size(), socketIndex+42), invIndex = 1;
             socketIndex < last; socketIndex++, invIndex++) {
            if ((invIndex)%9 == 8) { invIndex += 2; }
            String entry = list.get(socketIndex);
            this.addButton(this.createButton(entry, EditorGUI.ItemType.SOCKETS, Material.EMERALD,
                                             "&e"+entry, List.of(
                                                     "&bCurrent: &a"+cfg.getDouble(this.path+entry+".chance"),
                                                     "&6Left-Click: &eModify"), invIndex, guiClick));
        }
        this.addButton(this.createButton("prev-page", ContentType.BACK, Material.ENDER_PEARL, "&dPrevious Page", List.of(), 0, guiClick));
        this.addButton(this.createButton("next-page", ContentType.NEXT, Material.ENDER_PEARL, "&dNext Page", List.of(), 8, guiClick));
        this.addButton(this.createButton("return", ContentType.RETURN, Material.BARRIER, "&c&lReturn", List.of(), 53, guiClick));
    }

    private void sendSetMessage(String entry, String currentValue) {
        this.listening = entry;
        player.closeInventory();
        player.sendMessage("▸ Enter the desired chance for a "+entry+' '+this.name+" socket, or \"cancel\" to go back");
        BaseComponent component = new TextComponent("[Current chance]");
        component.setColor(ChatColor.GOLD);
        component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, currentValue));
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Enter the current chance to chat")));
        player.spigot().sendMessage(component);
    }

    @Override
    public void onChat(AsyncPlayerChatEvent event) {
        if (this.listening == null) { return; }
        event.setCancelled(true);
        String entry = this.listening;
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
        this.itemGenerator.getConfig().set(this.path+entry+".chance", value);
        saveAndReopen(getUserPage(this.player, 0));
    }
}
