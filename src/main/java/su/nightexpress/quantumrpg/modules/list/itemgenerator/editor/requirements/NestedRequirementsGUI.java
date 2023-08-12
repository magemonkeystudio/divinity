package su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.requirements;

import com.mojang.datafixers.util.Pair;
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
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.ItemGeneratorManager;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.AbstractEditorGUI;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.EditorGUI;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class NestedRequirementsGUI extends AbstractEditorGUI {
    private final MainRequirementsGUI.ItemType type;
    private final Material material;
    private boolean listening = false;
    private Integer levelListening;

    public NestedRequirementsGUI(@NotNull ItemGeneratorManager itemGeneratorManager, ItemGeneratorManager.GeneratorItem itemGenerator, MainRequirementsGUI.ItemType type, Material material) {
        super(itemGeneratorManager, itemGenerator, 54);
        setTitle("[&d" + itemGenerator.getId() + "&r] editor/" + EditorGUI.ItemType.REQUIREMENTS.getTitle());
        this.type = type;
        this.material = material;
    }

    @Override
    protected void onCreate(@NotNull Player player, @NotNull Inventory inventory, int page) {
        TreeMap<String, TreeMap<Integer, String>> requirements = getRequirements();

        List<Pair<String, Integer>> levels = new ArrayList<>();
        requirements.forEach((key, value) -> value.keySet().forEach(subKey -> levels.add(new Pair<>(key, subKey))));
        levels.add(null);
        int totalPages = Math.max((int) Math.ceil(levels.size() * 1.0 / 42), 1);
        final int currentPage = page < 1 ? totalPages : Math.min(page, totalPages);
        this.setUserPage(player, currentPage, totalPages);
        GuiClick guiClick = (player1, type, clickEvent) -> {
            this.setUserPage(player, currentPage, totalPages);
            if (type == null) {
                return;
            }
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
                        saveAndReopen(currentPage + 1);
                        break;
                    }
                    case BACK: {
                        saveAndReopen(currentPage - 1);
                        break;
                    }
                }
                return;
            }
            if (type == EditorGUI.ItemType.REQUIREMENTS) {
                GuiItem guiItem = this.getButton(player, clickEvent.getSlot());
                if (guiItem == null) {
                    return;
                }

                int slot = Integer.parseInt(guiItem.getId());
                Pair<String, Integer> entry = levels.get(slot);
                String identifier = entry.getFirst();
                int level = entry.getSecond();

                switch (clickEvent.getClick()) {
                    case DROP:
                    case CONTROL_DROP: {
                        requirements.get(identifier).remove(level);
                        setRequirements(requirements);
                        saveAndReopen(currentPage);
                        break;
                    }
                    default: {
                        sendSetMessage(level, requirements.get(identifier).get(level));
                        break;
                    }
                }
            } else if (type == AbstractEditorGUI.ItemType.NEW) {
                sendCreateMessage();
            }
        };
        for (int levelIndex = (currentPage - 1) * 42, last = Math.min(levels.size(), levelIndex + 42), invIndex = 1;
             levelIndex < last; levelIndex++, invIndex++) {
            if ((invIndex) % 9 == 8) {
                invIndex += 2;
            }
            Pair<String, Integer> entry = levels.get(levelIndex);
            if (entry == null) {
                QuantumRPG.instance.warn("Item is null");
                this.addButton(this.createButton("new", AbstractEditorGUI.ItemType.NEW, Material.REDSTONE, "&eAdd new requirement", List.of(), invIndex, guiClick));
            } else {
                String identifier = entry.getFirst();
                Integer level = entry.getSecond();
                this.addButton(this.createButton(String.valueOf(level), EditorGUI.ItemType.REQUIREMENTS, this.material,
                        "&e" + level, List.of(
                                getIdentifier(type) + ": " + identifier,
                                "&bCurrent Level: &a" + level,
                                "&6Left-Click: &eSet",
                                "&6Drop: &eRemove"), invIndex, guiClick));
            }
        }
        this.addButton(this.createButton("prev-page", ContentType.BACK, Material.ENDER_PEARL, "&dPrevious Page", List.of(), 0, guiClick));
        this.addButton(this.createButton("next-page", ContentType.NEXT, Material.ENDER_PEARL, "&dNext Page", List.of(), 8, guiClick));
        this.addButton(this.createButton("return", ContentType.RETURN, Material.BARRIER, "&c&lReturn", List.of(), 53, guiClick));
    }

    private void sendSetMessage(int level, String currentValue) {
        this.listening = true;
        this.levelListening = level;
        this.player.closeInventory();
        player.sendMessage("▸ Enter identifier(skill,job,..) and level-range for  " + level + ", or \"cancel\" to go back");
        player.sendMessage(" ");
        player.sendMessage("&7▸ Example for mcMMO: &a'unarmed:1:10' &7(Need Unarmed between lvl 1-10)".replace("&", "§"));
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
        player.sendMessage("▸ Enter the desired level for the new requirement, or \"cancel\" to go back");
    }

    @Override
    public void onChat(AsyncPlayerChatEvent event) {
        if (!this.listening) {
            return;
        }
        event.setCancelled(true);
        this.listening = false;
        Integer levelListening = this.levelListening;
        this.levelListening = null;
        String message = event.getMessage().strip();
        if (message.equalsIgnoreCase("cancel")) {
            saveAndReopen(getUserPage(this.player, 0));
            return;
        }
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
                public void run() {
                    sendSetMessage(level, null);
                }
            }.runTask(plugin);
        } else {
            String[] data = message.split(":", 2);
            String identifier = data[0];
            String levelMsg = data[1];

            TreeMap<String, TreeMap<Integer, String>> requirements = getRequirements();
            requirements.putIfAbsent(identifier, new TreeMap<>());
            requirements.get(identifier).put(levelListening, levelMsg);
            setRequirements(requirements);
            saveAndReopen(getUserPage(this.player, 0));
        }
    }

    private String getIdentifier(MainRequirementsGUI.ItemType type) {
        switch (type) {
            case MCMMO_SKILL:
                return "McMMO Skill";
            case JOBS_JOB:
                return "Job";
            case AURELIUM_SKILL:
                return "Aurelium Skill";
            case AURELIUM_STAT:
                return "Aurelium Stat";
            default:
                return "-";
        }
    }

    protected TreeMap<String, TreeMap<Integer, String>> getRequirements() {
        ConfigurationSection requirementsSection = this.itemGenerator.getConfig().getConfigurationSection(this.type.getPath());
        TreeMap<String, TreeMap<Integer, String>> requirements = new TreeMap<>();
        if (requirementsSection != null) {
            for (String identifier : requirementsSection.getKeys(false)) {
                requirements.putIfAbsent(identifier, new TreeMap<>());
                ConfigurationSection levelSection = this.itemGenerator.getConfig().getConfigurationSection(this.type.getPath() + "." + identifier);

                if (levelSection != null) {
                    for (String key : levelSection.getKeys(false)) {
                        int itemLvl = StringUT.getInteger(key, -1);
                        if (itemLvl <= 0) {
                            continue;
                        }
                        String requirement = levelSection.getString(key);
                        if (requirement == null || requirement.isEmpty()) {
                            continue;
                        }

                        requirements.get(identifier).put(itemLvl, requirement);
                    }
                }
            }
        }
        QuantumRPG.getInstance().info(String.valueOf(requirements));
        return requirements;
    }

    protected void setRequirements(TreeMap<String, TreeMap<Integer, String>> requirements) {
        JYML cfg = this.itemGenerator.getConfig();
        cfg.remove(this.type.getPath());
        for (Map.Entry<String, TreeMap<Integer, String>> entry : requirements.entrySet()) {
            for (Map.Entry<Integer, String> levelEntry : entry.getValue().entrySet()) {
                cfg.set(this.type.getPath() + '.' + entry.getKey() + "." + levelEntry.getKey(), levelEntry.getValue());
            }
        }
    }
}
