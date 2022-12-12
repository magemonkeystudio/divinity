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

import java.util.Collections;
import java.util.List;

public class ModelDataGUI extends AbstractEditorGUI {
    private final String id;
    private boolean listening = false;

    public ModelDataGUI(@NotNull ItemGeneratorManager itemGeneratorManager, ItemGeneratorManager.GeneratorItem itemGenerator, String material) {
        super(itemGeneratorManager, itemGenerator, 54);
        this.id = material;
        setTitle("[&d"+itemGenerator.getId()+"&r] editor/"+EditorGUI.ItemType.MATERIALS.getTitle());
    }

    @Override
    protected void onCreate(@NotNull Player player, @NotNull Inventory inventory, int page) {
        JYML cfg = this.itemGenerator.getConfig();
        List<Integer> modelList = cfg.getIntegerList(getPath());
        modelList.add(null);
        int totalPages = Math.max((int) Math.ceil(modelList.size()*1.0/42), 1);
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
                        new MainModelDataGUI(itemGeneratorManager, itemGenerator).open(player1, 1);
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
            if (type == MainMaterialsGUI.ItemType.MODEL_DATA) {
                GuiItem guiItem = this.getButton(player1, clickEvent.getSlot());
                if (guiItem == null) { return; }
                modelList.remove(Integer.parseInt(guiItem.getId()));
                setModelData(modelList);
                saveAndReopen(currentPage);
            } else if (type == ItemType.NEW) {
                sendCreateMessage();
            }
        };
        for (int modelIndex = (currentPage-1)*42, last = Math.min(modelList.size(), modelIndex+42), invIndex = 1;
             modelIndex < last; modelIndex++, invIndex++) {
            if ((invIndex)%9 == 8) { invIndex += 2; }
            Integer model = modelList.get(modelIndex);
            if (model == null) {
                this.addButton(this.createButton("new", ItemType.NEW, Material.REDSTONE, "&eAdd new model data", List.of(), invIndex, guiClick));
            } else {
                this.addButton(this.createButton(String.valueOf(modelIndex), MainMaterialsGUI.ItemType.MODEL_DATA, Material.END_CRYSTAL,
                                                 "&e"+model, List.of(
                                                         "&6Left-Click: &eRemove"), invIndex, guiClick));
            }
        }
        modelList.remove(modelList.size()-1);
        this.addButton(this.createButton("prev-page", ContentType.BACK, Material.ENDER_PEARL, "&dPrevious Page", List.of(), 0, guiClick));
        this.addButton(this.createButton("next-page", ContentType.NEXT, Material.ENDER_PEARL, "&dNext Page", List.of(), 8, guiClick));
        this.addButton(this.createButton("return", ContentType.RETURN, Material.BARRIER, "&c&lReturn", List.of(), 53, guiClick));
    }

    private String getPath() { return MainMaterialsGUI.ItemType.MODEL_DATA.getPath()+'.'+this.id; }

    private void setModelData(List<Integer> modelData) {
        JYML cfg = this.itemGenerator.getConfig();
        cfg.set(getPath(), modelData);
    }

    private void sendCreateMessage() {
        this.listening = true;
        this.player.closeInventory();
        player.sendMessage("▸ Enter the desired model data, or \"cancel\" to go back");
    }

    @Override
    public void onChat(AsyncPlayerChatEvent event) {
        if (!this.listening) { return; }
        event.setCancelled(true);
        this.listening = false;
        String message = event.getMessage().strip();
        if (message.equalsIgnoreCase("cancel")) {
            saveAndReopen(getUserPage(this.player, 0));
            return;
        }
        try {
            int model = Integer.parseInt(message);
            JYML cfg = this.itemGenerator.getConfig();
            String path = getPath();
            List<Integer> models = cfg.getIntegerList(path);
            models.add(model);
            Collections.sort(models);
            cfg.set(path, models);
            saveAndReopen(getUserPage(this.player, 0));
        } catch (NumberFormatException e) {
            plugin.lang().ItemGenerator_Cmd_Editor_Error_InvalidInput.replace("%input%", message).replace("%value%", "model id").send(player);
        }
    }
}
