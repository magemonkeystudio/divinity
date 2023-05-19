package su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.materials;

import mc.promcteam.engine.config.api.JYML;
import mc.promcteam.engine.manager.api.gui.ContentType;
import mc.promcteam.engine.manager.api.gui.GuiClick;
import mc.promcteam.engine.manager.api.gui.GuiItem;
import mc.promcteam.engine.utils.StringUT;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.ItemGeneratorManager;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.AbstractEditorGUI;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.EditorGUI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainModelDataGUI extends AbstractEditorGUI {
    private boolean listening = false;

    public MainModelDataGUI(@NotNull ItemGeneratorManager itemGeneratorManager, ItemGeneratorManager.GeneratorItem itemGenerator) {
        super(itemGeneratorManager, itemGenerator, 54);
        setTitle("[&d"+itemGenerator.getId()+"&r] editor/"+EditorGUI.ItemType.MATERIALS.getTitle());
    }

    @Override
    protected void onCreate(@NotNull Player player, @NotNull Inventory inventory, int page) {
        JYML cfg = this.itemGenerator.getConfig();
        Map<String,List<Integer>> map = new HashMap<>();
        List<String> list = new ArrayList<>();
        map.put("default", cfg.getIntegerList(MainMaterialsGUI.ItemType.MODEL_DATA.getPath()+".default"));
        list.add("default");
        ConfigurationSection modelDataCfg = cfg.getConfigurationSection(MainMaterialsGUI.ItemType.MODEL_DATA.getPath()+".special");
        if (modelDataCfg != null) {
            for (String key : modelDataCfg.getKeys(false)) {
                String path = "special."+key;
                map.put(path, modelDataCfg.getIntegerList(key));
                list.add(path);
            }
        }
        list.add(null);
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
            if (type == MainMaterialsGUI.ItemType.MODEL_DATA) {
                GuiItem guiItem = this.getButton(player1, clickEvent.getSlot());
                if (guiItem == null) { return; }
                String id = guiItem.getId();
                switch (clickEvent.getClick()) {
                    case DROP: case CONTROL_DROP: {
                        if (id.equals("default")) { return; }
                        map.remove(id);
                        setModelData(map);
                        saveAndReopen(currentPage);
                        break;
                    }
                    default: {
                        new ModelDataGUI(itemGeneratorManager, itemGenerator, id).open(player1, 1);
                        break;
                    }
                }
            } else if (type == ItemType.NEW) {
                sendCreateMessage();
            }
        };
        for (int materialIndex = (currentPage-1)*42, last = Math.min(list.size(), materialIndex+42), invIndex = 1;
             materialIndex < last; materialIndex++, invIndex++) {
            if ((invIndex)%9 == 8) { invIndex += 2; }
            String path = list.get(materialIndex);
            if (path == null) {
                this.addButton(this.createButton("new", ItemType.NEW, Material.REDSTONE, "&eAdd new material", List.of(), invIndex, guiClick));
            } else if (path.equals("default")) {
                List<String> stringList = new ArrayList<>();
                for (int cmd : map.get(path)) { stringList.add(String.valueOf(cmd)); }
                this.addButton(this.createButton(path, MainMaterialsGUI.ItemType.MODEL_DATA, Material.STONE,
                                                 "&e"+path, StringUT.replace(color(List.of(
                                                         "&bCurrent:",
                                                         "&a%current%",
                                                         "&6Left-Click: &eModify")), CURRENT_PLACEHOLDER, stringList), invIndex, guiClick));
            } else {
                String id = path.replace("special.", "");
                List<String> stringList = new ArrayList<>();
                for (int cmd : map.get(path)) { stringList.add(String.valueOf(cmd)); }
                this.addButton(this.createButton(path, MainMaterialsGUI.ItemType.MODEL_DATA, MainMaterialsGUI.getMaterialGroup(id),
                                                 "&e"+id, StringUT.replace(color(List.of(
                                                         "&bCurrent:",
                                                         "&a%current%",
                                                         "&6Left-Click: &eModify",
                                                         "&6Drop: &eRemove")), CURRENT_PLACEHOLDER, stringList), invIndex, guiClick));
            }
        }
        this.addButton(this.createButton("prev-page", ContentType.BACK, Material.ENDER_PEARL, "&dPrevious Page", List.of(), 0, guiClick));
        this.addButton(this.createButton("next-page", ContentType.NEXT, Material.ENDER_PEARL, "&dNext Page", List.of(), 8, guiClick));
        this.addButton(this.createButton("return", ContentType.RETURN, Material.BARRIER, "&c&lReturn", List.of(), 53, guiClick));
    }

    private void setModelData(Map<String,List<Integer>> modelData) {
        JYML cfg = this.itemGenerator.getConfig();
        String path = MainMaterialsGUI.ItemType.MODEL_DATA.getPath();
        cfg.remove(path);
        for (Map.Entry<String,List<Integer>> entry : modelData.entrySet()) {
            cfg.set(path+'.'+entry.getKey(), entry.getValue());
        }
    }

    private void sendCreateMessage() {
        this.listening = true;
        this.player.closeInventory();
        player.sendMessage("▸ Enter the desired material for the model data, or \"cancel\" to go back");
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
        JYML cfg = this.itemGenerator.getConfig();
        cfg.set(MainMaterialsGUI.ItemType.MODEL_DATA.getPath()+".special."+message, List.of());
        saveAndReopen(getUserPage(this.player, 0));
    }
}
