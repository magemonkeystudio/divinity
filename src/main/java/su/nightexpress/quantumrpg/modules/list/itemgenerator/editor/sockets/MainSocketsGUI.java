package su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.sockets;

import mc.promcteam.engine.manager.api.gui.ContentType;
import mc.promcteam.engine.manager.api.gui.GuiClick;
import mc.promcteam.engine.manager.api.gui.GuiItem;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.ItemGeneratorManager;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.AbstractEditorGUI;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.EditorGUI;

import java.util.ArrayList;
import java.util.List;

public class MainSocketsGUI extends AbstractEditorGUI {
    public MainSocketsGUI(@NotNull ItemGeneratorManager itemGeneratorManager, ItemGeneratorManager.GeneratorItem itemGenerator) {
        super(itemGeneratorManager, itemGenerator, 54);
        setTitle("[&d"+itemGenerator.getId()+"&r] editor/"+EditorGUI.ItemType.SOCKETS.getTitle());
    }

    @Override
    protected void onCreate(@NotNull Player player, @NotNull Inventory inventory, int page) {
        ConfigurationSection cfg = this.itemGenerator.getConfig().getConfigurationSection(EditorGUI.ItemType.SOCKETS.getPath());
        List<String> list = new ArrayList<>();
        if (cfg != null) {
            list.addAll(cfg.getKeys(false));
        }
        int totalPages = Math.max((int) Math.ceil(list.size()*1.0/42), 1);
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
                        new EditorGUI(itemGeneratorManager, itemGenerator).open(player1, 1);
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
                new SocketGUI(this.itemGeneratorManager, this.itemGenerator, guiItem.getId()).open(player1, 1);
            }
        };
        for (int socketIndex = (currentPage-1)*42, last = Math.min(list.size(), socketIndex+42), invIndex = 1;
             socketIndex < last; socketIndex++, invIndex++) {
            if ((invIndex)%9 == 8) { invIndex += 2; }
            String entry = list.get(socketIndex);
            this.addButton(this.createButton(entry, EditorGUI.ItemType.SOCKETS, Material.EMERALD,
                                             "&e"+entry, List.of(
                                                     "&6Left-Click: &eModify"), invIndex, guiClick));
        }
        this.addButton(this.createButton("prev-page", ContentType.BACK, Material.ENDER_PEARL, "&dPrevious Page", List.of(), 0, guiClick));
        this.addButton(this.createButton("next-page", ContentType.NEXT, Material.ENDER_PEARL, "&dNext Page", List.of(), 8, guiClick));
        this.addButton(this.createButton("return", ContentType.RETURN, Material.BARRIER, "&c&lReturn", List.of(), 53, guiClick));
    }
}
