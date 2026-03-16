package studio.magemonkey.divinity.manager.listener.object;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.magemonkey.codex.manager.IListener;
import studio.magemonkey.divinity.Divinity;
import studio.magemonkey.divinity.modules.LeveledItem;
import studio.magemonkey.divinity.modules.ModuleItem;
import studio.magemonkey.divinity.modules.api.QModuleDrop;
import studio.magemonkey.divinity.stats.items.ItemStats;

import java.util.HashSet;

public class ItemAutoUpdateListener extends IListener<Divinity> {

    public ItemAutoUpdateListener(@NotNull Divinity plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player    player = event.getPlayer();
        ItemStack item   = player.getInventory().getItem(event.getNewSlot());
        ItemStack updated = updateItem(item);
        if (updated != null) {
            player.getInventory().setItem(event.getNewSlot(), updated);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        updateInventory(event.getPlayer());
    }

    private void updateInventory(@NotNull Player player) {
        PlayerInventory inv      = player.getInventory();
        ItemStack[]     contents = inv.getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack updated = updateItem(contents[i]);
            if (updated != null) {
                inv.setItem(i, updated);
            }
        }
    }

    @Nullable
    private ItemStack updateItem(@Nullable ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return null;

        QModuleDrop<?> module = ItemStats.getModule(item);
        if (module == null || !module.isAutoUpdate()) return null;

        String itemId = ItemStats.getId(item);
        if (itemId == null) return null;

        ModuleItem moduleItem = module.getItemById(itemId);
        if (moduleItem == null) return null;

        ItemStack updated = moduleItem.update(item);
        if (module.isAutoUpdateOverrideChanges()) {
            return updated;
        }

        ItemStack template = createTemplate(moduleItem, item);
        if (!hasPlayerItemChanges(item, template)) {
            return updated;
        }

        return reapplyPlayerChanges(updated, item);
    }

    @NotNull
    private ItemStack createTemplate(@NotNull ModuleItem moduleItem, @NotNull ItemStack sourceItem) {
        if (moduleItem instanceof LeveledItem) {
            return ((LeveledItem) moduleItem).create(ItemStats.getLevel(sourceItem));
        }
        return moduleItem.create();
    }

    /**
     * Detects whether the source item differs from a clean module template in player-modifiable fields:
     * display name, lore, enchantments, item flags, item damage and armor/potion color.
     */
    private boolean hasPlayerItemChanges(@NotNull ItemStack source, @NotNull ItemStack template) {
        if (!source.getEnchantments().equals(template.getEnchantments())) return true;

        ItemMeta sourceMeta = source.getItemMeta();
        ItemMeta templateMeta = template.getItemMeta();
        if (sourceMeta == null || templateMeta == null) return sourceMeta != templateMeta;

        if (sourceMeta.hasDisplayName() != templateMeta.hasDisplayName()) return true;
        if (sourceMeta.hasDisplayName() && !sourceMeta.getDisplayName().equals(templateMeta.getDisplayName())) return true;

        if (sourceMeta.hasLore() != templateMeta.hasLore()) return true;
        if (sourceMeta.hasLore() && !sourceMeta.getLore().equals(templateMeta.getLore())) return true;

        if (!sourceMeta.getItemFlags().equals(templateMeta.getItemFlags())) return true;

        if (sourceMeta instanceof Damageable && templateMeta instanceof Damageable) {
            if (((Damageable) sourceMeta).getDamage() != ((Damageable) templateMeta).getDamage()) return true;
        }

        if (sourceMeta instanceof LeatherArmorMeta && templateMeta instanceof LeatherArmorMeta) {
            if (!((LeatherArmorMeta) sourceMeta).getColor().equals(((LeatherArmorMeta) templateMeta).getColor())) return true;
        }

        if (sourceMeta instanceof PotionMeta && templateMeta instanceof PotionMeta) {
            Color sourceColor = ((PotionMeta) sourceMeta).getColor();
            Color templateColor = ((PotionMeta) templateMeta).getColor();
            if (sourceColor == null ? templateColor != null : !sourceColor.equals(templateColor)) return true;
        }

        return false;
    }

    @NotNull
    private ItemStack reapplyPlayerChanges(@NotNull ItemStack updated, @NotNull ItemStack source) {
        ItemMeta sourceMeta = source.getItemMeta();
        ItemMeta updatedMeta = updated.getItemMeta();
        if (sourceMeta == null || updatedMeta == null) return updated;

        if (sourceMeta.hasDisplayName()) {
            updatedMeta.setDisplayName(sourceMeta.getDisplayName());
        }

        if (sourceMeta.hasLore()) {
            updatedMeta.setLore(sourceMeta.getLore());
        }

        updatedMeta.removeItemFlags(updatedMeta.getItemFlags().toArray(new ItemFlag[0]));
        updatedMeta.addItemFlags(sourceMeta.getItemFlags().toArray(new ItemFlag[0]));

        if (sourceMeta instanceof Damageable && updatedMeta instanceof Damageable) {
            ((Damageable) updatedMeta).setDamage(((Damageable) sourceMeta).getDamage());
        }

        if (sourceMeta instanceof LeatherArmorMeta && updatedMeta instanceof LeatherArmorMeta) {
            ((LeatherArmorMeta) updatedMeta).setColor(((LeatherArmorMeta) sourceMeta).getColor());
        }

        if (sourceMeta instanceof PotionMeta && updatedMeta instanceof PotionMeta) {
            ((PotionMeta) updatedMeta).setColor(((PotionMeta) sourceMeta).getColor());
        }

        updated.setItemMeta(updatedMeta);
        new HashSet<>(updated.getEnchantments().keySet()).forEach(updated::removeEnchantment);
        source.getEnchantments().forEach((enchantment, level) -> updated.addUnsafeEnchantment(enchantment, level));

        return updated;
    }
}
