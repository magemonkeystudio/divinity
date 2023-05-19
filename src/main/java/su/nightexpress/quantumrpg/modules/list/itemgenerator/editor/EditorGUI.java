package su.nightexpress.quantumrpg.modules.list.itemgenerator.editor;

import mc.promcteam.engine.config.api.JYML;
import mc.promcteam.engine.manager.api.gui.ContentType;
import mc.promcteam.engine.manager.api.gui.GuiClick;
import mc.promcteam.engine.manager.api.gui.GuiItem;
import mc.promcteam.engine.utils.ItemUT;
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
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.ItemGeneratorManager;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.enchantments.EnchantmentsGUI;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.materials.MainMaterialsGUI;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.requirements.MainRequirementsGUI;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.skills.MainSkillsGUI;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.sockets.MainSocketsGUI;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.stats.MainStatsGUI;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EditorGUI extends AbstractEditorGUI {
    private ItemType listening;

    public EditorGUI(@NotNull ItemGeneratorManager itemGeneratorManager, ItemGeneratorManager.GeneratorItem itemGenerator) {
        super(itemGeneratorManager, itemGenerator, 45);
    }

    @Override
    protected void onCreate(@NotNull Player player, @NotNull Inventory inventory, int page) {
        GuiClick guiClick = (player1, type, clickEvent) -> {
            if (type == null) { return; }
            Class<?> clazz = type.getClass();
            if (clazz.equals(ContentType.class)) {
                ContentType type2 = (ContentType) type;
                switch (type2) {
                    case EXIT: case RETURN: {
                        player1.closeInventory();
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
                                sendSetMessage(type2, this.itemGenerator.getName().substring("§r§f".length()).replace('§', '&'));
                                break;
                            }
                        }
                        break;
                    }
                    case MATERIALS: {
                        new MainMaterialsGUI(this.itemGeneratorManager, this.itemGenerator).open(player1, 1);
                        break;
                    }
                    case LORE: {
                        switch (clickType) {
                            case DROP: case CONTROL_DROP: {
                                setDefault(type2);
                                break;
                            }
                            default: {
                                new LoreGUI(this.itemGeneratorManager, this.itemGenerator, ItemType.LORE.getPath(), "[&d"+itemGenerator.getId()+"&r] editor/"+ItemType.LORE.getTitle(), () -> new EditorGUI(this.itemGeneratorManager, this.itemGenerator).open(player1, 1)).open(player1, 1);
                                break;
                            }
                        }
                        break;
                    }
                    case MODEL_DATA: case DURABILITY: {
                        switch (clickType) {
                            case MIDDLE: {
                                sendSetMessage(type2, String.valueOf(this.itemGenerator.getConfig().getInt(type2.getPath(), -1)));
                                break;
                            }
                            case LEFT: {
                                this.itemGenerator.getConfig().set(type2.getPath(), this.itemGenerator.getConfig().getInt(type2.getPath(), -1)-1);
                                saveAndReopen();
                                break;
                            }
                            case RIGHT: {
                                this.itemGenerator.getConfig().set(type2.getPath(), this.itemGenerator.getConfig().getInt(type2.getPath(), -1)+1);
                                saveAndReopen();
                                break;
                            }
                            case DROP: case CONTROL_DROP: {
                                this.itemGenerator.getConfig().remove(type2.getPath());
                                saveAndReopen();
                                break;
                            }
                        }
                        break;
                    }
                    case COLOR: {
                        switch (clickType) {
                            case DROP: case CONTROL_DROP: {
                                setDefault(type2);
                                break;
                            }
                            default: {
                                int[] color = this.itemGenerator.getColor();
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
                                this.itemGenerator.getConfig().set(type2.getPath(), !this.itemGenerator.isUnbreakable());
                                saveAndReopen();
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
                                sendSetMessage(type2, String.valueOf(this.itemGenerator.getPrefixChance()));
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
                                sendSetMessage(type2, String.valueOf(this.itemGenerator.getSuffixChance()));
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
                                new ItemFlagsGUI(this.itemGeneratorManager, this.itemGenerator).open(player1, 1);
                                break;
                            }
                        }
                        break;
                    }
                    case ENCHANTED: {
                        switch (clickType) {
                            case DROP: case CONTROL_DROP: {
                                this.itemGenerator.getConfig().remove(type2.getPath());
                                saveAndReopen();
                                break;
                            }
                            default: {
                                this.itemGenerator.getConfig().set(type2.getPath(), !this.itemGenerator.getConfig().getBoolean(type2.getPath()));
                                saveAndReopen();
                                break;
                            }
                        }
                        break;
                    }
                    case SKULL_HASH: {
                        switch (clickType) {
                            case DROP: case CONTROL_DROP: {
                                this.itemGenerator.getConfig().remove(type2.getPath());
                                saveAndReopen();
                                break;
                            }
                            default: {
                                String current = this.itemGenerator.getConfig().getString(type2.getPath());
                                sendSetMessage(type2, current == null ? "" : current);
                                break;
                            }
                        }
                        break;
                    }
                    case MIN_LEVEL: {
                        switch (clickType) {
                            case MIDDLE: {
                                sendSetMessage(type2, String.valueOf(this.itemGenerator.getMinLevel()));
                                break;
                            }
                            case LEFT: {
                                this.itemGenerator.getConfig().set(type2.getPath(), this.itemGenerator.getMinLevel()-1);
                                saveAndReopen();
                                break;
                            }
                            case RIGHT: {
                                this.itemGenerator.getConfig().set(type2.getPath(), this.itemGenerator.getMinLevel()+1);
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
                                sendSetMessage(type2, String.valueOf(this.itemGenerator.getMaxLevel()));
                                break;
                            }
                            case LEFT: {
                                this.itemGenerator.getConfig().set(type2.getPath(), this.itemGenerator.getMaxLevel()-1);
                                saveAndReopen();
                                break;
                            }
                            case RIGHT: {
                                this.itemGenerator.getConfig().set(type2.getPath(), this.itemGenerator.getMaxLevel()+1);
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
                    case TIER: {
                        switch (clickType) {
                            case DROP: case CONTROL_DROP: {
                                setDefault(type2);
                                break;
                            }
                            default: {
                                new TierGUI(this.itemGeneratorManager, this.itemGenerator).open(player1, 1);
                                break;
                            }
                        }
                        break;
                    }
                    case AMMO_TYPES: {
                        new AmmoTypesGUI(this.itemGeneratorManager, this.itemGenerator).open(player1, 1);
                        break;
                    }
                    case HAND_TYPES: {
                        new HandTypesGUI(this.itemGeneratorManager, this.itemGenerator).open(player1, 1);
                        break;
                    }
                    case DAMAGE_TYPES: case DEFENSE_TYPES: case ITEM_STATS: case SKILLAPI_ATTRIBUTES: {
                        new MainStatsGUI(this.itemGeneratorManager, this.itemGenerator, type2).open(player1, 1);
                        break;
                    }
                    case SOCKETS: {
                        new MainSocketsGUI(this.itemGeneratorManager, this.itemGenerator).open(player1, 1);
                        break;
                    }
                    case REQUIREMENTS: {
                        new MainRequirementsGUI(this.itemGeneratorManager, this.itemGenerator).open(player1, 1);
                        break;
                    }
                    case ENCHANTMENTS: {
                        new EnchantmentsGUI(this.itemGeneratorManager, this.itemGenerator).open(player1, 1);
                        break;
                    }
                    case SKILLS: {
                        new MainSkillsGUI(this.itemGeneratorManager, this.itemGenerator).open(player1, 1);
                        break;
                    }
                    case USES_BY_LEVEL: {
                        new UsesByLevelGUI(this.itemGeneratorManager, this.itemGenerator).open(player1, 1);
                        break;
                    }
                    case SAMPLE: {
                        saveAndReopen();
                        break;
                    }
                }
            }
        };
        this.addButton(this.createButton("name", ItemType.NAME, Material.NAME_TAG,
                                         "&eName format", StringUT.replace(color(List.of(
                                                 "&bCurrent: &a%current%",
                                                 "&6Left-Click: &eSet",
                                                 "&6Drop: &eSet to default value")), CURRENT_PLACEHOLDER, StringUT.wrap(itemGenerator.getName().substring("§r§f".length()), 30)), 0, guiClick));
        this.addButton(this.createButton("materials", ItemType.MATERIALS, Material.IRON_INGOT,
                                         "&eMaterials", List.of(
                                                 "&6Left-Click: &eModify"), 1, guiClick));
        this.addButton(this.createButton("lore", ItemType.LORE, Material.WRITABLE_BOOK,
                                         "&eLore format", StringUT.replace(color(List.of(
                                                 "&bCurrent:",
                                                 "&a----------",
                                                 "&f%current%",
                                                 "&a----------",
                                                 "&6Left-Click: &eModify")), CURRENT_PLACEHOLDER, itemGenerator.getConfig().getStringList(ItemType.LORE.getPath())), 2, guiClick));
        this.addButton(this.createButton("model-data", ItemType.MODEL_DATA, Material.END_CRYSTAL,
                                         "&eCustom Model Data", List.of(
                                                 "&bCurrent: &a"+this.itemGenerator.getConfig().getInt(ItemType.MODEL_DATA.getPath(), -1),
                                                 "&6Middle-Click: &eSet",
                                                 "&6Left-Click: &eDecrease",
                                                 "&6Right-Click: &eIncrease",
                                                 "&6Drop: &eSet to default value"), 3, guiClick));
        int[] color = itemGenerator.getColor();
        this.addButton(this.createButton("color", ItemType.COLOR, Material.MAGENTA_DYE,
                                         "&eColor", List.of(
                                                 "&bCurrent: &a"+color[0]+","+color[1]+","+color[2],
                                                 "&6Left-Click: &eSet",
                                                 "&6Drop: &eSet to default value"), 4, guiClick));
        this.addButton(this.createButton("durability", ItemType.DURABILITY, Material.ANVIL,
                                         "&eDurability", List.of(
                                                 "&bCurrent: &a"+this.itemGenerator.getConfig().getInt(ItemType.DURABILITY.getPath(), -1),
                                                 "&6Middle-Click: &eSet",
                                                 "&6Left-Click: &eDecrease",
                                                 "&6Right-Click: &eIncrease",
                                                 "&6Drop: &eSet to default value"), 5, guiClick));
        GuiItem guiItem = this.createButton("unbreakable", ItemType.UNBREAKABLE, Material.ELYTRA,
                                            "&eUnbreakable", List.of("&bCurrent: &a"+itemGenerator.isUnbreakable(),
                                                                     "&6Left-Click: &eToggle",
                                                                     "&6Drop: &eSet to default value"), 6, guiClick);
        if (!itemGenerator.isUnbreakable()) {
            ItemStack itemStack = guiItem.getItemRaw();
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta instanceof Damageable) {
                ((Damageable) itemMeta).setDamage(432);
                itemStack.setItemMeta(itemMeta);
                guiItem.setItem(itemStack);
            }
        }
        this.addButton(guiItem);
        this.addButton(this.createButton("prefix-chance", ItemType.PREFIX_CHANCE, Material.BROWN_MUSHROOM,
                                         "&ePrefix Chance", List.of(
                                                 "&bCurrent: &a"+itemGenerator.getPrefixChance(),
                                                 "&6Left-Click: &eSet",
                                                 "&6Drop: &eSet to default value"), 7, guiClick));
        this.addButton(this.createButton("suffix-chance", ItemType.SUFFIX_CHANCE, Material.RED_MUSHROOM,
                                         "&eSuffix Chance", List.of(
                                                 "&bCurrent: &a"+itemGenerator.getSuffixChance(),
                                                 "&6Left-Click: &eSet",
                                                 "&6Drop: &eSet to default value"), 8, guiClick));
        List<String> lore = new ArrayList<>();
        for (ItemFlag flag : itemGenerator.getFlags()) { lore.add("- "+flag.name().toLowerCase()); }
        this.addButton(this.createButton("item-flags", ItemType.ITEM_FLAGS, Material.OAK_SIGN,
                                         "&eItemFlags", StringUT.replace(color(List.of(
                                                 "&bCurrent:",
                                                 "&a%current%",
                                                 "&6Left-Click: &eModify",
                                                 "&6Drop: &eSet to default value")), CURRENT_PLACEHOLDER, lore), 12, guiClick));
        boolean enchanted = this.itemGenerator.getConfig().getBoolean(ItemType.ENCHANTED.getPath(), false);
        this.addButton(this.createButton("enchanted", ItemType.ENCHANTED, enchanted ? Material.ENCHANTED_BOOK : Material.BOOK,
                                         "&eEnchanted", List.of(
                                                 "&bCurrent: &a"+enchanted,
                                                 "&6Left-Click: &eToggle",
                                                 "&6Drop: &eSet to default value"), 13, guiClick));
        String hash = this.itemGenerator.getConfig().getString(ItemType.SKULL_HASH.getPath());
        guiItem = this.createButton("skull-hash", ItemType.SKULL_HASH, Material.PLAYER_HEAD,
                                    "&eSkull Hash", StringUT.replace(color(List.of(
                                            "&bCurrent: &a%current%",
                                            "&6Left-Click: &eSet",
                                            "&6Drop: &eSet to default value")), CURRENT_PLACEHOLDER, StringUT.wrap(hash == null ? "\"\"" : hash, 30)), 14, guiClick);

        if (hash != null) {
            ItemStack itemStack = guiItem.getItemRaw();
            ItemUT.addSkullTexture(itemStack, hash, this.itemGenerator.getId());
            guiItem.setItem(itemStack);
        }
        this.addButton(guiItem);
        this.addButton(this.createButton("min-level", ItemType.MIN_LEVEL, Material.EXPERIENCE_BOTTLE,
                                         "&eMinimum Level", List.of(
                                                 "&bCurrent: &a"+this.itemGenerator.getMinLevel(),
                                                 "&6Middle-Click: &eSet",
                                                 "&6Left-Click: &eDecrease",
                                                 "&6Right-Click: &eIncrease",
                                                 "&6Drop: &eSet to default value"), 20, guiClick));
        this.addButton(this.createButton("max-level", ItemType.MAX_LEVEL, Material.EXPERIENCE_BOTTLE,
                                         "&eMaximum Level", List.of(
                                                 "&bCurrent: &a"+this.itemGenerator.getMaxLevel(),
                                                 "&6Middle-Click: &eSet",
                                                 "&6Left-Click: &eDecrease",
                                                 "&6Right-Click: &eIncrease",
                                                 "&6Drop: &eSet to default value"), 21, guiClick));
        this.addButton(this.createButton("tier", ItemType.TIER, Material.DIAMOND,
                                         "&eTier", StringUT.replace(color(List.of(
                                                 "&bCurrent: &a%current%",
                                                 "&6Left-Click: &eSet")), CURRENT_PLACEHOLDER, StringUT.wrap(this.itemGenerator.getTier().getName(), 12)), 22,
                guiClick));
        lore = new ArrayList<>();
        ConfigurationSection ammoSection = this.itemGenerator.getConfig().getConfigurationSection(ItemType.AMMO_TYPES.getPath());
        if (ammoSection != null) {
            for (String ammoType : ammoSection.getKeys(false)) {
                lore.add("&a "+ammoType+": &f"+ammoSection.getDouble(ammoType));
            }
        }
        this.addButton(this.createButton("ammo-types", ItemType.AMMO_TYPES, Material.ARROW,
                                         "&eAmmo Types", StringUT.replace(color(List.of(
                                                 "&bCurrent:",
                                                 "%current%",
                                                 "&6Left-Click: &eModify")), CURRENT_PLACEHOLDER, lore), 23, guiClick));
        List<String> handTypes = new ArrayList<>();
        ConfigurationSection handSection = this.itemGenerator.getConfig().getConfigurationSection(ItemType.HAND_TYPES.getPath());
        if (handSection != null) {
            for (String handType : handSection.getKeys(false)) {
                handTypes.add("&a "+handType+": &f"+handSection.getDouble(handType));
            }
        }
        this.addButton(this.createButton("hand-types", ItemType.HAND_TYPES, Material.STICK,
                                         "&eHand Types", StringUT.replace(color(List.of(
                                                 "&bCurrent:",
                                                 "%current%",
                                                 "&6Left-Click: &eModify")), CURRENT_PLACEHOLDER, handTypes), 24, guiClick));
        this.addButton(this.createButton("damage-types", ItemType.DAMAGE_TYPES, Material.IRON_SWORD,
                                         "&eDamage Types", List.of(
                                                 "&6Left-Click: &eModify"), 27, guiClick));
        this.addButton(this.createButton("defense-types", ItemType.DEFENSE_TYPES, Material.IRON_CHESTPLATE,
                                         "&eDefense Types", List.of(
                                                 "&6Left-Click: &eModify"), 28, guiClick));
        this.addButton(this.createButton("item-stats", ItemType.ITEM_STATS, Material.PAPER,
                                         "&eItem Stats", List.of(
                                                "&6Left-Click: &eModify"), 29, guiClick));
        this.addButton(this.createButton("strillapi-attributes", ItemType.SKILLAPI_ATTRIBUTES, Material.BOOK,
                                         "&eSkillAPI Attributes", List.of(
                                                 "&6Left-Click: &eModify"), 30, guiClick));
        this.addButton(this.createButton("sockets", ItemType.SOCKETS, Material.EMERALD,
                                         "&eSockets", List.of(
                                                "&6Left-Click: &eModify"), 31, guiClick));
        this.addButton(this.createButton("requirements", ItemType.REQUIREMENTS, Material.REDSTONE,
                                         "&eRequirements", List.of(
                                                 "&6Left-Click: &eModify"), 32, guiClick));
        this.addButton(this.createButton("enchantments", ItemType.ENCHANTMENTS, Material.ENCHANTED_BOOK,
                                         "&eEnchantments", List.of(
                                                 "&6Left-Click: &eModify"), 33, guiClick));
        this.addButton(this.createButton("skills", ItemType.SKILLS, Material.FIRE_CHARGE,
                                         "&eSkills", List.of(
                                                 "&6Left-Click: &eModify"), 34, guiClick));
        lore = new ArrayList<>();
        for (Map.Entry<Integer,Integer> entry : UsesByLevelGUI.getUsesByLevel(this.itemGenerator.getConfig()).entrySet()) {
            lore.add("&a "+entry.getKey()+": &f"+entry.getValue());
        }
        this.addButton(this.createButton("uses-by-level", ItemType.USES_BY_LEVEL, Material.CAULDRON,
                                         "&eUses by level", StringUT.replace(List.of(
                                                 "&bCurrent:",
                                                 "&a%current%",
                                                 "&6Left-Click: &eModify"), CURRENT_PLACEHOLDER, lore), 35, guiClick));
        this.addButton(this.createButton("sample", ItemType.SAMPLE, this.itemGenerator.create(-1, -1, null), 40, guiClick));
        this.addButton(this.createButton("exit", ContentType.EXIT, Material.BARRIER, "&c&lExit", List.of(), 44, guiClick));
    }

    private void setDefault(ItemType itemType) {
        setDefault(itemType.getPath());
        saveAndReopen();
    }

    private void sendSetMessage(ItemType itemType, String currentValue) {
        this.listening = itemType;
        player.closeInventory();
        String name = itemType.getTitle();
        player.sendMessage("▸ Enter the desired "+name+", or \"cancel\" to go back");
        BaseComponent component = new TextComponent("[Current "+name+"]");
        component.setColor(ChatColor.GOLD);
        component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, currentValue));
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Enter the current "+name+" to chat")));
        player.spigot().sendMessage(component);
    }

    @Override
    public void onChat(AsyncPlayerChatEvent event) {
        if (this.listening == null) { return; }
        event.setCancelled(true);
        String message = event.getMessage().strip();
        if (!message.equalsIgnoreCase("cancel")) {
            ItemType itemType = this.listening;
            Player player = event.getPlayer();
            JYML cfg = itemGenerator.getConfig();
            switch (itemType) {
                // Strings
                case NAME: case SKULL_HASH: {
                    cfg.set(itemType.getPath(), message);
                    break;
                }
                // Integers
                case MODEL_DATA: case DURABILITY: case MIN_LEVEL: case MAX_LEVEL: {
                    try {
                        cfg.set(itemType.getPath(), Integer.parseInt(message));
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
                // Doubles
                case PREFIX_CHANCE: case SUFFIX_CHANCE: {
                    try {
                        cfg.set(itemType.getPath(), Double.parseDouble(message));
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
        }
        this.listening = null;
        saveAndReopen();
    }

    @Override
    protected void onClose(@NotNull Player player, @NotNull InventoryCloseEvent e) {

    }

    public enum ItemType {
        NAME("name"),
        MATERIALS("generator.materials"),
        LORE("lore"),
        MODEL_DATA("model-data"),
        COLOR("color"),
        DURABILITY("durability"),
        UNBREAKABLE("unbreakable"),
        PREFIX_CHANCE("generator.prefix-chance"),
        SUFFIX_CHANCE("generator.suffix-chance"),
        ITEM_FLAGS("item-flags"),
        ENCHANTED("enchanted"),
        SKULL_HASH("skull-hash"),
        MIN_LEVEL("level.min"),
        MAX_LEVEL("level.max"),
        TIER("tier"),
        AMMO_TYPES("generator.ammo-types"),
        HAND_TYPES("generator.hand-types"),
        DAMAGE_TYPES("generator.damage-types"),
        DEFENSE_TYPES("generator.defense-types"),
        ITEM_STATS("generator.item-stats"),
        SKILLAPI_ATTRIBUTES("generator.skillapi-attributes"),
        SOCKETS("generator.sockets"),
        REQUIREMENTS("user-requirements-by-level"),
        ENCHANTMENTS("generator.enchantments"),
        SKILLS("generator.skills"),
        USES_BY_LEVEL("uses-by-level"),
        SAMPLE(null),
        ;

        private final String path;

        ItemType(String path) { this.path = path; }

        public String getPath() { return path; }

        public String getTitle() { return this.name().replace('_', ' ').toLowerCase(); }
    }
}
