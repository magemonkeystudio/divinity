package com.promcteam.divinity.modules.list.itemgenerator.editor.materials;

import com.promcteam.codex.config.api.JYML;
import com.promcteam.codex.manager.api.menu.Slot;
import com.promcteam.divinity.modules.list.itemgenerator.editor.AbstractEditorGUI;
import com.promcteam.divinity.modules.list.itemgenerator.editor.EditorGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class ModelDataGUI extends AbstractEditorGUI {
    private final String id;

    public ModelDataGUI(Player player, ItemGeneratorReference itemGenerator, String material) {
        super(player,
                6,
                "[&d" + itemGenerator.getId() + "&r] editor/" + EditorGUI.ItemType.MATERIALS.getTitle(),
                itemGenerator);
        this.id = material;
    }

    @Override
    public void setContents() {
        JYML          cfg       = this.itemGenerator.getConfig();
        String        path      = MainMaterialsGUI.ItemType.MODEL_DATA.getPath() + '.' + this.id;
        List<Integer> modelList = cfg.getIntegerList(path);
        modelList.add(null);
        int i = 0;
        for (Integer model : modelList) {
            i++;
            if (i % this.inventory.getSize() == 53) {
                this.setSlot(i, getNextButton());
                i++;
            } else if (i % 9 == 8) {
                i++;
            }
            if (i % this.inventory.getSize() == 45) {
                this.setSlot(i, getPrevButton());
                i++;
            } else if (i % 9 == 0) {
                i++;
            }
            setSlot(i, model == null ?
                    new Slot(createItem(Material.REDSTONE, "&eAdd new model data")) {
                        @Override
                        public void onLeftClick() {
                            sendSetMessage("model data",
                                    null,
                                    s -> {
                                        modelList.add(Integer.parseInt(s));
                                        Collections.sort(modelList);
                                        cfg.set(path, modelList);
                                        saveAndReopen();
                                    });
                        }
                    } :
                    new Slot(createItem(Material.END_CRYSTAL,
                            "&e" + model,
                            "&6Left-Click: &eRemove")) {
                        @Override
                        public void onLeftClick() {
                            modelList.remove(model);
                            cfg.set(path, modelList);
                            saveAndReopen();
                        }
                    });
        }
        modelList.remove(modelList.size() - 1);
        this.setSlot(this.getPages() * this.inventory.getSize() - 9, getPrevButton());
        this.setSlot(this.getPages() * this.inventory.getSize() - 1, getNextButton());
    }
}
