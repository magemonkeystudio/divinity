package su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.requirements;

import mc.promcteam.engine.config.api.JYML;
import mc.promcteam.engine.manager.api.gui.ContentType;
import mc.promcteam.engine.manager.api.gui.GuiClick;
import mc.promcteam.engine.manager.api.gui.GuiItem;
import mc.promcteam.engine.utils.StringUT;
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
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.ItemGeneratorManager;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.AbstractEditorGUI;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.EditorGUI;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public abstract class RequirementsGUI extends AbstractEditorGUI {
    private boolean listening = false;
    private Integer levelListening;

    public RequirementsGUI(@NotNull ItemGeneratorManager itemGeneratorManager, ItemGeneratorManager.GeneratorItem itemGenerator) {
        super(itemGeneratorManager, itemGenerator, 54);
    }

    @Override
    protected void onCreate(@NotNull Player player, @NotNull Inventory inventory, int page) {
        TreeMap<Integer,String> requirements = getRequirements();
        List<Integer> levels = new ArrayList<>(requirements.keySet());
        levels.add(null);
        int totalPages = Math.max((int) Math.ceil(levels.size()*1.0/42), 1);
        final int currentPage = page < 1 ? totalPages : Math.min(page, totalPages);
        this.setUserPage(player, currentPage, totalPages);
        GuiClick guiClick = (player1, type, clickEvent) -> {
            this.setUserPage(player, currentPage, totalPages);
            if (type == null) { return; }
            Class<?> clazz = type.getClass();
            if (clazz.equals(ContentType.class)) {
                ContentType type2 = (ContentType) type;
                switch (type2) {
                    case RETURN:
                        new MainRequirementsGUI(itemGeneratorManager, itemGenerator).open(player1, 1);
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
            if (type == EditorGUI.ItemType.REQUIREMENTS) {
                GuiItem guiItem = this.getButton(player, clickEvent.getSlot());
                if (guiItem == null) { return; }
                int level = Integer.parseInt(guiItem.getId());
                switch (clickEvent.getClick()) {
                    case DROP: case CONTROL_DROP: {
                        requirements.remove(level);
                        setRequirements(requirements);
                        saveAndReopen(currentPage);
                        break;
                    }
                    default: {
                        sendSetMessage(level, requirements.get(level));
                        break;
                    }
                }
            } else if (type == ItemType.NEW) {
                sendCreateMessage();
            }
        };
        for (int levelIndex = (currentPage-1)*42, last = Math.min(levels.size(), levelIndex+42), invIndex = 1;
             levelIndex < last; levelIndex++, invIndex++) {
            if ((invIndex)%9 == 8) { invIndex += 2; }
            Integer level = levels.get(levelIndex);
            this.addButton(level == null ?
                                   this.createButton("new", ItemType.NEW, Material.REDSTONE, "&eAdd new requirement", List.of(), invIndex, guiClick) :
                                   this.createButton(String.valueOf(level), EditorGUI.ItemType.REQUIREMENTS, getButtonMaterial(),
                                                     "&e"+level, List.of(
                                                             "&bCurrent: &a"+requirements.get(level),
                                                             "&6Left-Click: &eSet",
                                                             "&6Drop: &eRemove"), invIndex, guiClick));
        }
        this.addButton(this.createButton("prev-page", ContentType.BACK, Material.ENDER_PEARL, "&dPrevious Page", List.of(), 0, guiClick));
        this.addButton(this.createButton("next-page", ContentType.NEXT, Material.ENDER_PEARL, "&dNext Page", List.of(), 8, guiClick));
        this.addButton(this.createButton("return", ContentType.RETURN, Material.BARRIER, "&c&lReturn", List.of(), 53, guiClick));
    }

    protected abstract String getPath();

    protected abstract Material getButtonMaterial();

    private void sendSetMessage(int level, String currentValue) {
        this.listening = true;
        this.levelListening = level;
        this.player.closeInventory();
        player.sendMessage("▸ Enter the desired requirement for level "+level);
        if (currentValue != null) {
            BaseComponent component = new TextComponent("[Current requirement]");
            component.setColor(ChatColor.GOLD);
            component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, currentValue));
            component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Enter the current requirement to chat")));
            player.spigot().sendMessage(component);
        }
    }

    private void sendCreateMessage() {
        this.listening = true;
        this.player.closeInventory();
        player.sendMessage("▸ Enter the desired level for the new requirement");
    }

    @Override
    public void onChat(AsyncPlayerChatEvent event) {
        if (!this.listening) { return; }
        event.setCancelled(true);
        this.listening = false;
        Integer levelListening = this.levelListening;
        this.levelListening = null;
        String message = event.getMessage().strip();
        if (levelListening == null) {
            // Creating new requirement
            int level;
            try {
                level = Integer.parseInt(message);
            } catch (NumberFormatException e) {
                plugin.lang().ItemGenerator_Cmd_Editor_Error_InvalidInput.replace("%input%", message).replace("%value%", "level").send(player);
                saveAndReopen(getUserPage(this.player, 0));
                return;
            }
            new BukkitRunnable() {
                @Override
                public void run() { sendSetMessage(level, null); }
            }.runTask(plugin);
        } else {
            TreeMap<Integer,String> requirements = getRequirements();
            requirements.put(levelListening, message);
            setRequirements(requirements);
            saveAndReopen(getUserPage(this.player, 0));
        }
    }

    protected TreeMap<Integer,String> getRequirements() {
        ConfigurationSection requirementsSection = this.itemGenerator.getConfig().getConfigurationSection(getPath());
        TreeMap<Integer,String> requirements = new TreeMap<>();
        if (requirementsSection != null) {
            for (String key : requirementsSection.getKeys(false)) {
                int itemLvl = StringUT.getInteger(key, -1);
                if (itemLvl <= 0) { continue; }

                String requirement = requirementsSection.getString(key);
                if (requirement == null || requirement.isEmpty()) { continue; }

                requirements.put(itemLvl, requirement);
            }
        }
        return requirements;
    }

    protected void setRequirements(TreeMap<Integer,String> requirements)  {
        JYML cfg = this.itemGenerator.getConfig();
        cfg.remove(getPath());
        for (Map.Entry<Integer,String> entry : requirements.entrySet()) {
            cfg.set(getPath()+'.'+entry.getKey(), entry.getValue());
        }
    }
}
