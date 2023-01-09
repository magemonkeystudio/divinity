package su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.materials;

import mc.promcteam.engine.config.api.JYML;
import mc.promcteam.engine.manager.api.gui.ContentType;
import mc.promcteam.engine.manager.api.gui.GuiClick;
import mc.promcteam.engine.manager.api.gui.GuiItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.ItemGeneratorManager;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.AbstractEditorGUI;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.EditorGUI;

import java.util.ArrayList;
import java.util.List;

public class MaterialListGUI extends AbstractEditorGUI {
    private boolean listening = false;

    public MaterialListGUI(@NotNull ItemGeneratorManager itemGeneratorManager, ItemGeneratorManager.GeneratorItem itemGenerator) {
        super(itemGeneratorManager, itemGenerator, 54);
        setTitle("[&d"+itemGenerator.getId()+"&r] editor/"+EditorGUI.ItemType.MATERIALS.getTitle());
    }

    @Override
    protected void onCreate(@NotNull Player player, @NotNull Inventory inventory, int page) {
        JYML cfg = this.itemGenerator.getConfig();
        List<String> materialList = new ArrayList<>(cfg.getStringList(MainMaterialsGUI.ItemType.LIST.getPath()));
        materialList.add(null);
        int totalPages = Math.max((int) Math.ceil(materialList.size()*1.0/42), 1);
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
                        new MainMaterialsGUI(itemGeneratorManager, itemGenerator).open(player1, 1);
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
            if (type == MainMaterialsGUI.ItemType.LIST) {
                GuiItem guiItem = this.getButton(player, clickEvent.getSlot());
                if (guiItem == null) { return; }
                materialList.remove(guiItem.getId());
                cfg.set(MainMaterialsGUI.ItemType.LIST.getPath(), materialList);
                saveAndReopen(currentPage);
            } else if (type == ItemType.NEW) {
                this.listening = true;
                this.player.closeInventory();
                player.sendMessage("▸ Enter the desired material, or \"cancel\" to go back");
            }
        };
        for (int materialIndex = (currentPage-1)*42, last = Math.min(materialList.size(), materialIndex+42), invIndex = 1;
             materialIndex < last; materialIndex++, invIndex++) {
            if ((invIndex)%9 == 8) { invIndex += 2; }
            String entry = materialList.get(materialIndex);
            this.addButton(entry == null ?
                                   this.createButton("new", ItemType.NEW, Material.REDSTONE, "&eAdd new material", List.of(), invIndex, guiClick) :
                                   this.createButton(entry, MainMaterialsGUI.ItemType.LIST, MainMaterialsGUI.getMaterial(entry),
                                                     "&e"+entry, List.of(
                                                            "&6Left-Click: &eRemove"), invIndex, guiClick));
        }
        materialList.remove(materialList.size()-1);
        this.addButton(this.createButton("prev-page", ContentType.BACK, Material.ENDER_PEARL, "&dPrevious Page", List.of(), 0, guiClick));
        this.addButton(this.createButton("next-page", ContentType.NEXT, Material.ENDER_PEARL, "&dNext Page", List.of(), 8, guiClick));
        this.addButton(this.createButton("return", ContentType.RETURN, Material.BARRIER, "&c&lReturn", List.of(), 53, guiClick));
    }

    @Override
    public void onChat(AsyncPlayerChatEvent event) {
        if (!this.listening) { return; }
        event.setCancelled(true);
        this.listening = false;
        String message = event.getMessage().strip().toUpperCase();
        JYML cfg = this.itemGenerator.getConfig();
        List<String> materials = this.itemGenerator.getConfig().getStringList(MainMaterialsGUI.ItemType.LIST.getPath());
        materials.add(message);
        cfg.set(MainMaterialsGUI.ItemType.LIST.getPath(), materials);
        saveAndReopen(getUserPage(this.player, 0));
    }
}
