package su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.materials;

import mc.promcteam.engine.manager.api.menu.Slot;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.AbstractEditorGUI;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.EditorGUI;
import su.nightexpress.quantumrpg.stats.items.ItemStats;
import su.nightexpress.quantumrpg.stats.items.attributes.DamageAttribute;
import su.nightexpress.quantumrpg.stats.items.attributes.DefenseAttribute;
import su.nightexpress.quantumrpg.stats.items.attributes.api.AbstractStat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NewStatModifierGUI extends AbstractEditorGUI {
    private final String group;
    private final StatModifierTypeGUI.ItemType statType;

    public NewStatModifierGUI(Player player, ItemGeneratorReference itemGenerator, String group, StatModifierTypeGUI.ItemType statType) {
        super(player, 6, "[&d" + itemGenerator.getId() + "&r] editor/" + EditorGUI.ItemType.MATERIALS.getTitle(), itemGenerator);
        this.group = group;
        this.statType = statType;
    }

    @Override
    public void setContents() {
        Material             material;
        List<String>         list         = new ArrayList<>();
        ConfigurationSection section      = itemGenerator.getConfig().getConfigurationSection(getPath());
        Set<String>          existingKeys = section == null ? new HashSet<>() : section.getKeys(false);
        switch (this.statType) {
            case DAMAGE: {
                material = Material.IRON_SWORD;
                for (DamageAttribute damageAttribute : ItemStats.getDamages()) {
                    boolean exists = false;
                    for (String existingKey : existingKeys) {
                        if (existingKey.equalsIgnoreCase(damageAttribute.getId())) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {list.add(damageAttribute.getId());}
                }
                break;
            }
            case DEFENSE: {
                material = Material.IRON_CHESTPLATE;
                for (DefenseAttribute defenseAttribute : ItemStats.getDefenses()) {
                    boolean exists = false;
                    for (String existingKey : existingKeys) {
                        if (existingKey.equalsIgnoreCase(defenseAttribute.getId())) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {list.add(defenseAttribute.getId());}
                }
                break;
            }
            case ITEM_STAT: {
                material = Material.OAK_SIGN;
                for (AbstractStat.Type itemStat : AbstractStat.Type.values()) {
                    boolean exists = false;
                    for (String existingKey : existingKeys) {
                        if (existingKey.equalsIgnoreCase(itemStat.name())) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {list.add(itemStat.name());}
                }
                break;
            }
            default: {
                material = Material.STONE;
                break;
            }
        }
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
            setSlot(i, new Slot(createItem(material,
                    "&e" + stat,
                    "&6Left-Click: &eCreate")) {
                @Override
                public void onLeftClick() {
                    sendSetMessage(stat + ' ' + statType.name().replace('_', ' ').toLowerCase() + " value",
                            null,
                            s -> {
                                itemGenerator.getConfig().set(getPath() + '.' + stat, Double.parseDouble(s));
                                saveAndReopen();
                                close(2);
                            });
                }
            });
        }
        this.setSlot(this.getPages() * this.inventory.getSize() - 9, getPrevButton());
        this.setSlot(this.getPages() * this.inventory.getSize() - 1, getNextButton());
    }

    private String getPath() {
        return MainMaterialsGUI.ItemType.STAT_MODIFIERS.getPath() + '.' + this.group + '.' + this.statType.getPath();
    }
}
