package su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.enchantments;

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
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.ItemGeneratorManager;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.AbstractEditorGUI;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.EditorGUI;

import java.util.List;

public class EnchantmentsGUI extends AbstractEditorGUI {
    private ItemType listening;

    public EnchantmentsGUI(@NotNull ItemGeneratorManager itemGeneratorManager, ItemGeneratorManager.GeneratorItem itemGenerator) {
        super(itemGeneratorManager, itemGenerator, 9);
        setTitle("[&d"+itemGenerator.getId()+"&r] editor/"+EditorGUI.ItemType.ENCHANTMENTS.getTitle());
    }

    @Override
    protected void onCreate(@NotNull Player player, @NotNull Inventory inventory, int i) {
        GuiClick guiClick = (player1, type, clickEvent) -> {
            if (type == null) { return; }
            Class<?> clazz = type.getClass();
            if (clazz.equals(ContentType.class)) {
                ContentType type2 = (ContentType) type;
                switch (type2) {
                    case EXIT: {
                        player.closeInventory();
                        break;
                    }
                    case RETURN: {
                        new EditorGUI(itemGeneratorManager, itemGenerator).open(player1, 1);
                        break;
                    }
                }
                return;
            }

            if (clazz.equals(ItemType.class)) {
                ItemType type2 = (ItemType) type;
                switch (type2) {
                    case MINIMUM: {
                        switch (clickEvent.getClick()) {
                            case LEFT: {
                                this.itemGenerator.getConfig().set(EditorGUI.ItemType.ENCHANTMENTS.getPath()+".minimum", Math.max(0, this.itemGenerator.getMinEnchantments()-1));
                                saveAndReopen();
                                break;
                            }
                            case RIGHT: {
                                this.itemGenerator.getConfig().set(EditorGUI.ItemType.ENCHANTMENTS.getPath()+".minimum", this.itemGenerator.getMinEnchantments()+1);
                                saveAndReopen();
                                break;
                            }
                            case DROP: case CONTROL_DROP: {
                                setDefault(EditorGUI.ItemType.ENCHANTMENTS.getPath()+".minimum");
                                saveAndReopen();
                                break;
                            }
                            default: {
                                sendSetMessage(type2, String.valueOf(this.itemGenerator.getMinEnchantments()));
                                break;
                            }
                        }
                        break;
                    }
                    case MAXIMUM: {
                        switch (clickEvent.getClick()) {
                            case LEFT: {
                                this.itemGenerator.getConfig().set(EditorGUI.ItemType.ENCHANTMENTS.getPath()+".maximum", Math.max(0, this.itemGenerator.getMaxEnchantments()-1));
                                saveAndReopen();
                                break;
                            }
                            case RIGHT: {
                                this.itemGenerator.getConfig().set(EditorGUI.ItemType.ENCHANTMENTS.getPath()+".maximum", this.itemGenerator.getMaxEnchantments()+1);
                                saveAndReopen();
                                break;
                            }
                            case DROP: case CONTROL_DROP: {
                                setDefault(EditorGUI.ItemType.ENCHANTMENTS.getPath()+".maximum");
                                saveAndReopen();
                                break;
                            }
                            default: {
                                sendSetMessage(type2, String.valueOf(this.itemGenerator.getMinEnchantments()));
                                break;
                            }
                        }
                        break;
                    }
                    case SAFE_ONLY: {
                        this.itemGenerator.getConfig().set(EditorGUI.ItemType.ENCHANTMENTS.getPath()+".safe-only", !this.itemGenerator.isSafeEnchant());
                        saveAndReopen();
                        break;
                    }
                    case SAFE_LEVELS: {
                        this.itemGenerator.getConfig().set(EditorGUI.ItemType.ENCHANTMENTS.getPath()+".safe-levels", !this.itemGenerator.isEnchantsSafeLevels());
                        saveAndReopen();
                        break;
                    }
                    case LIST: {
                        new EnchantmentListGUI(this.itemGeneratorManager, this.itemGenerator).open(player1, 1);
                        break;
                    }
                }
            }
        };
        this.addButton(this.createButton("minimum", ItemType.MINIMUM, Material.BROWN_MUSHROOM,
                                         "&eMinimum enchantments", List.of(
                                                 "&bCurrent: &a"+this.itemGenerator.getMinEnchantments(),
                                                 "&6Middle-Click: &eSet",
                                                 "&6Left-Click: &eDecrease",
                                                 "&6Right-Click: &eIncrease",
                                                 "&6Drop: &eSet to default value"), 0, guiClick));
        this.addButton(this.createButton("maximum", ItemType.MAXIMUM, Material.RED_MUSHROOM,
                                         "&eMaximum enchantments", List.of(
                                                "&bCurrent: &a"+this.itemGenerator.getMaxEnchantments(),
                                                "&6Middle-Click: &eSet",
                                                "&6Left-Click: &eDecrease",
                                                "&6Right-Click: &eIncrease",
                                                "&6Drop: &eSet to default value"), 1, guiClick));
        this.addButton(this.createButton("safe-only", ItemType.SAFE_ONLY, Material.BOW,
                                         "&eSafe enchantments only", List.of(
                                                 "&bCurrent: &a"+this.itemGenerator.isSafeEnchant(),
                                                 "&6Left-Click: &eToggle",
                                                 "&6Drop: &eSet to default value"), 2, guiClick));
        this.addButton(this.createButton("safe-levels", ItemType.SAFE_LEVELS, Material.EXPERIENCE_BOTTLE,
                                         "&eSafe enchantment levels only", List.of(
                                                 "&bCurrent: &a"+this.itemGenerator.isEnchantsSafeLevels(),
                                                 "&6Left-Click: &eToggle",
                                                 "&6Drop: &eSet to default value"), 3, guiClick));

        this.addButton(this.createButton("list", ItemType.LIST, Material.ENCHANTED_BOOK,
                                         "&eList of enchantments", List.of(
                                                 "&6Left-Click: &eToggle",
                                                 "&6Drop: &eSet to default value"), 4, guiClick));
        this.addButton(this.createButton("return", ContentType.RETURN, Material.BARRIER, "&c&lReturn", List.of(), 8, guiClick));
    }

    private void sendSetMessage(ItemType itemType, String currentValue) {
        this.listening = itemType;
        player.closeInventory();
        String name;
        switch (itemType) {
            case MINIMUM: {
                name = "minimum enchantments";
                break;
            }
            case MAXIMUM: {
                name = "maximum enchantments";
                break;
            }
            default: {
                throw new IllegalArgumentException();
            }
        }
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
        ItemType itemType = this.listening;
        this.listening = null;
        String message = event.getMessage().strip();
        int value;
        try {
            value = Integer.parseInt(message);
        } catch (NumberFormatException e) {
            plugin.lang().ItemGenerator_Cmd_Editor_Error_InvalidInput.replace("%input%", message).replace("%value%", "integer").send(player);
            saveAndReopen();
            return;
        }
        switch (itemType) {
            case MINIMUM: {
                this.itemGenerator.getConfig().set(EditorGUI.ItemType.ENCHANTMENTS.getPath()+".minimum", value);
                break;
            }
            case MAXIMUM: {
                this.itemGenerator.getConfig().set(EditorGUI.ItemType.ENCHANTMENTS.getPath()+".maximum", value);
                break;
            }
        }
        saveAndReopen();
    }

    public enum ItemType {
        MINIMUM,
        MAXIMUM,
        SAFE_ONLY,
        SAFE_LEVELS,
        LIST,
    }
}
