package studio.magemonkey.divinity.modules.list.itemgenerator.editor.stats;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import studio.magemonkey.codex.manager.api.menu.Slot;
import studio.magemonkey.divinity.modules.list.itemgenerator.editor.AbstractEditorGUI;
import studio.magemonkey.divinity.modules.list.itemgenerator.editor.EditorGUI;

/**
 * Intermediate category-selection GUI shown when clicking the "List" button
 * in the Item Stats editor (MainStatsGUI slot 3).
 *
 * <p>Presents four sub-categories:
 * <ul>
 *   <li>General     — classic TypedStat entries         (list:)</li>
 *   <li>Damage %    — DynamicBuffStat damage entries     (list-damage-buffs:)</li>
 *   <li>Defense %   — DynamicBuffStat defense entries    (list-defense-buffs:)</li>
 *   <li>Penetration — PenetrationStat entries            (list-penetration:)</li>
 * </ul>
 */
public class StatCategoryGUI extends AbstractEditorGUI {

    public StatCategoryGUI(Player player, ItemGeneratorReference itemGenerator) {
        super(player, 1, "Editor/Item Stats - Category", itemGenerator);
    }

    @Override
    public void setContents() {
        // Slot 1 — General
        setSlot(1, new Slot(createItem(Material.PAPER,
                "&eGeneral Stats",
                "&7Classic typed stats (critical rate, dodge, etc.)",
                "",
                "&6Left-Click: &eOpen")) {
            @Override
            public void onLeftClick() {
                openSubMenu(new StatListGUI(player, itemGenerator, EditorGUI.ItemType.ITEM_STATS, "list"));
            }
        });

        // Slot 3 — Damage %
        setSlot(3, new Slot(createItem(Material.IRON_SWORD,
                "&eDamage Buffs &6(%)",
                "&7Per-damage-type % buff stats",
                "",
                "&6Left-Click: &eOpen")) {
            @Override
            public void onLeftClick() {
                openSubMenu(new StatListGUI(player, itemGenerator, EditorGUI.ItemType.ITEM_STATS, "list-damage-buffs"));
            }
        });

        // Slot 5 — Defense %
        setSlot(5, new Slot(createItem(Material.IRON_CHESTPLATE,
                "&eDefense Buffs &6(%)",
                "&7Per-damage-type % defense buff stats",
                "",
                "&6Left-Click: &eOpen")) {
            @Override
            public void onLeftClick() {
                openSubMenu(new StatListGUI(player, itemGenerator, EditorGUI.ItemType.ITEM_STATS, "list-defense-buffs"));
            }
        });

        // Slot 7 — Penetration
        setSlot(7, new Slot(createItem(Material.ARROW,
                "&ePenetration",
                "&7Per-damage-type penetration stats",
                "",
                "&6Left-Click: &eOpen")) {
            @Override
            public void onLeftClick() {
                openSubMenu(new StatListGUI(player, itemGenerator, EditorGUI.ItemType.ITEM_STATS, "list-penetration"));
            }
        });
    }
}
