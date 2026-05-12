package studio.magemonkey.divinity.modules.list.itemgenerator.editor.stats;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import studio.magemonkey.codex.config.api.JYML;
import studio.magemonkey.codex.manager.api.menu.Slot;
import studio.magemonkey.divinity.Divinity;
import studio.magemonkey.divinity.hooks.EHook;
import studio.magemonkey.divinity.hooks.external.FabledHook;
import studio.magemonkey.divinity.modules.list.itemgenerator.editor.AbstractEditorGUI;
import studio.magemonkey.divinity.modules.list.itemgenerator.editor.EditorGUI;

import java.util.ArrayList;
import java.util.List;

public class StatListGUI extends AbstractEditorGUI {
    private final EditorGUI.ItemType itemType;
    /** Full config path to the list section, e.g. "generator.item-stats.list" */
    private final String             listSectionPath;

    /**
     * Opens the stat list for a specific sub-section (e.g. "list", "list-damage-buffs").
     */
    public StatListGUI(Player player, ItemGeneratorReference itemGenerator,
                       EditorGUI.ItemType itemType, String listSection) {
        super(player, 6, "Editor/" + itemType.getTitle() + " (" + listSection + ")", itemGenerator);
        this.itemType        = itemType;
        this.listSectionPath = itemType.getPath() + '.' + listSection;
    }

    /**
     * Backward-compatible constructor — defaults to the standard "list" section.
     */
    public StatListGUI(Player player, ItemGeneratorReference itemGenerator, EditorGUI.ItemType itemType) {
        this(player, itemGenerator, itemType, "list");
    }

    @Override
    public void setContents() {
        JYML                 cfg     = itemGenerator.getConfig();
        List<String>         list    = new ArrayList<>();
        ConfigurationSection section = cfg.getConfigurationSection(this.listSectionPath);
        if (section != null) {
            list.addAll(section.getKeys(false));
        }

        this.slots.clear();

        // Get from and to based on the page. I always can have 45 entries per page
        int from = this.getPage() * 45;
        int to = Math.min(from + 45, list.size());

        int i = 0;
        for(int j = from; j < to; j++) {
            String entry = list.get(j);
            i++;
            if (i % this.inventory.getSize() == 53) {
                this.setSlot(i, getNextButton());
                i++;
            } else if (i % 9 == 8) {
                i++;
            }
            if (i % this.inventory.getSize() == 45) {
                this.setSlot(i, getPrevButton());
                i++;
            } else if (i % 9 == 0) {
                i++;
            }

            ItemStack itemStack = null;
            switch (this.itemType) {
                case DAMAGE_TYPES: {
                    itemStack = new ItemStack(Material.IRON_SWORD);
                    break;
                }
                case DEFENSE_TYPES: {
                    itemStack = new ItemStack(Material.IRON_CHESTPLATE);
                    break;
                }
                case FABLED_ATTRIBUTES: {
                    FabledHook fabledHook = (FabledHook) Divinity.getInstance().getHook(EHook.SKILL_API);
                    if (fabledHook != null) itemStack = fabledHook.getAttributeIndicator(entry);
                    break;
                }
                default: {
                    // For ITEM_STATS categories, read per-entry icon from config
                    String iconKey = this.listSectionPath + '.' + entry + ".icon";
                    String iconName = cfg.getString(iconKey, "PAPER");
                    try {
                        itemStack = new ItemStack(Material.valueOf(iconName.toUpperCase()));
                    } catch (IllegalArgumentException ignored) {
                        itemStack = new ItemStack(Material.PAPER);
                    }
                    break;
                }
            }
            if (itemStack == null) {
                itemStack = new ItemStack(Material.PAPER);
            }
            String path = this.listSectionPath + '.' + entry + '.';
            String roundDisplay = this.itemType == EditorGUI.ItemType.FABLED_ATTRIBUTES
                    ? ""
                    : "&bRound: &a" + cfg.getBoolean(path + "round", false);

            createItem(itemStack,
                    "&e" + entry,
                    "&bCurrent:",
                    "&bChance: &a" + cfg.getDouble(path + "chance"),
                    "&bScale by level: &a" + cfg.getDouble(path + "scale-by-level"),
                    "&bMinimum value: &a" + cfg.getDouble(path + "min"),
                    "&bMaximum value: &a" + cfg.getDouble(path + "max"),
                    "&bFlat range: &a" + cfg.getBoolean(path + "flat-range"),
                    roundDisplay,
                    "",
                    "&eModify");
            final String entryPath = this.listSectionPath + '.' + entry;
            setSlot(i, new Slot(itemStack) {
                @Override
                public void onLeftClick() {
                    openSubMenu(new StatGUI(player,
                            itemGenerator,
                            itemType,
                            entryPath));
                }
            });

        }
        this.setSlot(this.getPages() * this.inventory.getSize() - 9, getPrevButton());
        this.setSlot(this.getPages() * this.inventory.getSize() - 1, getNextButton());
    }
}
