package studio.magemonkey.divinity.modules.list.repair;

import studio.magemonkey.codex.config.api.JYML;
import studio.magemonkey.codex.util.DataUT;
import studio.magemonkey.codex.util.ItemUT;
import studio.magemonkey.divinity.Divinity;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.magemonkey.codex.manager.api.gui.*;

import java.util.HashSet;
import java.util.List;

public class AnvilGUI extends NGUI<Divinity> {

    protected RepairManager repairManager;
    protected int           itemSlot;
    protected int           sourceSlot;
    protected int           resultSlot;

    private static final NamespacedKey META_KEY_REPAIR_SELECT =
            new NamespacedKey(Divinity.getInstance(), "QRPG_REPAIR_GUI_SELECTOR");

    public AnvilGUI(@NotNull RepairManager repairManager) {
        super(repairManager.plugin, repairManager.getJYML(), "gui.");
        this.repairManager = repairManager;

        JYML   cfg  = repairManager.getJYML();
        String path = "gui.";

        this.itemSlot = cfg.getInt(path + "item-slot");
        this.sourceSlot = cfg.getInt(path + "source-slot");
        this.resultSlot = cfg.getInt(path + "result-slot");

        GuiClick clickMain = (p, type, e) -> {
            if (type == ContentType.EXIT) {
                p.closeInventory();
            } else if (type == ContentType.ACCEPT) {
                Inventory inv    = e.getInventory();
                ItemStack result = getItem(inv, resultSlot);
                if (result.getType() == Material.AIR) return;

                ItemStack                target      = getItem(inv, itemSlot);
                RepairManager.RepairType rerpairType = getSelectedType(inv);

                if (rerpairType == null) {
                    repairManager.actionsError.process(p);
                    plugin.lang().Repair_Error_TypeNotSelected.send(p);
                    return;
                }
                if (!repairManager.payForRepair(p, rerpairType, target)) {
                    repairManager.actionsError.process(p);
                    plugin.lang().Repair_Error_TooExpensive.send(p);
                    return;
                }

                ItemUT.addItem(p, result);
                ItemStack src = getItem(inv, sourceSlot);

                // Prevent to dupe after close
                inv.setItem(itemSlot, null);
                inv.setItem(resultSlot, null);

                repairManager.actionsComplete.process(p);

                plugin.lang().Repair_Done
                        .replace("%item%", ItemUT.getItemName(target))
                        .send(p);

                open(p, null, src, rerpairType);
            }
        };

        for (String itemId : cfg.getSection(path + "content")) {
            GuiItem guiItem = cfg.getGuiItem(path + "content." + itemId, ContentType.class);
            if (guiItem == null) continue;

            if (guiItem.getType() != null) {
                guiItem.setClick(clickMain);
            }

            this.addButton(guiItem);
        }

        for (String itemId : cfg.getSection(path + "repair-types")) {
            GuiItem guiItem = cfg.getGuiItem(path + "repair-types." + itemId, RepairManager.RepairType.class);
            if (guiItem == null) continue;

            Enum<?> type = guiItem.getType();
            if (type != null && type.getClass().equals(RepairManager.RepairType.class)) {
                RepairManager.RepairType repairType = (RepairManager.RepairType) type;
                if (!repairType.isEnabled()) {
                    continue;
                }
            }

            this.addButton(guiItem);
        }
    }

    public void open(
            @NotNull Player player,
            @Nullable ItemStack target,
            @Nullable ItemStack source,
            @Nullable RepairManager.RepairType type) {

        if (target == null) {
            target = new ItemStack(Material.AIR);
        }
        if (source == null) {
            source = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) source.getItemMeta();
            if (meta == null) return;

            meta.setDisplayName(player.getName());
            meta.setOwningPlayer(player);
            source.setItemMeta(meta);
        }

        // GUI Fix
        this.clearUserCache(player);
        this.LOCKED_CACHE.add(player.getName());

        // Add items
        this.addButton(player, new JIcon(target), this.itemSlot);
        this.addButton(player, new JIcon(source), this.sourceSlot);
        this.addButton(player, new JIcon(repairManager.getResult(new ItemStack(target), player)), this.resultSlot);

        // Just a hack to add a glow to selected type.
        for (GuiItem guiItem : new HashSet<>(this.getContent().values())) {
            Enum<?> type2 = guiItem.getType();
            if (type2 == null || !type2.getClass().equals(RepairManager.RepairType.class)) {
                continue;
            }
            RepairManager.RepairType repairType = (RepairManager.RepairType) type2;
            ItemStack                itemGlow   = guiItem.getItem();
            this.replaceCostHave(player, target, itemGlow, repairType);
            if (type2 == type) {
                itemGlow.addUnsafeEnchantment(Enchantment.getByName("punch"), 1); // ARROW_DAMAGE/PUNCH
                DataUT.setData(itemGlow, META_KEY_REPAIR_SELECT, "true");
            }

            GuiClick clickRepair = new GuiClick() {
                @Override
                public void click(
                        @NotNull Player p, @Nullable Enum<?> type, @NotNull InventoryClickEvent e) {
                    Inventory inv    = e.getInventory();
                    ItemStack target = inv.getItem(itemSlot);
                    ItemStack src    = inv.getItem(sourceSlot);

                    // Prevent duplication for onClose event
                    inv.setItem(itemSlot, null);
                    inv.setItem(sourceSlot, null);

                    open(p, target, src, (RepairManager.RepairType) type2);
                }
            };

            JIcon active = new JIcon(itemGlow);
            active.setClick(clickRepair);

            for (int slot : guiItem.getSlots()) {
                this.addButton(player, active, slot);
            }
        }

        super.open(player, 1);
    }

    @Override
    protected void onCreate(@NotNull Player player, @NotNull Inventory inv, int page) {

    }

    @Nullable
    private RepairManager.RepairType getSelectedType(@NotNull Inventory inv) {
        for (GuiItem guiItem : this.getContent().values()) {
            Enum<?> type2 = guiItem.getType();
            if (type2 == null || !type2.getClass().equals(RepairManager.RepairType.class)) continue;

            for (int slot : guiItem.getSlots()) {
                ItemStack item = inv.getItem(slot);
                if (item == null) continue;

                String data = DataUT.getStringData(item, META_KEY_REPAIR_SELECT);
                if (data != null && data.equalsIgnoreCase("true")) {
                    return (RepairManager.RepairType) type2;
                }
            }
        }
        return null;
    }

    private void replaceCostHave(
            @NotNull Player player,
            @NotNull ItemStack target,
            @NotNull ItemStack item,
            @Nullable RepairManager.RepairType type) {

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        String strCost = String.valueOf(this.repairManager.calcCost(target, type));
        String strHave = String.valueOf(this.repairManager.getPlayerBalance(player, type, target));
        String strMat  = this.repairManager.getMaterialName(target);

        if (meta.hasDisplayName()) {
            String name = meta.getDisplayName()
                    .replace("%cost%", strCost)
                    .replace("%have%", strHave).replace("%mat%", strMat);
            meta.setDisplayName(name);
        }

        List<String> lore = meta.getLore();
        if (lore != null) {
            for (int i = 0; i < lore.size(); i++) {
                lore.set(i, lore.get(i)
                        .replace("%cost%", strCost)
                        .replace("%have%", strHave).replace("%mat%", strMat));
            }
            meta.setLore(lore);
        }

        item.setItemMeta(meta);
    }

    @Override
    public void click(@NotNull Player player, @Nullable ItemStack item, int slot, InventoryClickEvent e) {
        Inventory inv = e.getInventory();

        ItemStack target = this.getItem(inv, this.itemSlot);
        ItemStack source = this.getItem(inv, this.sourceSlot);

        // Click to select item to repair in player inventory
        if (slot >= inv.getSize() && target.getType() == Material.AIR) {
            this.repairManager.openAnvilGUI(player, item, source, this.getSelectedType(inv), true);
            return;
        }

        // Click to take item from repair to inventory
        if (slot < inv.getSize() && slot == itemSlot && target.getType() != Material.AIR) {
            inv.setItem(this.sourceSlot, null);
            this.open(player, null, source, this.getSelectedType(inv));
            return;
        }

        super.click(player, item, slot, e);
    }

    @Override
    public void onClose(@NotNull Player player, @NotNull InventoryCloseEvent e) {
        Inventory inv = e.getInventory();

        ItemStack item = inv.getItem(this.itemSlot);
        if (item != null) {
            ItemUT.addItem(player, item);
        }
    }

    @Override
    protected boolean cancelClick(int slot) {
        return true;
    }

    @Override
    protected boolean cancelPlayerClick() {
        return true;
    }

    @Override
    protected boolean ignoreNullClick() {
        return true;
    }
}
