package su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.skills;

import mc.promcteam.engine.config.api.JYML;
import mc.promcteam.engine.manager.api.gui.ContentType;
import mc.promcteam.engine.manager.api.gui.GuiClick;
import mc.promcteam.engine.manager.api.gui.GuiItem;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.hooks.EHook;
import su.nightexpress.quantumrpg.hooks.external.SkillAPIHK;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.ItemGeneratorManager;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.AbstractEditorGUI;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.EditorGUI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SkillListGUI extends AbstractEditorGUI {

    public SkillListGUI(@NotNull ItemGeneratorManager itemGeneratorManager, ItemGeneratorManager.GeneratorItem itemGenerator) {
        super(itemGeneratorManager, itemGenerator, 54);
        setTitle("[&d"+itemGenerator.getId()+"&r] editor/"+EditorGUI.ItemType.SKILLS.getTitle());
    }

    @Override
    protected void onCreate(@NotNull Player player, @NotNull Inventory inventory, int page) {
        List<String> list = new ArrayList<>();
        JYML cfg = this.itemGenerator.getConfig();
        ConfigurationSection section = cfg.getConfigurationSection(EditorGUI.ItemType.SKILLS.getPath()+".list");
        if (section != null) {
            list.addAll(section.getKeys(false));
        }
        List<String> missingList = new ArrayList<>();
        SkillAPIHK skillAPIHK = (SkillAPIHK) QuantumRPG.getInstance().getHook(EHook.SKILL_API);
        if (skillAPIHK != null) {
            for (String skillId : skillAPIHK.getSkills()) {
                boolean missing = true;
                for (String key : list) {
                    if (key.equalsIgnoreCase(skillId)) {
                        missing = false;
                        break;
                    }
                }
                if (missing) { missingList.add(skillId); }
            }
        }
        Collections.sort(missingList);
        if (!missingList.isEmpty()) { list.add(null); }

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
                        new MainSkillsGUI(itemGeneratorManager, itemGenerator).open(player1, 1);
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
            if (type.equals(EditorGUI.ItemType.SKILLS)) {
                GuiItem guiItem = this.getButton(player, clickEvent.getSlot());
                if (guiItem == null) { return; }
                String key = guiItem.getId();
                String path = EditorGUI.ItemType.SKILLS.getPath()+".list."+key;
                switch (clickEvent.getClick()) {
                    case DROP: case CONTROL_DROP: {
                        cfg.remove(path);
                        saveAndReopen(currentPage);
                        break;
                    }
                    default: {
                        new SkillGUI(this.itemGeneratorManager, this.itemGenerator, path).open(player1, 1);
                        break;
                    }
                }
            } else if (type == ItemType.NEW) {
                new NewSkillGUI(this.itemGeneratorManager, this.itemGenerator, missingList).open(player1, 1);
            }
        };
        for (int skillIndex = (currentPage-1)*42, last = Math.min(list.size(), skillIndex+42), invIndex = 1;
             skillIndex < last; skillIndex++, invIndex++) {
            if ((invIndex)%9 == 8) { invIndex += 2; }
            String key = list.get(skillIndex);
            if (key == null) {
                this.addButton(this.createButton("new", ItemType.NEW, Material.REDSTONE, "&eAdd new skill", List.of(), invIndex, guiClick));
            } else {
                String path = EditorGUI.ItemType.SKILLS.getPath()+".list."+key;
                GuiItem guiItem = this.createButton(key, EditorGUI.ItemType.SKILLS, Material.JACK_O_LANTERN,
                                                    "&e"+key, replaceLore(List.of(
                                                            "&bChance: &a"+cfg.getDouble(SkillGUI.ItemType.CHANCE.getPath(path)),
                                                            "&bMinimum level: &a"+cfg.getInt(SkillGUI.ItemType.MIN.getPath(path)),
                                                            "&bMaximum level: &a"+cfg.getInt(SkillGUI.ItemType.MAX.getPath(path)),
                                                            "&bLore format:",
                                                            "&a----------",
                                                            "&f%current%",
                                                            "&a----------",
                                                            "&6Left-Click: &eModify",
                                                            "&6Drop: &eRemove"), cfg.getStringList(SkillGUI.ItemType.LORE.getPath(path))), invIndex, guiClick);
                if (skillAPIHK != null) {
                    ItemStack indicator = skillAPIHK.getIndicator(key);
                    if (indicator != null) {
                        ItemStack itemStack = guiItem.getItemRaw();
                        itemStack.setType(indicator.getType());
                        ItemMeta itemMeta = itemStack.getItemMeta();
                        if (itemMeta != null) {
                            ItemMeta indicatorMeta = indicator.getItemMeta();
                            if (indicatorMeta != null && indicatorMeta.hasCustomModelData()) {
                                itemMeta.setCustomModelData(indicatorMeta.getCustomModelData());
                                itemStack.setItemMeta(itemMeta);
                            }
                        }
                        guiItem.setItem(itemStack);
                    }
                }
                this.addButton(guiItem);
            }
        }
        if (list.get(list.size()-1) == null) { list.remove(list.size()-1); }
        this.addButton(this.createButton("prev-page", ContentType.BACK, Material.ENDER_PEARL, "&dPrevious Page", List.of(), 0, guiClick));
        this.addButton(this.createButton("next-page", ContentType.NEXT, Material.ENDER_PEARL, "&dNext Page", List.of(), 8, guiClick));
        this.addButton(this.createButton("return", ContentType.RETURN, Material.BARRIER, "&c&lReturn", List.of(), 53, guiClick));
    }
}
