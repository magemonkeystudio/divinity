package su.nightexpress.quantumrpg.modules.list.itemgenerator;

import mc.promcteam.engine.config.api.JYML;
import mc.promcteam.engine.manager.api.gui.ContentType;
import mc.promcteam.engine.manager.api.gui.GuiClick;
import mc.promcteam.engine.manager.api.gui.GuiItem;
import mc.promcteam.engine.manager.api.gui.NGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.QuantumRPG;

public class EditorGUI extends NGUI<QuantumRPG> {
    private final ItemGeneratorManager itemGeneratorManager;
    private final ItemGeneratorManager.GeneratorItem itemGenerator;

    EditorGUI(@NotNull ItemGeneratorManager itemGeneratorManager, @NotNull JYML cfg, ItemGeneratorManager.GeneratorItem itemGenerator) {
        super(itemGeneratorManager.plugin, cfg, "editor-gui.");
        this.itemGeneratorManager = itemGeneratorManager;
        this.itemGenerator = itemGenerator;
        this.setTitle(this.getTitle().replace("%id%", itemGenerator.getId()));
        GuiClick guiClick = ((player, type, clickEvent) -> {
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
                        break;
                    }
                    case PREFIX_CHANCE: {
                        break;
                    }
                    case SUFFIX_CHANCE: {
                        break;
                    }
                    case LORE: {
                        break;
                    }
                    case COLOR: {
                        break;
                    }
                    case UNBREAKABLE: {
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
                        break;
                    }
                }
                player.sendMessage(type2.name());
                return;
            }
        });

        for (String id : cfg.getSection("editor-gui.content")) {
            GuiItem guiItem = cfg.getGuiItem("editor-gui.content." + id, ItemType.class);
            if (guiItem == null) continue;

            if (guiItem.getType() != null) {
                guiItem.setClick(guiClick);
            }
            this.addButton(guiItem);
        }
    }

    public ItemGeneratorManager.GeneratorItem getItemGenerator() { return itemGenerator; }

    @Override
    protected void onCreate(@NotNull Player player, @NotNull Inventory inventory, int i) { }

    @Override
    protected void onClose(@NotNull Player player, @NotNull InventoryCloseEvent e) {
        super.onClose(player, e);
        itemGeneratorManager.onEditorClose(this);
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
