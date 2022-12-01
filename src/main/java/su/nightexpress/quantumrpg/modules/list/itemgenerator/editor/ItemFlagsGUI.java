package su.nightexpress.quantumrpg.modules.list.itemgenerator.editor;

import mc.promcteam.engine.config.api.JYML;
import mc.promcteam.engine.manager.api.gui.ContentType;
import mc.promcteam.engine.manager.api.gui.GuiClick;
import mc.promcteam.engine.manager.api.gui.GuiItem;
import mc.promcteam.engine.manager.api.gui.NGUI;
import mc.promcteam.engine.utils.constants.JStrings;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.QuantumRPG;

import java.util.*;

public class ItemFlagsGUI extends NGUI<QuantumRPG> { // TODO extend EditorGUI
    private static final String PATH = EditorGUI.ItemType.ITEM_FLAGS.getPath();
    private final EditorGUI parentGUI;

    public ItemFlagsGUI(EditorGUI parentGUI, @NotNull QuantumRPG plugin, @NotNull String title) {
        super(plugin, title, ((int) Math.ceil((ItemFlag.values().length+1)*1.0/9))*9);
        this.parentGUI = parentGUI;
        GuiClick guiClick = (player, type, inventoryClickEvent) -> {
            if (type == null) { return; }
            Class<?> clazz = type.getClass();
            if (clazz.equals(ContentType.class)) {
                ContentType type2 = (ContentType) type;
                switch (type2) {
                    case BACK:
                    case RETURN:
                        parentGUI.listening = null;
                        parentGUI.open(player, 1);
                        break;
                    case EXIT: {
                        player.closeInventory();
                        break;
                    }
                }
                return;
            }

            String flag = type.name().toLowerCase();
            JYML cfg = parentGUI.itemGenerator.getConfig();
            Set<String> itemFlags = new HashSet<>(cfg.getStringList(PATH));
            if (itemFlags.contains(JStrings.MASK_ANY)) {
                itemFlags.remove(JStrings.MASK_ANY);
                for (ItemFlag itemFlag : ItemFlag.values()) {
                    itemFlags.add(itemFlag.name().toLowerCase());
                }
            }
            switch (inventoryClickEvent.getClick()) {
                case DROP: case CONTROL_DROP: {
                    Set<String> defaultFlags = new HashSet<>(EditorGUI.commonItemGenerator.getStringList(PATH));
                    if (defaultFlags.contains(flag) || defaultFlags.contains(JStrings.MASK_ANY)) {
                        itemFlags.add(flag);
                    } else {
                        itemFlags.remove(flag);
                    }
                    break;
                }
                default: {
                    if (itemFlags.contains(flag)) { itemFlags.remove(flag); } else { itemFlags.add(flag); }
                    break;
                }
            }
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
            cfg.set(PATH, new ArrayList<>(itemFlags));
            parentGUI.reload(cfg, (gui) -> gui.open(new ItemFlagsGUI(gui, plugin, title), EditorGUI.ItemType.ITEM_FLAGS));
        };
        Set<ItemFlag> flags = parentGUI.itemGenerator.getFlags();
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
                itemMeta.setLore(List.of(ChatColor.AQUA+"Current: "+ChatColor.GREEN+flags.contains(itemFlag),
                                         ChatColor.GOLD+"Left-Click: "+ChatColor.YELLOW+"Set",
                                         ChatColor.GOLD+"Drop: "+ChatColor.YELLOW+"Set to default value"));
                itemStack.setItemMeta(itemMeta);
            }
            GuiItem guiItem = new GuiItem(itemFlag.name().toLowerCase(), itemFlag, itemStack, false, 0, new TreeMap<>(), Collections.emptyMap(), null, new int[]{itemFlag.ordinal()});
            guiItem.setClick(guiClick);
            this.addButton(guiItem);
        }
        ItemStack itemStack = new ItemStack(Material.BARRIER);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName(ChatColor.YELLOW.toString()+ChatColor.BOLD+"Return");
            itemStack.setItemMeta(itemMeta);
        }
        GuiItem guiItem = new GuiItem("return", ContentType.RETURN, itemStack, false, 0, new TreeMap<>(), Collections.emptyMap(), null, new int[]{ItemFlag.values().length});
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
        parentGUI.listening = null;
    }
}
