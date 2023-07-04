package su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.enchantments;

import mc.promcteam.engine.manager.api.gui.ContentType;
import mc.promcteam.engine.manager.api.gui.GuiClick;
import mc.promcteam.engine.manager.api.gui.GuiItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.ItemGeneratorManager;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.AbstractEditorGUI;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.EditorGUI;

import java.util.List;

public class NewEnchantmentGUI extends AbstractEditorGUI {
    private final List<String> list;
    private String listening;

    public NewEnchantmentGUI(@NotNull ItemGeneratorManager itemGeneratorManager, ItemGeneratorManager.GeneratorItem itemGenerator, List<String> missingList) {
        super(itemGeneratorManager, itemGenerator, 54);
        this.list = missingList;
    }

    @Override
    protected void onCreate(@NotNull Player player, @NotNull Inventory inventory, int page) {
        int totalPages = Math.max((int) Math.ceil(this.list.size()*1.0/42), 1);
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
                        new EnchantmentListGUI(itemGeneratorManager, itemGenerator).open(player1, 1);
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
            if (type.equals(ItemType.NEW)) {
                GuiItem guiItem = this.getButton(player, clickEvent.getSlot());
                if (guiItem == null) { return; }
                sendSetMessage(guiItem.getId());
            }
        };
        for (int enchantmentIndex = (currentPage-1)*42, last = Math.min(this.list.size(), enchantmentIndex+42), invIndex = 1;
             enchantmentIndex < last; enchantmentIndex++, invIndex++) {
            if ((invIndex)%9 == 8) { invIndex += 2; }
            String key = this.list.get(enchantmentIndex);
            this.addButton(this.createButton(key, ItemType.NEW, Material.ENCHANTED_BOOK,
                                             "&e"+key, List.of(
                                                   "&6Left-Click: &eSet"), invIndex, guiClick));
        }
        this.addButton(this.createButton("prev-page", ContentType.BACK, Material.ENDER_PEARL, "&dPrevious Page", List.of(), 0, guiClick));
        this.addButton(this.createButton("next-page", ContentType.NEXT, Material.ENDER_PEARL, "&dNext Page", List.of(), 8, guiClick));
        this.addButton(this.createButton("return", ContentType.RETURN, Material.BARRIER, "&c&lReturn", List.of(), 53, guiClick));
    }

    private void sendSetMessage(String key) {
        this.listening = key;
        this.player.closeInventory();
        player.sendMessage("▸ Enter the desired level range, or \"cancel\" to go back");
    }

    @Override
    public void onChat(AsyncPlayerChatEvent event) {
        if (this.listening == null) { return; }
        event.setCancelled(true);
        String key = this.listening;
        this.listening = null;
        String message = event.getMessage().strip();
        try {
            String[] strings = message.split(":");
            if (strings.length > 2) { throw new IllegalArgumentException(); }
            for (String string : strings) { Integer.parseInt(string); }
        } catch (IllegalArgumentException e) {
            plugin.lang().ItemGenerator_Cmd_Editor_Error_InvalidInput.replace("%input%", message).replace("%value%", "level range").send(player);
            saveAndReopen(getUserPage(this.player, 0));
            return;
        }
        this.itemGenerator.getConfig().set(EditorGUI.ItemType.ENCHANTMENTS.getPath()+".list."+key, message);
        saveAndReopen();
        new BukkitRunnable() {
            @Override
            public void run() {
                new EnchantmentListGUI(itemGeneratorManager, itemGenerator).open(player, 1);
            }
        }.runTask(plugin);
    }
}
