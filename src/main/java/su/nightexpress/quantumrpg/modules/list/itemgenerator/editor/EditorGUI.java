package su.nightexpress.quantumrpg.modules.list.itemgenerator.editor;

import mc.promcteam.engine.config.api.JYML;
import mc.promcteam.engine.manager.api.gui.ContentType;
import mc.promcteam.engine.manager.api.gui.GuiClick;
import mc.promcteam.engine.manager.api.gui.GuiItem;
import mc.promcteam.engine.manager.api.gui.NGUI;
import mc.promcteam.engine.utils.StringUT;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.ItemGeneratorManager;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Consumer;

public class EditorGUI extends NGUI<QuantumRPG> {
    static final String CURRENT_PLACEHOLDER = "%current%";
    static YamlConfiguration commonItemGenerator;
    
    final ItemGeneratorManager itemGeneratorManager;
    final ItemGeneratorManager.GeneratorItem itemGenerator;
    Player player;
    ItemType listening;

    public EditorGUI(@NotNull ItemGeneratorManager itemGeneratorManager, @NotNull JYML cfg, ItemGeneratorManager.GeneratorItem itemGenerator) {
        super(itemGeneratorManager.plugin, cfg, "editor-gui.");
        this.itemGeneratorManager = itemGeneratorManager;
        this.itemGenerator = itemGenerator;
        if (EditorGUI.commonItemGenerator == null) {
            try (InputStreamReader in = new InputStreamReader(Objects.requireNonNull(plugin.getClass().getResourceAsStream(this.itemGeneratorManager.getPath()+"items/common.yml")))) {
                EditorGUI.commonItemGenerator = YamlConfiguration.loadConfiguration(in);
            } catch (IOException exception) { throw new RuntimeException(exception); }
        }
        this.setTitle(this.getTitle().replace("%id%", itemGenerator.getId()));
        GuiClick guiClick = new GuiClick() {
            @Override
            public void click(@NotNull Player player, @Nullable Enum<?> type, @NotNull InventoryClickEvent clickEvent) {
                if (type == null) { return; }
                Class<?> clazz = type.getClass();
                if (clazz.equals(ContentType.class)) {
                    ContentType type2 = (ContentType) type;
                    switch (type2) {
                        case BACK:
                        case EXIT:
                        case RETURN:
                            player.closeInventory();
                            break;
                    }
                    return;
                }

                if (clazz.equals(ItemType.class)) {
                    ItemType type2 = (ItemType) type;
                    ClickType clickType = clickEvent.getClick();
                    switch (type2) {
                        case NAME: {
                            switch (clickType) {
                                case DROP: case CONTROL_DROP: {
                                    setDefault(type2, EditorGUI.this.itemGenerator.getConfig());
                                    break;
                                }
                                default: {
                                    sendSetMessage(type2, EditorGUI.this.itemGenerator.getName().substring("§r§f".length()).replace('§', '&'));
                                    break;
                                }
                            }
                            break;
                        }
                        case PREFIX_CHANCE: {
                            switch (clickType) {
                                case DROP: case CONTROL_DROP: {
                                    setDefault(type2, EditorGUI.this.itemGenerator.getConfig());
                                    break;
                                }
                                default: {
                                    sendSetMessage(type2, String.valueOf(EditorGUI.this.itemGenerator.getPrefixChance()));
                                    break;
                                }
                            }
                            break;
                        }
                        case SUFFIX_CHANCE: {
                            switch (clickType) {
                                case DROP: case CONTROL_DROP: {
                                    setDefault(type2, EditorGUI.this.itemGenerator.getConfig());
                                    break;
                                }
                                default: {
                                    sendSetMessage(type2, String.valueOf(EditorGUI.this.itemGenerator.getSuffixChance()));
                                    break;
                                }
                            }
                            break;
                        }
                        case LORE: {
                            break;
                        }
                        case COLOR: {
                            switch (clickType) {
                                case DROP: case CONTROL_DROP: {
                                    setDefault(type2, EditorGUI.this.itemGenerator.getConfig());
                                    break;
                                }
                                default: {
                                    int[] color = EditorGUI.this.itemGenerator.getColor();
                                    sendSetMessage(type2, color[0]+","+color[1]+","+color[2]);
                                    break;
                                }
                            }
                            break;
                        }
                        case UNBREAKABLE: {
                            switch (clickType) {
                                case DROP: case CONTROL_DROP: {
                                    setDefault(type2, EditorGUI.this.itemGenerator.getConfig());
                                    break;
                                }
                                default: {
                                    JYML cfg = EditorGUI.this.itemGenerator.getConfig();
                                    cfg.set(type2.getPath(), !EditorGUI.this.itemGenerator.isUnbreakable());
                                    reload(cfg);
                                    break;
                                }
                            }
                            break;
                        }
                        case ITEM_FLAGS: {
                            switch (clickType) {
                                case DROP: case CONTROL_DROP: {
                                    setDefault(type2, EditorGUI.this.itemGenerator.getConfig());
                                    break;
                                }
                                default: {
                                    open(new ItemFlagsGUI(EditorGUI.this, plugin, EditorGUI.this.getTitle()+'/'+type2.getTitle()), type2);
                                    break;
                                }
                            }
                            break;
                        }
                        case TIER: {
                            break;
                        }
                        case MATERIALS: {
                            break;
                        }
                        case MIN_LEVEL: {
                            break;
                        }
                        case MAX_LEVEL: {
                            break;
                        }
                        case AMMO_TYPES: {
                            break;
                        }
                        case HAND_TYPES: {
                            break;
                        }
                        case DAMAGE_TYPES: {
                            break;
                        }
                        case DEFENSE_TYPES: {
                            break;
                        }
                        case ITEM_STATS: {
                            break;
                        }
                        case SOCKETS: {
                            break;
                        }
                        case REQUIREMENTS: {
                            break;
                        }
                        case ENCHANTMENTS: {
                            break;
                        }
                        case ABILITIES: {
                            break;
                        }
                        case SAMPLE: {
                            for (String id : cfg.getSection("editor-gui.content")) {
                                GuiItem guiItem = cfg.getGuiItem("editor-gui.content."+id, ItemType.class);
                                if (guiItem == null || guiItem.getType() != ItemType.SAMPLE) { continue; }
                                guiItem.setClick(this);
                                guiItem.setItem(EditorGUI.this.itemGenerator.create());
                                EditorGUI.this.addButton(guiItem);
                                player.closeInventory();
                                EditorGUI.this.open(player, 1);
                            }
                            break;
                        }
                    }
                }
            }
        };

        for (String id : cfg.getSection("editor-gui.content")) {
            GuiItem guiItem = cfg.getGuiItem("editor-gui.content." + id, ItemType.class);
            if (guiItem == null) { continue; }
            Enum<?> type = guiItem.getType();
            if (type != null) {
                guiItem.setClick(guiClick);
                if (type instanceof ItemType) {
                    ItemStack itemStack = guiItem.getItemRaw();
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    if (itemMeta != null) {
                        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                        itemStack.setItemMeta(itemMeta);
                        guiItem.setItem(itemStack);
                    }
                    switch ((ItemType) type) {
                        case NAME: {
                            replaceLore(guiItem, itemGenerator.getName().substring("§r§f".length()), 30);
                            break;
                        }
                        case PREFIX_CHANCE: {
                            replaceLore(guiItem, String.valueOf(itemGenerator.getPrefixChance()), -1);
                        }
                        case SUFFIX_CHANCE: {
                            replaceLore(guiItem, String.valueOf(itemGenerator.getSuffixChance()), -1);
                        }
                        case COLOR: {
                            int[] color = itemGenerator.getColor();
                            replaceLore(guiItem, color[0]+","+color[1]+","+color[2], -1);
                            break;
                        }
                        case UNBREAKABLE: {
                            replaceLore(guiItem, String.valueOf(itemGenerator.isUnbreakable()), -1);
                            break;
                        }
                        case ITEM_FLAGS: {
                            List<String> itemFlags = new ArrayList<>();
                            for (ItemFlag flag : itemGenerator.getFlags()) { itemFlags.add(flag.name().toLowerCase()); }
                            replaceLore(guiItem, itemFlags);
                            break;
                        }
                        case SAMPLE: {
                            guiItem.setItem(this.itemGenerator.create(-1, -1, null));
                            break;
                        }
                    }
                }
            }

            this.addButton(guiItem);
        }
    }

    @Override
    public void open(@NotNull Player player, int page) {
        super.open(player, page);
        this.player = player;
    }

    void open(NGUI<QuantumRPG> childMenu, ItemType itemType) {
        this.listening = itemType;
        childMenu.open(player, 1);
    }

    private void reload(JYML cfg) { reload(cfg, null); }

    void reload(JYML cfg, @Nullable Consumer<EditorGUI> consumer) {
        if (cfg.saveChanges()) {
            Player player = this.player;
            itemGeneratorManager.load(EditorGUI.this.itemGenerator.getId(), cfg);
            new BukkitRunnable() {
                @Override
                public void run() {
                    EditorGUI editorGUI = itemGeneratorManager.openEditor(EditorGUI.this.itemGenerator.getId(), player);
                    if (consumer != null) {
                        consumer.accept(editorGUI);
                    }
                }
            }.runTask(plugin);
        } else if (consumer != null) {
            new BukkitRunnable() {
                @Override
                public void run() { consumer.accept(EditorGUI.this); }
            }.runTask(plugin);
        }
    }

    void setDefault(ItemType itemType, JYML cfg) {
        cfg.set(itemType.getPath(), commonItemGenerator.get(itemType.getPath()));
        reload(cfg);
    }

    private void sendSetMessage(ItemType itemType, String currentValue) {
        EditorGUI.this.listening = itemType;
        player.closeInventory();
        String name = itemType.getTitle();
        player.sendMessage("▸ Enter the desired "+name);
        BaseComponent component = new TextComponent("[Current "+name+"]");
        component.setColor(ChatColor.GOLD);
        component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, currentValue));
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Enter the current "+name+" to chat")));
        player.spigot().sendMessage(component);
    }

    private void replaceLore(GuiItem guiItem, String value, int maxLength) {
        ItemStack itemStack = guiItem.getItemRaw();
        if (maxLength > 0) {
            replaceLore(itemStack, value, maxLength);
        } else {
            replaceLore(itemStack, value);
        }
        guiItem.setItem(itemStack);
    }

    private void replaceLore(GuiItem guiItem, List<String> value) {
        ItemStack itemStack = guiItem.getItemRaw();
        replaceLore(itemStack, value);
        guiItem.setItem(itemStack);
    }

    private void replaceLore(ItemStack itemStack, String value, int maxLength) {
        ItemMeta meta = itemStack.getItemMeta(); if (meta == null) { return; }
        List<String> lore = meta.getLore(); if (lore == null) { return; }

        List<String> splitValue = new ArrayList<>();
        while (value.length() > maxLength) {
            int i = value.lastIndexOf(' ', maxLength);
            if (i < 0) { i = maxLength; }
            splitValue.add(value.substring(0, i));
            value = value.substring(i);
        }
        splitValue.add(value);

        for (int i = 0, loreSize = lore.size(); i < loreSize; i++) {
            String line = lore.get(i);
            int pos = line.indexOf(CURRENT_PLACEHOLDER);
            if (pos < 0) { continue; }
            String format = StringUT.getColor(line.substring(0, pos));
            lore.set(i, line.substring(0, pos)+splitValue.get(0));
            for (int j = 1, valueSize = splitValue.size(); j < valueSize; j++) {
                i++;
                lore.add(i, format+splitValue.get(j));
            }
            lore.set(i, lore.get(i)+line.substring(pos+CURRENT_PLACEHOLDER.length()));
        }
        meta.setLore(lore);
        itemStack.setItemMeta(meta);
    }

    private void replaceLore(ItemStack itemStack, String value) {
        ItemMeta meta = itemStack.getItemMeta(); if (meta == null) { return; }
        List<String> lore = meta.getLore(); if (lore == null) { return; }
        for (int i = 0, loreSize = lore.size(); i < loreSize; i++) {
            String line = lore.get(i);
            if (line.contains(CURRENT_PLACEHOLDER)) {
                lore.set(i, line.replace(CURRENT_PLACEHOLDER, value));
            }
        }
        meta.setLore(lore);
        itemStack.setItemMeta(meta);
    }

    private void replaceLore(ItemStack itemStack, List<String> value) {
        ItemMeta meta = itemStack.getItemMeta(); if (meta == null) { return; }
        List<String> lore = meta.getLore(); if (lore == null) { return; }
        if (value.isEmpty()) { return; }
        for (int i = 0, loreSize = lore.size(); i < loreSize; i++) {
            String line = lore.get(i);
            int pos = line.indexOf(CURRENT_PLACEHOLDER);
            if (pos < 0) { continue; }
            String format = StringUT.getColor(line.substring(0, pos));
            lore.set(i, line.replace(CURRENT_PLACEHOLDER, value.get(0)));
            for (int j = 1, size = value.size(); j < size; j++) {
                i++;
                lore.add(i, format+value.get(j));
            }
        }
        meta.setLore(lore);
        itemStack.setItemMeta(meta);
    }

    public ItemGeneratorManager.GeneratorItem getItemGenerator() { return itemGenerator; }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        if (this.listening == null) { return; }
        ItemType itemType = this.listening;
        Player player = event.getPlayer();
        if (!player.equals(this.player)) { return; }
        event.setCancelled(true);
        JYML cfg = itemGenerator.getConfig();
        String message = event.getMessage().strip();
        switch (itemType) {
            case NAME: {
                cfg.set("name", message);
                break;
            }
            case PREFIX_CHANCE: {
                try {
                    cfg.set("generator.prefix-chance", Double.parseDouble(message));
                } catch (NumberFormatException e) { this.listening = null; }
                break;
            }
            case SUFFIX_CHANCE: {
                try {
                    cfg.set("generator.suffix-chance", Double.parseDouble(message));
                } catch (NumberFormatException e) { this.listening = null; }
                break;
            }
            case COLOR: {
                String newColor = null;
                String[] splitString = message.split(",");
                if (splitString.length == 3) {
                    int[] rgb  = new int[3];
                    try {
                        for (int i = 0; i < 3; i++) {
                            rgb[i] = Integer.parseInt(splitString[i].strip());
                        }
                        newColor = rgb[0]+","+rgb[1]+","+rgb[2];
                    } catch (NumberFormatException ignored) { }
                }
                if (newColor == null) {
                    this.listening = null;
                } else { cfg.set("color", newColor); }
                break;
            }
            default: {
                this.listening = null;
                break;
            }
        }
        if (listening == null) {
            // Nothing changed
            plugin.lang().ItemGenerator_Cmd_Editor_Error_InvalidInput.replace("%input%", message).replace("%value%", itemType.getTitle()).send(player);
            return;
        }
        this.listening = null;
        reload(cfg);
    }

    @Override
    protected void onCreate(@NotNull Player player, @NotNull Inventory inventory, int i) { }

    @Override
    protected void onClose(@NotNull Player player, @NotNull InventoryCloseEvent e) {
        if (this.listening != null) { return; }
        this.player = null;
        super.onClose(player, e);
        // TODO fix for children editors
    }

    @Override
    protected boolean ignoreNullClick() { return true; }

    @Override
    protected boolean cancelClick(int i) { return true; }

    @Override
    protected boolean cancelPlayerClick() { return true; }

    public enum ItemType {
        NAME("name"),
        PREFIX_CHANCE("generator.prefix-chance"),
        SUFFIX_CHANCE("generator.suffix-chance"),
        LORE("lore"),
        COLOR("color"),
        UNBREAKABLE("unbreakable"),
        ITEM_FLAGS("item-flags"),
        TIER("tier"),
        MIN_LEVEL("level.min"),
        MAX_LEVEL("level.max"),
        MATERIALS("generator.materials"),
        REQUIREMENTS("user-requirements-by-level"),
        AMMO_TYPES("ammo-types"),
        HAND_TYPES("hand-types"),
        ENCHANTMENTS("enchantments"),
        DAMAGE_TYPES("damage-types"),
        DEFENSE_TYPES("defense-types"),
        ITEM_STATS("item-stats"),
        SOCKETS("sockets"),
        ABILITIES("abilities"),
        SAMPLE(null),
        ;

        private final String path;

        ItemType(String path) { this.path = path; }

        public String getPath() { return path; }

        public String getTitle() {
            return this.name().replace('_', ' ').toLowerCase();
        }
    }
}
