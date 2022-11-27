package su.nightexpress.quantumrpg.modules.list.itemgenerator;

import mc.promcteam.engine.config.api.JYML;
import mc.promcteam.engine.manager.api.gui.ContentType;
import mc.promcteam.engine.manager.api.gui.GuiClick;
import mc.promcteam.engine.manager.api.gui.GuiItem;
import mc.promcteam.engine.manager.api.gui.NGUI;
import mc.promcteam.engine.utils.StringUT;
import mc.promcteam.engine.utils.constants.JStrings;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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

import java.util.*;
import java.util.function.Consumer;

public class EditorGUI extends NGUI<QuantumRPG> {
    private static final String CURRENT_PLACEHOLDER = "%current%";
    
    private final ItemGeneratorManager itemGeneratorManager;
    private final ItemGeneratorManager.GeneratorItem itemGenerator;
    private Player player;
    private ItemType listening;

    EditorGUI(@NotNull ItemGeneratorManager itemGeneratorManager, @NotNull JYML cfg, ItemGeneratorManager.GeneratorItem itemGenerator) {
        super(itemGeneratorManager.plugin, cfg, "editor-gui.");
        this.itemGeneratorManager = itemGeneratorManager;
        this.itemGenerator = itemGenerator;
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
                    switch (type2) {
                        case NAME: {
                            sendSetMessage(type2, EditorGUI.this.itemGenerator.getName().substring("§r§f".length()).replace('§', '&'));
                            break;
                        }
                        case PREFIX_CHANCE: {
                            sendSetMessage(type2, String.valueOf(EditorGUI.this.itemGenerator.getPrefixChance()));
                            break;
                        }
                        case SUFFIX_CHANCE: {
                            sendSetMessage(type2, String.valueOf(EditorGUI.this.itemGenerator.getSuffixChance()));
                            break;
                        }
                        case LORE: {
                            break;
                        }
                        case COLOR: {
                            int[] color = EditorGUI.this.itemGenerator.getColor();
                            sendSetMessage(type2, color[0]+","+color[1]+","+color[2]);
                            break;
                        }
                        case UNBREAKABLE: {
                            JYML cfg = EditorGUI.this.itemGenerator.getConfig();
                            cfg.set("unbreakable", !EditorGUI.this.itemGenerator.isUnbreakable());
                            reload(cfg);
                            break;
                        }
                        case ITEM_FLAGS: {
                            open(new ItemFlagsGUI(plugin, EditorGUI.this.getTitle()+"/Item Flags"), type2);
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

    private void open(NGUI<QuantumRPG> childMenu, ItemType itemType) {
        this.listening = itemType;
        childMenu.open(player, 1);
    }

    private void reload(JYML cfg) { reload(cfg, null); }

    private void reload(JYML cfg, @Nullable Consumer<EditorGUI> consumer) {
        cfg.saveChanges();
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
    }

    private void sendSetMessage(ItemType itemType, String currentValue) {
        EditorGUI.this.listening = itemType;
        player.closeInventory();
        String name = itemType.name().toLowerCase().replace('_', ' ');
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
        Player player = event.getPlayer();
        if (!player.equals(this.player)) { return; }
        event.setCancelled(true);
        JYML cfg = itemGenerator.getConfig();
        String message = event.getMessage().strip();
        switch (this.listening) {
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
                    plugin.lang().ItemGenerator_Cmd_Editor_Error_InvalidInput.replace("%input%", message).replace("%value%", "color").send(player);
                    this.listening = null;
                } else { cfg.set("color", newColor); }
                break;
            }
            default: {
                this.listening = null;
                break;
            }
        }
        if (listening == null) { return; } // Nothing changed
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
        this.itemGeneratorManager.onEditorClose(this);
    }

    @Override
    protected boolean ignoreNullClick() { return true; }

    @Override
    protected boolean cancelClick(int i) { return true; }

    @Override
    protected boolean cancelPlayerClick() { return true; }

    public enum ItemType {
        NAME,
        PREFIX_CHANCE,
        SUFFIX_CHANCE,
        LORE,
        COLOR,
        UNBREAKABLE,
        ITEM_FLAGS,
        TIER,
        MIN_LEVEL,
        MAX_LEVEL,
        MATERIALS,
        REQUIREMENTS,
        AMMO_TYPES,
        HAND_TYPES,
        ENCHANTMENTS,
        DAMAGE_TYPES,
        DEFENSE_TYPES,
        ITEM_STATS,
        SOCKETS,
        ABILITIES,
        SAMPLE,
    }

    public class ItemFlagsGUI extends NGUI<QuantumRPG> {

        public ItemFlagsGUI(@NotNull QuantumRPG plugin, @NotNull String title) {
            super(plugin, title, ((int) Math.ceil((ItemFlag.values().length+1)*1.0/9))*9);
            GuiClick guiClick = (player, type, inventoryClickEvent) -> {
                if (type == null) { return; }
                Class<?> clazz = type.getClass();
                if (clazz.equals(ContentType.class)) {
                    ContentType type2 = (ContentType) type;
                    switch (type2) {
                        case BACK:
                        case RETURN:
                            EditorGUI.this.listening = null;
                            EditorGUI.this.open(player, 1);
                            break;
                        case EXIT: {
                            player.closeInventory();
                            break;
                        }
                    }
                    return;
                }

                String flag = type.name().toLowerCase();
                JYML cfg = EditorGUI.this.itemGenerator.getConfig();
                List<String> itemFlags = cfg.getStringList("item-flags");
                if (itemFlags.contains(JStrings.MASK_ANY)) {
                    itemFlags.remove(JStrings.MASK_ANY);
                    for (ItemFlag itemFlag : ItemFlag.values()) {
                        itemFlags.add(itemFlag.name().toLowerCase());
                    }
                }
                if (itemFlags.contains(flag)) { itemFlags.remove(flag); } else { itemFlags.add(flag); }
                boolean all = true;
                for (ItemFlag itemFlag : ItemFlag.values()) {
                    if (!itemFlags.contains(itemFlag.name().toLowerCase())) {
                        all = false;
                        break;
                    }
                }
                if (all) {
                    itemFlags.clear();
                    itemFlags.add(JStrings.MASK_ANY);
                }
                cfg.set("item-flags", itemFlags);
                EditorGUI.this.reload(cfg, (gui) -> gui.open(gui.new ItemFlagsGUI(plugin, title), ItemType.ITEM_FLAGS));
            };
            Set<ItemFlag> flags = EditorGUI.this.itemGenerator.getFlags();
            for (ItemFlag itemFlag : ItemFlag.values()) {
                Material material;
                switch (itemFlag) {
                    case HIDE_ENCHANTS: {
                        material = Material.ENCHANTED_BOOK;
                        break;
                    }
                    case HIDE_ATTRIBUTES: {
                        material = Material.OAK_SIGN;
                        break;
                    }
                    case HIDE_UNBREAKABLE: {
                        material = Material.ANVIL;
                        break;
                    }
                    case HIDE_DESTROYS: {
                        material = Material.DIAMOND_PICKAXE;
                        break;
                    }
                    case HIDE_PLACED_ON: {
                        material = Material.OAK_PLANKS;
                        break;
                    }
                    case HIDE_POTION_EFFECTS: {
                        material = Material.POTION;
                        break;
                    }
                    case HIDE_DYE: {
                        material = Material.MAGENTA_DYE;
                        break;
                    }
                    default: {
                        material = Material.STONE;
                        break;
                    }
                }
                ItemStack itemStack = new ItemStack(material);
                ItemMeta itemMeta = itemStack.getItemMeta();
                if (itemMeta != null) {
                    itemMeta.setDisplayName(ChatColor.YELLOW+itemFlag.name().toLowerCase());
                    itemMeta.setLore(List.of(ChatColor.AQUA+"Current: "+ChatColor.GREEN+flags.contains(itemFlag), ChatColor.GOLD+"Left-Click: "+ChatColor.YELLOW+"Set"));
                    itemStack.setItemMeta(itemMeta);
                }
                GuiItem guiItem = new GuiItem(itemFlag.name().toLowerCase(), itemFlag, itemStack, false, 0, new TreeMap<>(), Collections.emptyMap(), null, new int[] {itemFlag.ordinal()});
                guiItem.setClick(guiClick);
                this.addButton(guiItem);
            }
            ItemStack itemStack = new ItemStack(Material.BARRIER);
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta != null) {
                itemMeta.setDisplayName(ChatColor.YELLOW.toString()+ChatColor.BOLD+"Return");
                itemStack.setItemMeta(itemMeta);
            }
            GuiItem guiItem = new GuiItem("return", ContentType.RETURN, itemStack, false, 0, new TreeMap<>(), Collections.emptyMap(), null, new int[] {ItemFlag.values().length});
            guiItem.setClick(guiClick);
            this.addButton(guiItem);
        }

        @Override
        protected void onCreate(@NotNull Player player, @NotNull Inventory inventory, int i) { }

        @Override
        protected boolean ignoreNullClick() { return true; }

        @Override
        protected boolean cancelClick(int i) { return true; }

        @Override
        protected boolean cancelPlayerClick() { return true; }

        @Override
        protected void onClose(@NotNull Player player, @NotNull InventoryCloseEvent e) {
            EditorGUI.this.listening = null;
        }
    }
}
