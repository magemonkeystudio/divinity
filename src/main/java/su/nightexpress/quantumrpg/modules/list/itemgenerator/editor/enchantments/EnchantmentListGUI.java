package su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.enchantments;

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
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.ItemGeneratorManager;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.AbstractEditorGUI;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.EditorGUI;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EnchantmentListGUI extends AbstractEditorGUI {
    private String listening;

    public EnchantmentListGUI(@NotNull ItemGeneratorManager itemGeneratorManager, ItemGeneratorManager.GeneratorItem itemGenerator) {
        super(itemGeneratorManager, itemGenerator, 54);
        setTitle("[&d"+itemGenerator.getId()+"&r] editor/"+EditorGUI.ItemType.ENCHANTMENTS.getTitle());
    }

    @Override
    protected void onCreate(@NotNull Player player, @NotNull Inventory inventory, int page) {
        Map<String, String> map = new LinkedHashMap<>();
        ConfigurationSection section = this.itemGenerator.getConfig().getConfigurationSection(EditorGUI.ItemType.ENCHANTMENTS.getPath()+".list");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                map.put(key, section.getString(key));
            }
        }
        List<String> list = new ArrayList<>(map.keySet());
        Enchantment[] vanillaEnchantments = Enchantment.values();
        if (list.size() < vanillaEnchantments.length) {
            list.add(null);
        }
        List<String> missingList = new ArrayList<>();
        for (Enchantment enchantment : vanillaEnchantments) {
            String key = enchantment.getKey().toString().substring("minecraft:".length());
            if (!list.contains(key)) { missingList.add(key); }
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
                        new EnchantmentsGUI(itemGeneratorManager, itemGenerator).open(player1, 1);
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
            if (type.equals(EditorGUI.ItemType.ENCHANTMENTS)) {
                GuiItem guiItem = this.getButton(player, clickEvent.getSlot());
                if (guiItem == null) { return; }
                String key = guiItem.getId();
                switch (clickEvent.getClick()) {
                    case DROP: case CONTROL_DROP: {
                        this.itemGenerator.getConfig().remove(EditorGUI.ItemType.ENCHANTMENTS.getPath()+".list."+key);
                        saveAndReopen(currentPage);
                        break;
                    }
                    default: {
                        sendSetMessage(key, map.get(key));
                        break;
                    }
                }
            } else if (type == ItemType.NEW) {
                new NewEnchantmentGUI(this.itemGeneratorManager, this.itemGenerator, missingList).open(player1, 1);
            }
        };
        for (int materialIndex = (currentPage-1)*42, last = Math.min(list.size(), materialIndex+42), invIndex = 1;
             materialIndex < last; materialIndex++, invIndex++) {
            if ((invIndex)%9 == 8) { invIndex += 2; }
            String key = list.get(materialIndex);
            this.addButton(key == null ?
                                   this.createButton("new", ItemType.NEW, Material.REDSTONE, "&eAdd new enchantment", List.of(), invIndex, guiClick) :
                                   this.createButton(key, EditorGUI.ItemType.ENCHANTMENTS, Material.ENCHANTED_BOOK,
                                                     "&e"+key, List.of(
                                                             "&bCurrent: &a"+map.get(key),
                                                            "&6Left-Click: &eSet",
                                                            "&6Drop: &eRemove"), invIndex, guiClick));
        }
        if (list.get(list.size()-1) == null) { list.remove(list.size()-1); }
        this.addButton(this.createButton("prev-page", ContentType.BACK, Material.ENDER_PEARL, "&dPrevious Page", List.of(), 0, guiClick));
        this.addButton(this.createButton("next-page", ContentType.NEXT, Material.ENDER_PEARL, "&dNext Page", List.of(), 8, guiClick));
        this.addButton(this.createButton("return", ContentType.RETURN, Material.BARRIER, "&c&lReturn", List.of(), 53, guiClick));
    }

    private void sendSetMessage(String key, String currentValue) {
        this.listening = key;
        this.player.closeInventory();
        player.sendMessage("▸ Enter the desired level range, or \"cancel\" to go back");
        if (currentValue != null) {
            BaseComponent component = new TextComponent("[Current value]");
            component.setColor(ChatColor.GOLD);
            component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, currentValue));
            component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Enter the current value to chat")));
            player.spigot().sendMessage(component);
        }
    }

    @Override
    public void onChat(AsyncPlayerChatEvent event) {
        if (this.listening == null) { return; }
        event.setCancelled(true);
        String key = this.listening;
        this.listening = null;
        String message = event.getMessage().strip();
        try {
            String[] strings = message.split(":");
            if (strings.length > 2) { throw new IllegalArgumentException(); }
            for (String string : strings) { Integer.parseInt(string); }
        } catch (IllegalArgumentException e) {
            plugin.lang().ItemGenerator_Cmd_Editor_Error_InvalidInput.replace("%input%", message).replace("%value%", "level range").send(player);
            saveAndReopen(getUserPage(this.player, 0));
            return;
        }
        this.itemGenerator.getConfig().set(EditorGUI.ItemType.ENCHANTMENTS.getPath()+".list."+key, message);
        saveAndReopen(getUserPage(this.player, 0));
    }
}
