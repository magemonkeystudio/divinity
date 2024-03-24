package com.promcteam.divinity.modules.list.itemgenerator.editor.skills;

import com.promcteam.codex.config.api.JYML;
import com.promcteam.codex.manager.api.menu.Slot;
import com.promcteam.codex.utils.StringUT;
import com.promcteam.divinity.Divinity;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import com.promcteam.divinity.hooks.EHook;
import com.promcteam.divinity.hooks.external.FabledHook;
import com.promcteam.divinity.modules.list.itemgenerator.editor.AbstractEditorGUI;
import com.promcteam.divinity.modules.list.itemgenerator.editor.EditorGUI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SkillListGUI extends AbstractEditorGUI {

    public SkillListGUI(Player player, ItemGeneratorReference itemGenerator) {
        super(player,
                6,
                "[&d" + itemGenerator.getId() + "&r] editor/" + EditorGUI.ItemType.SKILLS.getTitle(),
                itemGenerator);
    }

    @Override
    public void setContents() {
        List<String>         list    = new ArrayList<>();
        JYML                 cfg     = this.itemGenerator.getConfig();
        ConfigurationSection section = cfg.getConfigurationSection(EditorGUI.ItemType.SKILLS.getPath() + ".list");
        if (section != null) {
            list.addAll(section.getKeys(false));
        }
        List<String> missingList = new ArrayList<>();
        FabledHook   fabledHook  = (FabledHook) Divinity.getInstance().getHook(EHook.SKILL_API);
        if (fabledHook != null) {
            for (String skillId : fabledHook.getSkills()) {
                boolean missing = true;
                for (String key : list) {
                    if (key.equalsIgnoreCase(skillId)) {
                        missing = false;
                        break;
                    }
                }
                if (missing) {
                    missingList.add(skillId);
                }
            }
        }
        Collections.sort(missingList);
        if (!missingList.isEmpty()) {
            list.add(null);
        }
        int i = 0;
        for (String key : list) {
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
            String path = EditorGUI.ItemType.SKILLS.getPath() + ".list." + key;
            if (key == null) {
                setSlot(i, new Slot(createItem(Material.REDSTONE, "&eAdd new skill")) {
                    @Override
                    public void onLeftClick() {
                        openSubMenu(new NewSkillGUI(player, itemGenerator, missingList));
                    }
                });
            } else {
                ItemStack itemStack = createItem(Material.JACK_O_LANTERN,
                        "&e" + key,
                        StringUT.replace(CURRENT_PLACEHOLDER, cfg.getStringList(SkillGUI.ItemType.LORE.getPath(path)),
                                "&bChance: &a" + cfg.getDouble(SkillGUI.ItemType.CHANCE.getPath(path)),
                                "&bMinimum level: &a" + cfg.getInt(SkillGUI.ItemType.MIN.getPath(path)),
                                "&bMaximum level: &a" + cfg.getInt(SkillGUI.ItemType.MAX.getPath(path)),
                                "&bLore format:",
                                "&a----------",
                                "&f%current%",
                                "&a----------",
                                "&6Left-Click: &eModify",
                                "&6Drop: &eRemove")
                );
                if (fabledHook != null) {
                    ItemStack indicator = fabledHook.getSkillIndicator(key);
                    if (indicator != null) {
                        itemStack.setType(indicator.getType());
                        ItemMeta itemMeta = itemStack.getItemMeta();
                        if (itemMeta != null) {
                            ItemMeta indicatorMeta = indicator.getItemMeta();
                            if (indicatorMeta != null && indicatorMeta.hasCustomModelData()) {
                                itemMeta.setCustomModelData(indicatorMeta.getCustomModelData());
                                itemStack.setItemMeta(itemMeta);
                            }
                        }
                    }
                }
                setSlot(i, new Slot(itemStack) {
                    @Override
                    public void onLeftClick() {
                        openSubMenu(new SkillGUI(player, itemGenerator, path));
                    }

                    @Override
                    public void onDrop() {
                        cfg.remove(path);
                        saveAndReopen();
                    }
                });
            }
        }
        if (list.get(list.size() - 1) == null) {
            list.remove(list.size() - 1);
        }
        this.setSlot(this.getPages() * this.inventory.getSize() - 9, getPrevButton());
        this.setSlot(this.getPages() * this.inventory.getSize() - 1, getNextButton());
    }
}
