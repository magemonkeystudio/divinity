package su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.stats;

import mc.promcteam.engine.manager.api.menu.Slot;
import mc.promcteam.engine.config.api.JYML;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.hooks.EHook;
import su.nightexpress.quantumrpg.hooks.external.SkillAPIHK;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.AbstractEditorGUI;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.EditorGUI;

import java.util.ArrayList;
import java.util.List;

public class StatListGUI extends AbstractEditorGUI {
    private final EditorGUI.ItemType itemType;

    public StatListGUI(Player player, ItemGeneratorReference itemGenerator, EditorGUI.ItemType itemType) {
        super(player, 6, "[&d" + itemGenerator.getId() + "&r] editor/" + itemType.getTitle(), itemGenerator);
        this.itemType = itemType;
    }

    @Override
    public void setContents() {
        JYML         cfg  = itemGenerator.getConfig();
        List<String> list = new ArrayList<>();
        ConfigurationSection section = cfg.getConfigurationSection(MainStatsGUI.ItemType.LIST.getPath(this.itemType));
        if (section != null) {
            list.addAll(section.getKeys(false));
        }
        int i = 0;
        for (String entry : list) {
            i++;
            if (i % this.inventory.getSize() == 53) {
                this.setSlot(i, getNextButton());
                i++;
            } else if (i % 9 == 8) {i++;}
            if (i % this.inventory.getSize() == 45) {
                this.setSlot(i, getPrevButton());
                i++;
            } else if (i % 9 == 0) {i++;}

            Material material        = null;
            Integer  customModelData = null;
            switch (this.itemType) {
                case DAMAGE_TYPES: {
                    material = Material.IRON_SWORD;
                    break;
                }
                case DEFENSE_TYPES: {
                    material = Material.IRON_CHESTPLATE;
                    break;
                }
                case SKILLAPI_ATTRIBUTES: {
                    SkillAPIHK skillAPIHK = (SkillAPIHK) QuantumRPG.getInstance().getHook(EHook.SKILL_API);
                    if (skillAPIHK != null) {
                        ItemStack indicator = skillAPIHK.getAttributeIndicator(entry);
                        material = indicator.getType();
                        ItemMeta meta = indicator.getItemMeta();
                        if (meta != null && meta.hasCustomModelData()) {
                            customModelData = meta.getCustomModelData();
                        }
                    }
                    break;
                }
            }
            if (material == null) {material = Material.PAPER;}
            String path = MainStatsGUI.ItemType.LIST.getPath(this.itemType) + '.' + entry + '.';
            String roundDisplay = this.itemType == EditorGUI.ItemType.SKILLAPI_ATTRIBUTES
                    ? ""
                    : "&bRound: &a" + cfg.getBoolean(path + "round", false);

            ItemStack itemStack = createItem(material,
                    "&e" + entry,
                    "&bCurrent:",
                    "&bChance: &a" + cfg.getDouble(path + "chance"),
                    "&bScale by level: &a" + cfg.getDouble(path + "scale-by-level"),
                    "&bMinimum value: &a" + cfg.getDouble(path + "min"),
                    "&bMaximum value: &a" + cfg.getDouble(path + "max"),
                    "&bFlat range: &a" + cfg.getBoolean(path + "flat-range"),
                    roundDisplay,
                    "",
                    "&6Left-Click: &eModify");
            if (customModelData != null) {
                ItemMeta meta = itemStack.getItemMeta();
                if (meta != null) {
                    meta.setCustomModelData(customModelData);
                    itemStack.setItemMeta(meta);
                }
            }
            setSlot(i, new Slot(itemStack) {
                @Override
                public void onLeftClick() {
                    openSubMenu(new StatGUI(player, itemGenerator, itemType, MainStatsGUI.ItemType.LIST.getPath(itemType) + '.' + entry));
                }
            });

        }
        this.setSlot(this.getPages() * this.inventory.getSize() - 9, getPrevButton());
        this.setSlot(this.getPages() * this.inventory.getSize() - 1, getNextButton());
    }
}
