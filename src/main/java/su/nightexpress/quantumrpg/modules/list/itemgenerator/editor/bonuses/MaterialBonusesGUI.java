package su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.bonuses;

import mc.promcteam.engine.config.api.JYML;
import mc.promcteam.engine.manager.api.menu.Slot;
import mc.promcteam.engine.utils.StringUT;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.AbstractEditorGUI;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.EditorGUI;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.materials.MainMaterialsGUI;
import su.nightexpress.quantumrpg.stats.items.ItemStats;
import su.nightexpress.quantumrpg.stats.items.attributes.DamageAttribute;
import su.nightexpress.quantumrpg.stats.items.attributes.DefenseAttribute;
import su.nightexpress.quantumrpg.stats.items.attributes.api.AbstractStat;

import java.util.*;

public class MaterialBonusesGUI extends AbstractEditorGUI {

    public MaterialBonusesGUI(Player player, ItemGeneratorReference itemGenerator) {
        super(player, 6, "[&d" + itemGenerator.getId() + "&r] editor/" + EditorGUI.ItemType.BONUSES.getTitle(), itemGenerator);
    }

    @Override
    public void setContents() {
        JYML                      cfg                 = this.itemGenerator.getConfig();
        Map<String, List<String>> map                 = new HashMap<>();
        List<String>              list                = new ArrayList<>();
        ConfigurationSection      bonusesSection = cfg.getConfigurationSection(MainBonusesGUI.ItemType.MATERIAL.getPath());
        if (bonusesSection != null) {
            for (String key : bonusesSection.getKeys(false)) {
                List<String>         lore    = new ArrayList<>();
                ConfigurationSection section = bonusesSection.getConfigurationSection(key + ".damage-types");
                if (section != null) {
                    Set<String> keys = section.getKeys(false);
                    if (!keys.isEmpty()) {
                        lore.add("&2Damage types:");
                        for (String damageType : keys) {
                            StringBuilder stringBuilder = new StringBuilder(" ");
                            String        value         = Objects.requireNonNull(section.getString(damageType));
                            DamageAttribute damageAttribute = ItemStats.getDamageById(damageType);
                            if (damageAttribute == null) {
                                stringBuilder.append(damageType);
                                stringBuilder.append(": ");
                                stringBuilder.append(value);
                            } else {
                                stringBuilder.append(damageAttribute.getFormat().replace("%value%", value));
                            }
                            lore.add(StringUT.color(stringBuilder.toString()));
                        }
                    }
                }
                section = bonusesSection.getConfigurationSection(key + ".defense-types");
                if (section != null) {
                    Set<String> keys = section.getKeys(false);
                    if (!keys.isEmpty()) {
                        lore.add("&2Defense types:");
                        for (String defenseType : keys) {
                            StringBuilder stringBuilder = new StringBuilder(" ");
                            String        value         = Objects.requireNonNull(section.getString(defenseType));
                            DefenseAttribute defenseAttribute = ItemStats.getDefenseById(defenseType);
                            if (defenseAttribute == null) {
                                stringBuilder.append(defenseType);
                                stringBuilder.append(": ");
                                stringBuilder.append(value);
                            } else {
                                stringBuilder.append(defenseAttribute.getFormat().replace("%value%", value));
                            }
                            lore.add(StringUT.color(stringBuilder.toString()));
                        }
                    }
                }
                section = bonusesSection.getConfigurationSection(key + ".item-stats");
                if (section != null) {
                    Set<String> keys = section.getKeys(false);
                    if (!keys.isEmpty()) {
                        lore.add("&2Item Stats:");
                        for (String statType : keys) {
                            StringBuilder stringBuilder = new StringBuilder(" ");
                            String        value         = Objects.requireNonNull(section.getString(statType));
                            stringBuilder.append(AbstractStat.Type.getByName(statType) == null ? "&f" : "&6");
                            stringBuilder.append(statType);
                            stringBuilder.append(": ");
                            stringBuilder.append("&6");
                            stringBuilder.append(value);
                            lore.add(StringUT.color(stringBuilder.toString()));
                        }
                    }
                }
                map.put(key, lore);
                list.add(key);
            }
        }
        list.add(null);
        int i = 0;
        for (String group : list) {
            i++;
            if (i % this.inventory.getSize() == 53) {
                this.setSlot(i, getNextButton());
                i++;
            } else if (i % 9 == 8) {i++;}
            if (i % this.inventory.getSize() == 45) {
                this.setSlot(i, getPrevButton());
                i++;
            } else if (i % 9 == 0) {i++;}
            setSlot(i, group == null ?
                    new Slot(createItem(Material.REDSTONE, "&eAdd new material or group")) {
                        @Override
                        public void onLeftClick() {
                            sendSetMessage("material or item group",
                                    null,
                                    s -> {
                                        String path = MainBonusesGUI.ItemType.MATERIAL.getPath() + '.' + s;
                                        cfg.set(path, cfg.createSection(path));
                                        saveAndReopen();
                                    });
                        }
                    } :
                    new Slot(createItem(MainMaterialsGUI.getMaterialGroup(group),
                            "&e" + group, StringUT.replace(CURRENT_PLACEHOLDER, map.get(group),
                                    "&bCurrent:",
                                    "&a%current%",
                                    "&6Left-Click: &eModify",
                                    "&6Drop: &eRemove"))) {
                        @Override
                        public void onLeftClick() {
                            openSubMenu(new BonusesGUI(player, itemGenerator, MainBonusesGUI.ItemType.MATERIAL.getPath() + '.' + group));
                        }

                        @Override
                        public void onDrop() {
                            cfg.remove(MainBonusesGUI.ItemType.MATERIAL.getPath() + '.' + group);
                            saveAndReopen();
                        }
                    });
        }
        list.remove(list.size() - 1);
        this.setSlot(this.getPages() * this.inventory.getSize() - 9, getPrevButton());
        this.setSlot(this.getPages() * this.inventory.getSize() - 1, getNextButton());
    }
}
