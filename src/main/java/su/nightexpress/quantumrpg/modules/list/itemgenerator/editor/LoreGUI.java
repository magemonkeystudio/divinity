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
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.ItemGeneratorManager;

import java.util.ArrayList;
import java.util.List;

public class LoreGUI extends AbstractEditorGUI {
    private final String path;
    private List<String> lore;
    private Integer listening = null;
    private final Runnable onReturn;

    public LoreGUI(@NotNull ItemGeneratorManager itemGeneratorManager, ItemGeneratorManager.GeneratorItem itemGenerator, String path, String title, Runnable onReturn) {
        super(itemGeneratorManager, itemGenerator, 54);
        this.path = path;
        this.onReturn = onReturn;
        setTitle(title);
    }

    @Override
    protected void onCreate(@NotNull Player player, @NotNull Inventory inventory, int page) {
        JYML cfg = this.itemGenerator.getConfig();
        this.lore = new ArrayList<>(cfg.getStringList(path));
        this.lore.add(null);
        int totalPages = Math.max((int) Math.ceil(this.lore.size()*1.0/42), 1);
        final int currentPage = page < 1 ? totalPages : Math.min(page, totalPages);
        this.setUserPage(player, currentPage, totalPages);
        GuiClick guiClick = (player1, type, clickEvent) -> {
            this.setUserPage(player1, currentPage, totalPages);
            if (type == null) { return; }
            Class<?> clazz = type.getClass();
            if (clazz.equals(ContentType.class)) {
                ContentType type2 = (ContentType) type;
                switch (type2) {
                    case RETURN:
                        if (this.onReturn == null) {
                            player1.closeInventory();
                        } else {
                            this.onReturn.run();
                        }
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
            if (type == EditorGUI.ItemType.LORE) {
                GuiItem guiItem = this.getButton(player1, clickEvent.getSlot());
                if (guiItem == null) { return; }
                int loreIndex = Integer.parseInt(guiItem.getId());
                switch (clickEvent.getClick()) {
                    case DROP: case CONTROL_DROP: {
                        this.lore.remove(loreIndex);
                        setLore();
                        saveAndReopen(currentPage);
                        break;
                    }
                    case LEFT: {
                        this.lore.add(loreIndex, "");
                        sendSetMessage(loreIndex, null);
                        break;
                    }
                    case RIGHT: {
                        this.lore.add(loreIndex+1, "");
                        sendSetMessage(loreIndex+1, null);
                        break;
                    }
                    default: {
                        sendSetMessage(loreIndex, this.lore.get(loreIndex));
                        break;
                    }
                }
            } else if (type == ItemType.NEW) {
                this.lore.add("");
                sendSetMessage(this.lore.size()-1, null);
            }
        };
        for (int loreIndex = (currentPage-1)*42, last = Math.min(lore.size(), loreIndex+42), invIndex = 1;
             loreIndex < last; loreIndex++, invIndex++) {
            if ((invIndex)%9 == 8) { invIndex += 2; }
            String loreLine = lore.get(loreIndex);
            this.addButton(loreLine == null ?
                                   this.createButton("new", ItemType.NEW, Material.REDSTONE, "&eAdd new lore line", List.of(), invIndex, guiClick) :
                                   this.createButton(String.valueOf(loreIndex), EditorGUI.ItemType.LORE, Material.BOOK,
                                                     "".equals(loreLine) ? "''" : loreLine, List.of(
                                                   "&6Left-Click: &eAdd to left",
                                                   "&6Right-Click: &eAdd to right",
                                                   "&6Middle-Click: &eSet",
                                                   "&6Drop: &eRemove"), invIndex, guiClick));
        }
        lore.remove(lore.size()-1);
        this.addButton(this.createButton("prev-page", ContentType.BACK, Material.ENDER_PEARL, "&dPrevious Page", List.of(), 0, guiClick));
        this.addButton(this.createButton("next-page", ContentType.NEXT, Material.ENDER_PEARL, "&dNext Page", List.of(), 8, guiClick));
        this.addButton(this.createButton("return", ContentType.RETURN, Material.BARRIER, "&c&lReturn", List.of(), 53, guiClick));
    }

    private void setLore() {
        this.itemGenerator.getConfig().set(path, this.lore);
    }

    private void sendSetMessage(int index, String currentValue) {
        this.listening = index;
        this.player.closeInventory();
        player.sendMessage("▸ Enter the desired value for lore line "+index+", or \"cancel\" to go back");
        if (currentValue != null) {
            BaseComponent component = new TextComponent("[Current value]");
            component.setColor(ChatColor.GOLD);
            component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, currentValue));
            component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Enter the current value to chat")));
            player.spigot().sendMessage(component);
        }
    }

    @Override
    public void onChat(AsyncPlayerChatEvent event) {
        if (this.listening == null) { return; }
        event.setCancelled(true);
        int index = this.listening;
        this.listening = null;
        String message = event.getMessage();
        if (message.strip().equalsIgnoreCase("cancel")) {
            saveAndReopen(getUserPage(this.player, 0));
            return;
        }
        lore.set(index, message);
        setLore();
        saveAndReopen(getUserPage(this.player, 0));
    }
}
