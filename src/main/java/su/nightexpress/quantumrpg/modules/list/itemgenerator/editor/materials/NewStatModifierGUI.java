package su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.materials;

import mc.promcteam.engine.config.api.JYML;
import mc.promcteam.engine.manager.api.gui.ContentType;
import mc.promcteam.engine.manager.api.gui.GuiClick;
import mc.promcteam.engine.manager.api.gui.GuiItem;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.ItemGeneratorManager;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.AbstractEditorGUI;
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
    private String listeningStat = null;

    public NewStatModifierGUI(@NotNull ItemGeneratorManager itemGeneratorManager, ItemGeneratorManager.GeneratorItem itemGenerator, String group, StatModifierTypeGUI.ItemType statType) {
        super(itemGeneratorManager, itemGenerator, 54);
        this.group = group;
        this.statType = statType;
    }

    private String getPath() {
        return MainMaterialsGUI.ItemType.STAT_MODIFIERS.getPath()+'.'+this.group+'.'+this.statType.getPath();
    }

    @Override
    protected void onCreate(@NotNull Player player, @NotNull Inventory inventory, int page) {
        Material material;
        List<String> list = new ArrayList<>();
        ConfigurationSection section = this.itemGenerator.getConfig().getConfigurationSection(getPath());
        Set<String> existingKeys = section == null ? new HashSet<>() : section.getKeys(false);
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
                    if (!exists) { list.add(damageAttribute.getId()); }
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
                    if (!exists) { list.add(defenseAttribute.getId()); }
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
                    if (!exists) { list.add(itemStat.name()); }
                }
                break;
            }
            default: {
                material = Material.STONE;
                break;
            }
        }
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
                        new StatModifierTypeGUI(itemGeneratorManager, itemGenerator, group).open(player1, 1);
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
            if (type == ItemType.NEW) {
                GuiItem guiItem = this.getButton(player1, clickEvent.getSlot());
                if (guiItem == null) { return; }
                sendCreateMessage(guiItem.getId());
            }
        };
        for (int statIndex = (currentPage-1)*42, last = Math.min(list.size(), statIndex+42), invIndex = 1;
             statIndex < last; statIndex++, invIndex++) {
            if ((invIndex)%9 == 8) { invIndex += 2; }
            String stat = list.get(statIndex);
            this.addButton(this.createButton(stat, ItemType.NEW, material,
                                             "&e"+stat, List.of("&6Left-Click: &eCreate"), invIndex, guiClick));
        }
        this.addButton(this.createButton("prev-page", ContentType.BACK, Material.ENDER_PEARL, "&dPrevious Page", List.of(), 0, guiClick));
        this.addButton(this.createButton("next-page", ContentType.NEXT, Material.ENDER_PEARL, "&dNext Page", List.of(), 8, guiClick));
        this.addButton(this.createButton("return", ContentType.RETURN, Material.BARRIER, "&c&lReturn", List.of(), 53, guiClick));
    }

    private void sendCreateMessage(String id) {
        this.listeningStat = id;
        this.player.closeInventory();
        player.sendMessage("▸ Enter the desired "+id+' '+this.statType.name().replace('_', ' ').toLowerCase()+" value, or \"cancel\" to go back");
    }

    @Override
    public void onChat(AsyncPlayerChatEvent event) {
        if (this.listeningStat == null) { return; }
        event.setCancelled(true);
        String id = this.listeningStat;
        this.listeningStat = null;
        String message = event.getMessage().strip();
        if (message.equalsIgnoreCase("cancel")) {
            saveAndReopen(getUserPage(this.player, 0));
            return;
        }
        JYML cfg = this.itemGenerator.getConfig();
        cfg.set(getPath()+'.'+id, message);
        cfg.saveChanges();
        this.itemGeneratorManager.load(this.itemGenerator.getId(), cfg);
        new BukkitRunnable() {
            @Override
            public void run() {
                new StatModifiersGUI(itemGeneratorManager, itemGenerator, group).open(player, 1);
            }
        }.runTask(plugin);
    }
}
