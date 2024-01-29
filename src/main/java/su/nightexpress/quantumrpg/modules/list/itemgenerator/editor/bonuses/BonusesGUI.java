package su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.bonuses;

import mc.promcteam.engine.config.api.JYML;
import mc.promcteam.engine.manager.api.menu.Slot;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.AbstractEditorGUI;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.EditorGUI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BonusesGUI extends AbstractEditorGUI {
    private final String path;

    public BonusesGUI(Player player, ItemGeneratorReference itemGenerator, String path) {
        super(player, 6, "[&d" + itemGenerator.getId() + "&r] editor/" + EditorGUI.ItemType.BONUSES.getTitle(), itemGenerator);
        this.path = path;
    }

    @Override
    public void setContents() {
        JYML                 cfg                  = this.itemGenerator.getConfig();
        Map<String, String>  map                  = new HashMap<>();
        List<String>         list                 = new ArrayList<>();
        ConfigurationSection configurationSection = cfg.getConfigurationSection(this.path);
        if (configurationSection != null) {
            ConfigurationSection section = configurationSection.getConfigurationSection("damage-types");
            if (section != null) {
                for (String damageType : section.getKeys(false)) {
                    String path = "damage-types." + damageType;
                    map.put(path, section.getString(damageType));
                    list.add(path);
                }
            }
            section = configurationSection.getConfigurationSection("defense-types");
            if (section != null) {
                for (String defenseType : section.getKeys(false)) {
                    String path = "defense-types." + defenseType;
                    map.put(path, section.getString(defenseType));
                    list.add(path);
                }
            }
            section = configurationSection.getConfigurationSection("item-stats");
            if (section != null) {
                for (String stat : section.getKeys(false)) {
                    String path = "item-stats." + stat;
                    map.put(path, section.getString(stat));
                    list.add(path);
                }
            }
        }
        list.add(null);
        int i = 0;
        for (String stat : list) {
            i++;
            if (i % this.inventory.getSize() == 53) {
                this.setSlot(i, getNextButton());
                i++;
            } else if (i % 9 == 8) {i++;}
            if (i % this.inventory.getSize() == 45) {
                this.setSlot(i, getPrevButton());
                i++;
            } else if (i % 9 == 0) {i++;}
            if (stat == null) {
                setSlot(i, new Slot(createItem(Material.REDSTONE, "&eAdd new stat bonus")) {
                    @Override
                    public void onLeftClick() {
                        openSubMenu(new BonusStatTypeGUI(player, itemGenerator, path));
                    }
                });
            } else {
                Material material;
                String   id;
                String   value = map.get(stat);
                if (stat.startsWith("damage-types.")) {
                    material = Material.IRON_SWORD;
                    id = stat.substring("damage-types.".length())+" damage";
                } else if (stat.startsWith("defense-types.")) {
                    material = Material.IRON_CHESTPLATE;
                    id = stat.substring("defense-types.".length())+" defense";
                } else if (stat.startsWith("item-stats.")) {
                    material = Material.OAK_SIGN;
                    id = stat.substring("item-stats.".length())+" stat";
                } else {
                    material = Material.OAK_SIGN;
                    id = stat;
                }
                String path = this.path + '.' + stat;
                setSlot(i, new Slot(createItem(material,
                        "&e" + id,
                        "&bCurrent: &a" + value,
                        "&6Left-Click: &eSet",
                        "&6Drop: &eRemove")) {
                    @Override
                    public void onLeftClick() {
                        sendSetMessage(id + " value",
                                itemGenerator.getConfig().getString(path),
                                s -> {
                                    cfg.set(path, Double.parseDouble(s));
                                    saveAndReopen();
                                });
                    }

                    @Override
                    public void onDrop() {
                        cfg.remove(path);
                        saveAndReopen();
                    }
                });
            }
        }
        list.remove(list.size() - 1);
        this.setSlot(this.getPages() * this.inventory.getSize() - 9, getPrevButton());
        this.setSlot(this.getPages() * this.inventory.getSize() - 1, getNextButton());
    }
}
