package com.promcteam.divinity.manager.listener.object;

import com.promcteam.codex.manager.IListener;
import com.promcteam.codex.util.ItemUT;
import com.promcteam.divinity.Divinity;
import com.promcteam.divinity.stats.items.ItemStats;
import com.promcteam.divinity.stats.items.attributes.HandAttribute;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class ItemHandListener extends IListener<Divinity> {

    public ItemHandListener(@NotNull Divinity plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onHandHeld(PlayerItemHeldEvent e) {
        int    slot   = e.getNewSlot();
        Player player = e.getPlayer();

        ItemStack toHold = player.getInventory().getItem(slot);
        if (toHold == null || toHold.getType() == Material.AIR) return;

        ItemStack inOff = player.getInventory().getItemInOffHand();
        if (ItemUT.isAir(inOff)) return;

        HandAttribute handTo  = ItemStats.getHand(toHold);
        HandAttribute handOff = ItemStats.getHand(inOff);

        if ((handTo != null && handTo.getType() == HandAttribute.Type.TWO)
                || (handOff != null && handOff.getType() == HandAttribute.Type.TWO)) {
            plugin.lang().Module_Item_Interact_Error_Hand.send(player);
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onHandSwap(PlayerSwapHandItemsEvent e) {
        Player player = e.getPlayer();

        ItemStack off = e.getOffHandItem();
        if (off == null || off.getType() == Material.AIR) return;

        HandAttribute handOff = ItemStats.getHand(off);
        if (handOff != null && handOff.getType() == HandAttribute.Type.TWO) {
            plugin.lang().Module_Item_Interact_Error_Hand.send(player);
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onHandDrag(InventoryDragEvent e) {
        if (e.getInventory().getType() != InventoryType.CRAFTING) return;

        ItemStack drag = e.getOldCursor();
        if (ItemUT.isAir(drag)) return;

        HandAttribute hand = ItemStats.getHand(drag);
        if (hand == null || hand.getType() != HandAttribute.Type.TWO) return;

        Player       player = (Player) e.getWhoClicked();
        Set<Integer> slots  = e.getRawSlots();

        if (slots.contains(45)) { // Offhand
            plugin.lang().Module_Item_Interact_Error_Hand.send(player);
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onHandClose(InventoryCloseEvent e) {
        Player    player = (Player) e.getPlayer();
        ItemStack off    = player.getInventory().getItemInOffHand();
        if (ItemUT.isAir(off)) return;

        ItemStack main = player.getInventory().getItemInMainHand();

        HandAttribute pickh = ItemStats.getHand(main);
        HandAttribute offh  = ItemStats.getHand(off);

        if ((pickh != null && pickh.getType() == HandAttribute.Type.TWO)
                || (offh != null && offh.getType() == HandAttribute.Type.TWO)) {

            ItemUT.addItem(player, new ItemStack(off));
            player.getInventory().setItemInOffHand(null);
            plugin.lang().Module_Item_Interact_Error_Hand.send(player);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onHandHoldOffClick(InventoryClickEvent e) {
        Inventory inv = e.getInventory();
        if (inv.getType() != InventoryType.CRAFTING) return;

        ItemStack cursor = e.getCursor();
        if (cursor == null || cursor.getType() == Material.AIR) return;

        if (e.getSlot() == 40) { // Offhand
            Player        player  = (Player) e.getWhoClicked();
            HandAttribute handAtt = ItemStats.getHand(cursor);
            if (handAtt != null && handAtt.getType() == HandAttribute.Type.TWO) {
                plugin.lang().Module_Item_Interact_Error_Hand.send(player);
                e.setCancelled(true);
                return;
            } else {
                if (this.holdMainTwo(player)) {
                    plugin.lang().Module_Item_Interact_Error_Hand.send(player);
                    e.setCancelled(true);
                }
            }
        }
    }

    private boolean holdMainTwo(@NotNull Player player) {
        ItemStack     main = player.getInventory().getItemInMainHand();
        HandAttribute hand = ItemStats.getHand(main);
        return hand != null && hand.getType() == HandAttribute.Type.TWO;
    }
}
