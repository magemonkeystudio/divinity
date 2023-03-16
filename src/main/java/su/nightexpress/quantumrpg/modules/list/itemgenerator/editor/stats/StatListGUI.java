package su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.stats;

import mc.promcteam.engine.config.api.JYML;
import mc.promcteam.engine.manager.api.gui.ContentType;
import mc.promcteam.engine.manager.api.gui.GuiClick;
import mc.promcteam.engine.manager.api.gui.GuiItem;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.hooks.EHook;
import su.nightexpress.quantumrpg.hooks.external.SkillAPIHK;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.ItemGeneratorManager;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.AbstractEditorGUI;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.editor.EditorGUI;

import java.util.ArrayList;
import java.util.List;

public class StatListGUI extends AbstractEditorGUI {
    private final EditorGUI.ItemType itemType;
    private final Runnable onReturn;

    public StatListGUI(@NotNull ItemGeneratorManager itemGeneratorManager, ItemGeneratorManager.GeneratorItem itemGenerator, EditorGUI.ItemType itemType, Runnable onReturn) {
        super(itemGeneratorManager, itemGenerator, 54);
        this.itemType = itemType;
        this.onReturn = onReturn;
        setTitle("[&d"+itemGenerator.getId()+"&r] editor/"+this.itemType.getTitle());
    }

    @Override
    protected void onCreate(@NotNull Player player, @NotNull Inventory inventory, int page) {
        JYML cfg = this.itemGenerator.getConfig();
        List<String> list = new ArrayList<>();
        ConfigurationSection section = cfg.getConfigurationSection(MainStatsGUI.ItemType.LIST.getPath(this.itemType));
        if (section != null) {
            list.addAll(section.getKeys(false));
        }
        int totalPages = Math.max((int) Math.ceil(list.size()*1.0/42), 1);
        final int currentPage = page < 1 ? totalPages : Math.min(page, totalPages);
        this.setUserPage(player, currentPage, totalPages);
        GuiClick guiClick = (player1, type, clickEvent) -> {
            this.setUserPage(player1, currentPage, totalPages);
            if (type == null) { return; }
            Class<?> clazz = type.getClass();
            if (clazz.equals(ContentType.class)) {
                ContentType type2 = (ContentType) type;
                switch (type2) {
                    case RETURN: {
                        if (this.onReturn == null) {
                            player1.closeInventory();
                        } else {
                            this.onReturn.run();
                        }
                        break;
                    }
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
            Runnable onReturn = () -> new StatListGUI(itemGeneratorManager, itemGenerator, itemType, this.onReturn).open(player1, currentPage);
            if (type == MainStatsGUI.ItemType.LIST) {
                GuiItem guiItem = this.getButton(player, clickEvent.getSlot());
                if (guiItem == null) { return; }
                new StatGUI(itemGeneratorManager, itemGenerator, this.itemType, MainStatsGUI.ItemType.LIST.getPath(this.itemType)+'.'+guiItem.getId(), onReturn).open(player1, 1);
            }
        };
        for (int statIndex = (currentPage-1)*42, last = Math.min(list.size(), statIndex+42), invIndex = 1;
             statIndex < last; statIndex++, invIndex++) {
            if ((invIndex)%9 == 8) { invIndex += 2; }
            String entry = list.get(statIndex);
            if (entry == null) {
                this.addButton(this.createButton("new", ItemType.NEW, Material.REDSTONE, "&eAdd new "+itemType.getTitle(), List.of(), invIndex, guiClick));
            } else {
                Material material = null;
                Integer customModelData = null;
                switch (this.itemType) {
                    case DAMAGE_TYPES: {
                        material = Material.IRON_SWORD;
                        break;
                    }
                    case DEFENSE_TYPES: {
                        material = Material.IRON_CHESTPLATE;
                        break;
                    }
                    case SKILLAPI_ATTRIBUTES: {
                        SkillAPIHK skillAPIHK = (SkillAPIHK) QuantumRPG.getInstance().getHook(EHook.SKILL_API);
                        if (skillAPIHK != null) {
                            ItemStack indicator = skillAPIHK.getAttributeIndicator(entry);
                            material = indicator.getType();
                            ItemMeta meta = indicator.getItemMeta();
                            if (meta != null && meta.hasCustomModelData()) {
                                customModelData = meta.getCustomModelData();
                            }
                        }
                        break;
                    }
                }
                if (material == null) { material = Material.PAPER; }

                String path = MainStatsGUI.ItemType.LIST.getPath(this.itemType)+'.'+entry+'.';
                String roundDisplay = this.itemType == EditorGUI.ItemType.SKILLAPI_ATTRIBUTES ? "" : "&bRound: &a"+cfg.getBoolean(path+"round", false);
                GuiItem guiItem = this.createButton(entry, MainStatsGUI.ItemType.LIST, Material.IRON_SWORD,
                                                    "&e"+entry, List.of(
                                "&bCurrent:",
                                "&bChance: &a"+cfg.getDouble(path+"chance"),
                                "&bScale by level: &a"+cfg.getDouble(path+"scale-by-level"),
                                "&bMinimum value: &a"+cfg.getDouble(path+"min"),
                                "&bMaximum value: &a"+cfg.getDouble(path+"max"),
                                "&bFlat range: &a"+cfg.getBoolean(path+"flat-range"),
                                roundDisplay,
                                "",
                                "&6Left-Click: &eModify"), invIndex, guiClick);
                ItemStack itemStack = guiItem.getItemRaw();
                itemStack.setType(material);
                if (customModelData != null) {
                    ItemMeta meta = itemStack.getItemMeta();
                    if (meta != null) {
                        meta.setCustomModelData(customModelData);
                        itemStack.setItemMeta(meta);
                    }
                }
                guiItem.setItem(itemStack);
                this.addButton(guiItem);
            }
        }
        this.addButton(this.createButton("prev-page", ContentType.BACK, Material.ENDER_PEARL, "&dPrevious Page", List.of(), 0, guiClick));
        this.addButton(this.createButton("next-page", ContentType.NEXT, Material.ENDER_PEARL, "&dNext Page", List.of(), 8, guiClick));
        this.addButton(this.createButton("return", ContentType.RETURN, Material.BARRIER, "&c&lReturn", List.of(), 53, guiClick));
    }
}
