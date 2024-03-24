package com.promcteam.divinity.manager.listener.object;

import com.google.common.collect.Sets;
import com.promcteam.codex.manager.IListener;
import com.promcteam.codex.utils.ItemUT;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import com.promcteam.divinity.QuantumRPG;
import com.promcteam.divinity.config.EngineCfg;
import com.promcteam.divinity.utils.ItemUtils;

import java.util.Set;

public class ItemRequirementListener extends IListener<QuantumRPG> {

    public ItemRequirementListener(@NotNull QuantumRPG plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onRequirementsItemAttack(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) return;
        Player    player = (Player) e.getDamager();
        ItemStack item   = player.getInventory().getItemInMainHand();

        if (!ItemUtils.canUse(item, player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onRequirementsItemHeld(PlayerItemHeldEvent e) {
        if (EngineCfg.ATTRIBUTES_ALLOW_HOLD_REQUIREMENTS) return;

        int       slot   = e.getNewSlot();
        Player    player = e.getPlayer();
        ItemStack item   = player.getInventory().getItem(slot);
        if (item == null || item.getType() == Material.AIR) return;

        if (!ItemUtils.canUse(item, player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onRequirementsItemUse(PlayerInteractEvent e) {
        ItemStack item = e.getItem();
        if (item == null || item.getType() == Material.AIR) return;

        Player player = e.getPlayer();
        if (!ItemUtils.canUse(item, player)) {
            e.setCancelled(true);
            e.setUseItemInHand(Result.DENY);
            e.setUseInteractedBlock(Result.DENY);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onRequirementsItemDispense(BlockDispenseArmorEvent e) {
        ItemStack    item   = e.getItem();
        LivingEntity entity = e.getTargetEntity();
        if (entity instanceof Player) {
            Player player = (Player) entity;
            if (!ItemUtils.canUse(item, player)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onRequirementsItemDrag(InventoryDragEvent e) {
        if (e.getInventory().getType() != InventoryType.CRAFTING) return;

        ItemStack drag = e.getOldCursor();
        if (ItemUT.isAir(drag)) return;

        Player       player = (Player) e.getWhoClicked();
        Set<Integer> slots  = e.getRawSlots();
        Set<Integer> deny   = Sets.newHashSet(5, 6, 7, 8, 45);

        boolean doCheck = slots.stream().anyMatch(slotRaw -> deny.contains(slotRaw));

        if (doCheck && !ItemUtils.canUse(drag, player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onRequirementsItemClick(InventoryClickEvent e) {
        if (e.getInventory().getType() != InventoryType.CRAFTING) return;

        Player player = (Player) e.getWhoClicked();
        int    slot   = e.getSlot();
        if ((slot >= 36 && slot <= 40) || slot == player.getInventory().getHeldItemSlot()) {
            ItemStack drag = e.getCursor();
            if (drag != null && !ItemUtils.canUse(drag, player)) {
                e.setCancelled(true);
                return;
            }
        }

        ItemStack item = e.getCurrentItem();
        if (item == null) return;

        if (e.getAction() == InventoryAction.HOTBAR_SWAP && !ItemUtils.canUse(item, player)
                && !EngineCfg.ATTRIBUTES_ALLOW_HOLD_REQUIREMENTS) {
            e.setCancelled(true);
            return;
        }

        if (e.isShiftClick() && !ItemUtils.canUse(item, player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onRequirementsBlockBreak(BlockBreakEvent e) {
        Player    player = e.getPlayer();
        ItemStack item   = player.getInventory().getItemInMainHand();
        if (!ItemUtils.canUse(item, player)) {
            e.setCancelled(true);
        }
    }
}
