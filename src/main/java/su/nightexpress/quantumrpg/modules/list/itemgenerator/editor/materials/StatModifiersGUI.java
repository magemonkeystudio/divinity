package su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.materials;

import mc.promcteam.engine.config.api.JYML;
import mc.promcteam.engine.manager.api.gui.ContentType;
import mc.promcteam.engine.manager.api.gui.GuiClick;
import mc.promcteam.engine.manager.api.gui.GuiItem;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.ItemGeneratorManager;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.AbstractEditorGUI;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.EditorGUI;

import java.util.*;

public class StatModifiersGUI extends AbstractEditorGUI {
    private final String group;
    private String listeningPath = null;

    public StatModifiersGUI(@NotNull ItemGeneratorManager itemGeneratorManager, ItemGeneratorManager.GeneratorItem itemGenerator, String group) {
        super(itemGeneratorManager, itemGenerator, 54);
        this.group = group;
        setTitle("[&d"+itemGenerator.getId()+"&r] editor/"+EditorGUI.ItemType.MATERIALS.getTitle());
    }

    @Override
    protected void onCreate(@NotNull Player player, @NotNull Inventory inventory, int page) {
        JYML cfg = this.itemGenerator.getConfig();
        Map<String,String> map = new HashMap<>();
        List<String> list = new ArrayList<>();
        ConfigurationSection configurationSection = cfg.getConfigurationSection(getPath());
        if (configurationSection != null) {
            ConfigurationSection section = configurationSection.getConfigurationSection("damage-types");
            if (section != null) {
                for (String damageType : section.getKeys(false)) {
                    String path = "damage-types."+damageType;
                    map.put(path, section.getString(damageType));
                    list.add(path);
                }
            }
            section = configurationSection.getConfigurationSection("defense-types");
            if (section != null) {
                for (String defenseType : section.getKeys(false)) {
                    String path = "defense-types."+defenseType;
                    map.put(path, section.getString(defenseType));
                    list.add(path);
                }
            }
            section = configurationSection.getConfigurationSection("item-stats");
            if (section != null) {
                for (String stat : section.getKeys(false)) {
                    String path = "item-stats."+stat;
                    map.put(path, section.getString(stat));
                    list.add(path);
                }
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
                        new MainStatModifiersGUI(itemGeneratorManager, itemGenerator).open(player1, 1);
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
                        cfg.remove(getPath()+'.'+guiItem.getId());
                        saveAndReopen(currentPage);
                        break;
                    }
                    default: {
                        sendSetMessage(guiItem.getId(), Objects.requireNonNull(guiItem.getItem().getItemMeta()).getDisplayName());
                        break;
                    }
                }
            } else if (type == ItemType.NEW) {
                new StatModifierTypeGUI(this.itemGeneratorManager, this.itemGenerator, this.group).open(player1, page);
            }
        };
        for (int statIndex = (currentPage-1)*42, last = Math.min(list.size(), statIndex+42), invIndex = 1;
             statIndex < last; statIndex++, invIndex++) {
            if ((invIndex)%9 == 8) { invIndex += 2; }
            String stat = list.get(statIndex);
            if (stat == null) {
                this.addButton(this.createButton("new", ItemType.NEW, Material.REDSTONE, "&eAdd new stat modifier", List.of(), invIndex, guiClick));
            } else {
                Material material;
                String id;
                String value = map.get(stat);
                if (stat.startsWith("damage-types.")) {
                    material = Material.IRON_SWORD;
                    id = stat.substring("damage-types.".length());
                } else if (stat.startsWith("defense-types.")) {
                    material = Material.IRON_CHESTPLATE;
                    id = stat.substring("defense-types.".length());
                } else if (stat.startsWith("item-stats.")) {
                    material = Material.OAK_SIGN;
                    id = stat.substring("item-stats.".length());
                } else {
                    material = Material.OAK_SIGN;
                    id = stat;
                }
                this.addButton(this.createButton(stat, MainMaterialsGUI.ItemType.STAT_MODIFIERS, material,
                                                 "&e"+id, List.of(
                                                         "&bCurrent: &a"+value,
                                                         "&6Left-Click: &eSet",
                                                         "&6Drop: &eRemove"), invIndex, guiClick));
            }
        }
        list.remove(list.size()-1);
        this.addButton(this.createButton("prev-page", ContentType.BACK, Material.ENDER_PEARL, "&dPrevious Page", List.of(), 0, guiClick));
        this.addButton(this.createButton("next-page", ContentType.NEXT, Material.ENDER_PEARL, "&dNext Page", List.of(), 8, guiClick));
        this.addButton(this.createButton("return", ContentType.RETURN, Material.BARRIER, "&c&lReturn", List.of(), 53, guiClick));
    }

    private String getPath() { return MainMaterialsGUI.ItemType.STAT_MODIFIERS.getPath()+'.'+this.group; }

    private void sendSetMessage(String path, String name) {
        this.listeningPath = path;
        this.player.closeInventory();
        player.sendMessage("▸ Enter the desired "+name+" value, or \"cancel\" to go back");
        BaseComponent component = new TextComponent("[Current value]");
        component.setColor(ChatColor.GOLD);
        component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, this.itemGenerator.getConfig().getString(getPath()+'.'+path)));
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Enter the current value to chat")));
        player.spigot().sendMessage(component);
    }

    @Override
    public void onChat(AsyncPlayerChatEvent event) {
        if (this.listeningPath == null) { return; }
        event.setCancelled(true);
        String path = this.listeningPath;
        this.listeningPath = null;
        String message = event.getMessage().strip();
        if (message.equalsIgnoreCase("cancel")) {
            saveAndReopen(getUserPage(this.player, 0));
            return;
        }
        JYML cfg = this.itemGenerator.getConfig();
        cfg.set(getPath()+'.'+path, message);
        saveAndReopen(getUserPage(this.player, 0));
    }
}
