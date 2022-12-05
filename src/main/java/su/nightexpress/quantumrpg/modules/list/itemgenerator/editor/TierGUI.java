package su.nightexpress.quantumrpg.modules.list.itemgenerator.editor;

import mc.promcteam.engine.config.api.JYML;
import mc.promcteam.engine.manager.api.gui.ContentType;
import mc.promcteam.engine.manager.api.gui.GuiClick;
import mc.promcteam.engine.manager.api.gui.GuiItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.config.Config;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.ItemGeneratorManager;
import su.nightexpress.quantumrpg.stats.tiers.Tier;

import java.util.List;

public class TierGUI extends AbstractEditorGUI {

    public TierGUI(@NotNull ItemGeneratorManager itemGeneratorManager, ItemGeneratorManager.GeneratorItem itemGenerator, String title) {
        super(itemGeneratorManager, itemGenerator, 54);
        setTitle("[&d"+itemGenerator.getId()+"&r] editor/"+EditorGUI.ItemType.TIER.getTitle());
    }

    @Override
    protected void onCreate(@NotNull Player player, @NotNull Inventory inventory, int page) {
        Tier[] tiers = Config.getTiers().toArray(new Tier[0]);
        int totalPages = Math.max((int) Math.ceil(tiers.length*1.0/42), 1);
        final int currentPage = page < 1 ? totalPages : Math.min(page, totalPages);
        this.setUserPage(player, currentPage, totalPages);
        GuiClick guiClick = (player1, type, inventoryClickEvent) -> {
            this.setUserPage(player, currentPage, totalPages);
            if (type == null) { return; }
            Class<?> clazz = type.getClass();
            if (clazz.equals(ContentType.class)) {
                ContentType type2 = (ContentType) type;
                switch (type2) {
                    case RETURN:
                        new EditorGUI(itemGeneratorManager, itemGenerator).open(player1, 1);
                        break;
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
            if (type != EditorGUI.ItemType.TIER) { return; }
            GuiItem guiItem = this.getButton(player, inventoryClickEvent.getSlot());
            if (guiItem == null) { return; }
            JYML cfg = this.itemGenerator.getConfig();
            cfg.set(EditorGUI.ItemType.TIER.getPath(), guiItem.getId());
            saveAndReopen(currentPage);
        };
        for (int tierIndex = (currentPage-1)*42, last = Math.min(tiers.length, tierIndex+42), invIndex = 1;
             tierIndex < last; tierIndex++, invIndex++) {
            if ((invIndex)%9 == 8) { invIndex += 2; }
            Tier tier = tiers[tierIndex];
            this.addButton(this.createButton(tier.getId(), EditorGUI.ItemType.TIER, tier.getId().equals(this.itemGenerator.getTier().getId()) ? Material.DIAMOND : Material.IRON_INGOT, tier.getName(), List.of(), invIndex, guiClick));
        }
        this.addButton(this.createButton("prev-page", ContentType.BACK, Material.ENDER_PEARL, "&dPrevious Page", List.of(), 0, guiClick));
        this.addButton(this.createButton("next-page", ContentType.NEXT, Material.ENDER_PEARL, "&dNext Page", List.of(), 8, guiClick));
        this.addButton(this.createButton("exit", ContentType.RETURN, Material.BARRIER, "&c&lReturn", List.of(), 53, guiClick));
    }
}
