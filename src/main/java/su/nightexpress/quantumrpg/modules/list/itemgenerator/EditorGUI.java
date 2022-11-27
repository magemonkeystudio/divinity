package su.nightexpress.quantumrpg.modules.list.itemgenerator;

import mc.promcteam.engine.config.api.JYML;
import mc.promcteam.engine.manager.api.gui.ContentType;
import mc.promcteam.engine.manager.api.gui.GuiClick;
import mc.promcteam.engine.manager.api.gui.GuiItem;
import mc.promcteam.engine.manager.api.gui.NGUI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.quantumrpg.QuantumRPG;

import java.util.ArrayList;
import java.util.List;

public class EditorGUI extends NGUI<QuantumRPG> {
    private final ItemGeneratorManager itemGeneratorManager;
    private final ItemGeneratorManager.GeneratorItem itemGenerator;
    private Player player;
    private ItemType chatListening;

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
                            sendSetMessage(type2, itemGenerator.getName().substring("§r§f".length()).replace('§', '&'));
                            break;
                        }
                        case PREFIX_CHANCE: {
                            sendSetMessage(type2, String.valueOf(itemGenerator.getPrefixChance()));
                            break;
                        }
                        case SUFFIX_CHANCE: {
                            sendSetMessage(type2, String.valueOf(itemGenerator.getSuffixChance()));
                            break;
                        }
                        case LORE: {
                            break;
                        }
                        case COLOR: {
                            int[] color = itemGenerator.getColor();
                            sendSetMessage(type2, color[0]+","+color[1]+","+color[2]);
                            break;
                        }
                        case UNBREAKABLE: {
                            JYML cfg = itemGenerator.getConfig();
                            cfg.set("unbreakable", !itemGenerator.isUnbreakable());
                            reload(cfg);
                            break;
                        }
                        case ITEM_FLAGS: {
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
                    ItemType itemType = (ItemType) type;
                    switch (itemType) {
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

    private void reload(JYML cfg) {
        cfg.saveChanges();
        itemGeneratorManager.reload(itemGenerator.getId());
        new BukkitRunnable() {
            @Override
            public void run() { itemGeneratorManager.openEditor(itemGenerator.getId(), player); }
        }.runTask(plugin);
    }

    private void sendSetMessage(ItemType itemType, String currentValue) {
        EditorGUI.this.chatListening = itemType;
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
            replaceLore(itemStack, "%current%", value, maxLength);
        } else {
            replaceLore(itemStack, "%current%", value);
        }
        guiItem.setItem(itemStack);
    }

    private void replaceLore(ItemStack itemStack, String placeholder, String value, int maxLength) {
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
            int pos = line.indexOf(placeholder);
            if (pos < 0) { break; }
            String format;
            {
                StringBuilder builder = new StringBuilder("");
                int j = 0;
                while (true) {
                    int t = line.indexOf('§', j);
                    j = line.indexOf('&', j);
                    if (t >= 0 && (j < 0 || t < j)) { j = t; }
                    if (j >= 0) {
                        j++;
                        if (j > pos) { break; }
                        ChatColor color = ChatColor.getByChar(line.charAt(j));
                        if (color != null) { builder.append(color); }
                    } else {
                        break;
                    }
                }
                format = builder.toString();
            }
            lore.set(i, line.substring(0, pos)+format+splitValue.get(0));
            for (int j = 1, valueSize = splitValue.size(); j < valueSize; j++) {
                lore.add(i+1, format+splitValue.get(j));
                i++;
            }
            lore.set(i, lore.get(i)+line.substring(pos+placeholder.length()));
        }
        meta.setLore(lore);
        itemStack.setItemMeta(meta);
    }

    private void replaceLore(ItemStack itemStack, String placeholder, String value) {
        ItemMeta meta = itemStack.getItemMeta(); if (meta == null) { return; }
        List<String> lore = meta.getLore(); if (lore == null) { return; }
        for (int i = 0, loreSize = lore.size(); i < loreSize; i++) {
            String line = lore.get(i);
            if (line.contains(placeholder)) {
                lore.set(i, line.replace(placeholder, value));
            }
        }
        meta.setLore(lore);
        itemStack.setItemMeta(meta);
    }

    public ItemGeneratorManager.GeneratorItem getItemGenerator() { return itemGenerator; }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        if (this.chatListening == null) { return; }
        Player player = event.getPlayer();
        if (!player.equals(this.player)) { return; }
        event.setCancelled(true);
        JYML cfg = itemGenerator.getConfig();
        String message = event.getMessage().strip();
        switch (this.chatListening) {
            case NAME: {
                cfg.set("name", message);
                break;
            }
            case PREFIX_CHANCE: {
                try {
                    cfg.set("generator.prefix-chance", Double.parseDouble(message));
                } catch (NumberFormatException e) { this.chatListening = null; }
                break;
            }
            case SUFFIX_CHANCE: {
                try {
                    cfg.set("generator.suffix-chance", Double.parseDouble(message));
                } catch (NumberFormatException e) { this.chatListening = null; }
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
                    this.chatListening = null;
                } else { cfg.set("color", newColor); }
                break;
            }
            default: {
                this.chatListening = null;
                break;
            }
        }
        if (chatListening == null) { return; } // Nothing changed
        this.chatListening = null;
        reload(cfg);
    }

    @Override
    protected void onCreate(@NotNull Player player, @NotNull Inventory inventory, int i) { }

    @Override
    protected void onClose(@NotNull Player player, @NotNull InventoryCloseEvent e) {
        if (this.chatListening != null) { return; }
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
        ;
    }
}
