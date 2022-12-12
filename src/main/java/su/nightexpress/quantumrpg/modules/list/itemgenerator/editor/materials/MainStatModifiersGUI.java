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
import su.nightexpress.quantumrpg.stats.items.ItemStats;
import su.nightexpress.quantumrpg.stats.items.attributes.DamageAttribute;
import su.nightexpress.quantumrpg.stats.items.attributes.DefenseAttribute;
import su.nightexpress.quantumrpg.stats.items.attributes.api.AbstractStat;

import java.util.*;

public class MainStatModifiersGUI extends AbstractEditorGUI {
    private boolean listening = false;

    public MainStatModifiersGUI(@NotNull ItemGeneratorManager itemGeneratorManager, ItemGeneratorManager.GeneratorItem itemGenerator) {
        super(itemGeneratorManager, itemGenerator, 54);
        setTitle("[&d"+itemGenerator.getId()+"&r] editor/"+EditorGUI.ItemType.MATERIALS.getTitle());
    }

    @Override
    protected void onCreate(@NotNull Player player, @NotNull Inventory inventory, int page) {
        JYML cfg = this.itemGenerator.getConfig();
        Map<String,List<String>> map = new HashMap<>();
        List<String> list = new ArrayList<>();
        ConfigurationSection statModifierSection = cfg.getConfigurationSection(MainMaterialsGUI.ItemType.STAT_MODIFIERS.getPath());
        if (statModifierSection != null) {
            for (String key : statModifierSection.getKeys(false)) {
                List<String> lore = new ArrayList<>();
                ConfigurationSection section = statModifierSection.getConfigurationSection(key+".damage-types");
                if (section != null) {
                    Set<String> keys = section.getKeys(false);
                    if (!keys.isEmpty()) {
                        lore.add("&2Damage types:");
                        for (String damageType : keys) {
                            StringBuilder stringBuilder = new StringBuilder(" ");
                            String value = Objects.requireNonNull(section.getString(damageType));
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
                section = statModifierSection.getConfigurationSection(key+".defense-types");
                if (section != null) {
                    Set<String> keys = section.getKeys(false);
                    if (!keys.isEmpty()) {
                        lore.add("&2Defense types:");
                        for (String defenseType : keys) {
                            StringBuilder stringBuilder = new StringBuilder(" ");
                            String value = Objects.requireNonNull(section.getString(defenseType));
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
                section = statModifierSection.getConfigurationSection(key+".item-stats");
                if (section != null) {
                    Set<String> keys = section.getKeys(false);
                    if (!keys.isEmpty()) {
                        lore.add("&2Item Stats:");
                        for (String statType : keys) {
                            StringBuilder stringBuilder = new StringBuilder(" ");
                            String value = Objects.requireNonNull(section.getString(statType));
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
            if (type == MainMaterialsGUI.ItemType.STAT_MODIFIERS) {
                GuiItem guiItem = this.getButton(player1, clickEvent.getSlot());
                if (guiItem == null) { return; }
                switch (clickEvent.getClick()) {
                    case DROP: case CONTROL_DROP: {
                        cfg.remove(MainMaterialsGUI.ItemType.STAT_MODIFIERS.getPath()+'.'+guiItem.getId());
                        saveAndReopen(currentPage);
                        break;
                    }
                    default: {
                        new StatModifiersGUI(this.itemGeneratorManager, this.itemGenerator, guiItem.getId()).open(player, 1);
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
            String group = list.get(materialIndex);
            this.addButton(group == null ?
                                   this.createButton("new", ItemType.NEW, Material.REDSTONE, "&eAdd new material or group", List.of(), invIndex, guiClick) :
                                   this.createButton(group, MainMaterialsGUI.ItemType.STAT_MODIFIERS, MainMaterialsGUI.getMaterialGroup(group),
                                                     "&e"+group, replaceLore(List.of(
                                                             "&bCurrent:",
                                                             "&a%current%",
                                                             "&6Left-Click: &eModify",
                                                             "&6Drop: &eRemove"), map.get(group)), invIndex, guiClick));
        }
        list.remove(list.size()-1);
        this.addButton(this.createButton("prev-page", ContentType.BACK, Material.ENDER_PEARL, "&dPrevious Page", List.of(), 0, guiClick));
        this.addButton(this.createButton("next-page", ContentType.NEXT, Material.ENDER_PEARL, "&dNext Page", List.of(), 8, guiClick));
        this.addButton(this.createButton("return", ContentType.RETURN, Material.BARRIER, "&c&lReturn", List.of(), 53, guiClick));
    }

    private void sendCreateMessage() {
        this.listening = true;
        this.player.closeInventory();
        player.sendMessage("▸ Enter the desired material or item group, or \"cancel\" to go back");
    }

    @Override
    public void onChat(AsyncPlayerChatEvent event) {
        if (!this.listening) { return; }
        event.setCancelled(true);
        this.listening = false;
        String message = event.getMessage().strip().replace(' ', '_');
        if (message.equalsIgnoreCase("cancel")) {
            saveAndReopen(getUserPage(this.player, 0));
            return;
        }
        JYML cfg = this.itemGenerator.getConfig();
        String path = MainMaterialsGUI.ItemType.STAT_MODIFIERS.getPath()+'.'+message;
        cfg.set(path, cfg.createSection(path));
        saveAndReopen(getUserPage(this.player, 0));
    }
}
