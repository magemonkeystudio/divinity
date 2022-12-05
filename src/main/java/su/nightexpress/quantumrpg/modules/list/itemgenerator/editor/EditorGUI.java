package su.nightexpress.quantumrpg.modules.list.itemgenerator.editor;

import mc.promcteam.engine.config.api.JYML;
import mc.promcteam.engine.manager.api.gui.ContentType;
import mc.promcteam.engine.manager.api.gui.GuiClick;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.ItemGeneratorManager;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.requirements.MainRequirementsGUI;

import java.util.ArrayList;
import java.util.List;

public class EditorGUI extends AbstractEditorGUI {
    private ItemType listening;

    public EditorGUI(@NotNull ItemGeneratorManager itemGeneratorManager, ItemGeneratorManager.GeneratorItem itemGenerator) {
        super(itemGeneratorManager, itemGenerator, 45);
    }

    @Override
    protected void onCreate(@NotNull Player player, @NotNull Inventory inventory, int page) {
        GuiClick guiClick = new GuiClick() {
            @Override
            public void click(@NotNull Player player, @Nullable Enum<?> type, @NotNull InventoryClickEvent clickEvent) {
                if (type == null) { return; }
                Class<?> clazz = type.getClass();
                if (clazz.equals(ContentType.class)) {
                    ContentType type2 = (ContentType) type;
                    switch (type2) {
                        case EXIT: case RETURN: {
                            player.closeInventory();
                            break;
                        }
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
                                    setDefault(type2);
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
                                    setDefault(type2);
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
                                    setDefault(type2);
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
                                    setDefault(type2);
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
                                    setDefault(type2);
                                    break;
                                }
                                default: {
                                    EditorGUI.this.itemGenerator.getConfig().set(type2.getPath(), !EditorGUI.this.itemGenerator.isUnbreakable());
                                    saveAndReopen();
                                    break;
                                }
                            }
                            break;
                        }
                        case ITEM_FLAGS: {
                            switch (clickType) {
                                case DROP: case CONTROL_DROP: {
                                    setDefault(type2);
                                    break;
                                }
                                default: {
                                    new ItemFlagsGUI(itemGeneratorManager, itemGenerator, EditorGUI.this.getTitle()).open(player, 1);
                                    break;
                                }
                            }
                            break;
                        }
                        case TIER: {
                            switch (clickType) {
                                case DROP: case CONTROL_DROP: {
                                    setDefault(type2);
                                    break;
                                }
                                default: {
                                    new TierGUI(itemGeneratorManager, itemGenerator, EditorGUI.this.getTitle()).open(player, 1);
                                    break;
                                }
                            }
                            break;
                        }
                        case MATERIALS: {
                            break;
                        }
                        case MIN_LEVEL: {
                            switch (clickType) {
                                case MIDDLE: {
                                    sendSetMessage(type2, String.valueOf(EditorGUI.this.itemGenerator.getMinLevel()));
                                    break;
                                }
                                case LEFT: {
                                    EditorGUI.this.itemGenerator.getConfig().set(type2.getPath(), EditorGUI.this.itemGenerator.getMinLevel()-1);
                                    saveAndReopen();
                                    break;
                                }
                                case RIGHT: {
                                    EditorGUI.this.itemGenerator.getConfig().set(type2.getPath(), EditorGUI.this.itemGenerator.getMinLevel()+1);
                                    saveAndReopen();
                                    break;
                                }
                                case DROP: case CONTROL_DROP: {
                                    setDefault(type2);
                                    break;
                                }
                            }
                            break;
                        }
                        case MAX_LEVEL: {
                            switch (clickType) {
                                case MIDDLE: {
                                    sendSetMessage(type2, String.valueOf(EditorGUI.this.itemGenerator.getMaxLevel()));
                                    break;
                                }
                                case LEFT: {
                                    EditorGUI.this.itemGenerator.getConfig().set(type2.getPath(), EditorGUI.this.itemGenerator.getMaxLevel()-1);
                                    saveAndReopen();
                                    break;
                                }
                                case RIGHT: {
                                    EditorGUI.this.itemGenerator.getConfig().set(type2.getPath(), EditorGUI.this.itemGenerator.getMaxLevel()+1);
                                    saveAndReopen();
                                    break;
                                }
                                case DROP: case CONTROL_DROP: {
                                    setDefault(type2);
                                    break;
                                }
                            }
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
                            new MainRequirementsGUI(itemGeneratorManager, itemGenerator).open(player, 1);
                            break;
                        }
                        case ENCHANTMENTS: {
                            break;
                        }
                        case ABILITIES: {
                            break;
                        }
                        case SAMPLE: {
                            EditorGUI.this.addButton(EditorGUI.this.createButton("sample", ItemType.SAMPLE, EditorGUI.this.itemGenerator.create(-1, -1, null), 40, this));
                            player.closeInventory();
                            EditorGUI.this.open(player, 1);
                            break;
                        }
                    }
                }
            }
        };
        this.addButton(this.createButton("name", ItemType.NAME, Material.NAME_TAG,
                                         "&eName format", replaceLore(List.of(
                                                 "&bCurrent: &a%current%",
                                                 "&6Left-Click: &eSet",
                                                 "&6Drop: &eSet to default value"), itemGenerator.getName().substring("§r§f".length()), 30), 0, guiClick));
        this.addButton(this.createButton("prefix-chance", ItemType.PREFIX_CHANCE, Material.BROWN_MUSHROOM,
                                         "&ePrefix Chance", List.of(
                                                 "&bCurrent: &a"+itemGenerator.getPrefixChance(),
                                                 "&6Left-Click: &eSet",
                                                 "&6Drop: &eSet to default value"), 1, guiClick));
        this.addButton(this.createButton("suffix-chance", ItemType.SUFFIX_CHANCE, Material.RED_MUSHROOM,
                                         "&eSuffix Chance", List.of(
                                                 "&bCurrent: &a"+itemGenerator.getSuffixChance(),
                                                 "&6Left-Click: &eSet",
                                                 "&6Drop: &eSet to default value"), 2, guiClick));
        this.addButton(this.createButton("lore", ItemType.LORE, Material.BOOK,
                                         "&eLore format", List.of(
                                                 "&bCurrent:",
                                                 "&a----------",
                                                 "&r%current%",
                                                 "&a----------",
                                                 "&6Left-Click: &eSet"), 3, guiClick));
        int[] color = itemGenerator.getColor();
        this.addButton(this.createButton("color", ItemType.COLOR, Material.MAGENTA_DYE,
                                         "&eColor", List.of(
                                                 "&bCurrent: &a"+color[0]+","+color[1]+","+color[2],
                                                 "&6Left-Click: &eSet",
                                                 "&6Drop: &eSet to default value"), 4, guiClick));
        this.addButton(this.createButton("unbreakable", ItemType.UNBREAKABLE, Material.ANVIL,
                                         "&eUnbreakable", List.of(
                                                 "&bCurrent: &a"+itemGenerator.isUnbreakable(),
                                                 "&6Left-Click: &eToggle",
                                                 "&6Drop: &eSet to default value"), 5, guiClick));
        List<String> itemFlags = new ArrayList<>();
        for (ItemFlag flag : itemGenerator.getFlags()) { itemFlags.add(flag.name().toLowerCase()); }
        this.addButton(this.createButton("item-flags", ItemType.ITEM_FLAGS, Material.OAK_SIGN,
                                         "&eItemFlags", replaceLore(List.of(
                                                 "&bCurrent:",
                                                 "&a%current%",
                                                 "&6Left-Click: &eSet",
                                                 "&6Drop: &eSet to default value"), itemFlags), 6, guiClick));
        this.addButton(this.createButton("tier", ItemType.TIER, Material.DIAMOND,
                                         "&eTier", replaceLore(List.of(
                                                 "&bCurrent: &a%current%",
                                                 "&6Left-Click: &eSet"), this.itemGenerator.getTier().getName(), 30), 7, guiClick));
        this.addButton(this.createButton("materials", ItemType.MATERIALS, Material.IRON_INGOT,
                                         "&eMaterials", List.of(
                                                 "&6Left-Click: &eSet"), 8, guiClick));
        this.addButton(this.createButton("min-level", ItemType.MIN_LEVEL, Material.EXPERIENCE_BOTTLE,
                                         "&eMinimum Level", List.of(
                                                 "&bCurrent: &a"+this.itemGenerator.getMinLevel(),
                                                 "&6Middle-Click: &eSet",
                                                 "&6Left-Click: &eIncrease",
                                                 "&6Right-Click: &eDecrease",
                                                 "&6Drop: &eSet to default value"), 11, guiClick));
        this.addButton(this.createButton("max-level", ItemType.MAX_LEVEL, Material.EXPERIENCE_BOTTLE,
                                         "&eMaximum Level", List.of(
                                                 "&bCurrent: &a"+this.itemGenerator.getMaxLevel(),
                                                 "&6Middle-Click: &eSet",
                                                 "&6Left-Click: &eIncrease",
                                                 "&6Right-Click: &eDecrease",
                                                 "&6Drop: &eSet to default value"), 12, guiClick));
        this.addButton(this.createButton("ammo-types", ItemType.AMMO_TYPES, Material.ARROW,
                                         "&eAmmo Types", List.of(
                                                 "&bCurrent:",
                                                 "&r%current%",
                                                 "&6Left-Click: &eIncrease",
                                                 "&6Right-Click: &eDecrease"), 14, guiClick));
        this.addButton(this.createButton("hand-types", ItemType.HAND_TYPES, Material.STICK,
                                         "&eHand Types", List.of(
                                                 "&bCurrent:",
                                                 "&r%current%",
                                                 "&6Left-Click: &eSet"), 15, guiClick));
        this.addButton(this.createButton("damage-types", ItemType.DAMAGE_TYPES, Material.IRON_SWORD,
                                         "&eDamage Types", List.of(
                                                 "&6Left-Click: &eSet"), 20, guiClick));
        this.addButton(this.createButton("defense-types", ItemType.DEFENSE_TYPES, Material.IRON_CHESTPLATE,
                                         "&eDefense Types", List.of(
                                                 "&6Left-Click: &eSet"), 21, guiClick));
        this.addButton(this.createButton("item-stats", ItemType.ITEM_STATS, Material.PAPER,
                                         "&eItem Stats", List.of(
                                                "&6Left-Click: &eSet"), 23, guiClick));
        this.addButton(this.createButton("sockets", ItemType.SOCKETS, Material.EMERALD,
                                         "&eSockets", List.of(
                                                "&6Left-Click: &eSet"), 24, guiClick));
        this.addButton(this.createButton("requirements", ItemType.REQUIREMENTS, Material.REDSTONE,
                                         "&eRequirements", List.of(
                                                 "&6Left-Click: &eSet"), 29, guiClick));
        this.addButton(this.createButton("enchantments", ItemType.ENCHANTMENTS, Material.FIRE_CHARGE,
                                         "&eAbilities", List.of(
                                                 "&6Left-Click: &eSet"), 33, guiClick));
        this.addButton(this.createButton("sample", ItemType.SAMPLE, this.itemGenerator.create(-1, -1, null), 40, guiClick));
        this.addButton(this.createButton("exit", ContentType.EXIT, Material.BARRIER, "&c&lExit", List.of(), 44, guiClick));
    }

    private void setDefault(ItemType itemType) {
        this.itemGenerator.getConfig().set(itemType.getPath(), commonItemGenerator.get(itemType.getPath()));
        saveAndReopen();
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

    @Override
    public void onChat(AsyncPlayerChatEvent event) {
        if (this.listening == null) { return; }
        ItemType itemType = this.listening;
        Player player = event.getPlayer();
        event.setCancelled(true);
        JYML cfg = itemGenerator.getConfig();
        String message = event.getMessage().strip();
        switch (itemType) {
            case NAME: {
                cfg.set(itemType.getPath(), message);
                break;
            }
            case PREFIX_CHANCE: case SUFFIX_CHANCE: {
                try {
                    cfg.set(itemType.getPath(), Double.parseDouble(message));
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
                } else { cfg.set(itemType.getPath(), newColor); }
                break;
            }
            case MIN_LEVEL: case MAX_LEVEL: {
                try {
                    cfg.set(itemType.getPath(), Integer.parseInt(message));
                } catch (NumberFormatException e) { this.listening = null; }
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
        }
        this.listening = null;
        saveAndReopen();
    }

    @Override
    protected void onClose(@NotNull Player player, @NotNull InventoryCloseEvent e) {

    }

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

        public String getTitle() { return this.name().replace('_', ' ').toLowerCase(); }
    }
}
