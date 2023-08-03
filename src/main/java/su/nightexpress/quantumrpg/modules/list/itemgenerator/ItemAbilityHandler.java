package su.nightexpress.quantumrpg.modules.list.itemgenerator;

import mc.promcteam.engine.api.armor.ArmorEquipEvent;
import mc.promcteam.engine.manager.IListener;
import mc.promcteam.engine.manager.api.Loadable;
import mc.promcteam.engine.utils.ItemUT;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.hooks.EHook;
import su.nightexpress.quantumrpg.hooks.external.SkillAPIHK;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.ItemGeneratorManager.GeneratorItem;
import su.nightexpress.quantumrpg.stats.items.ItemStats;
import su.nightexpress.quantumrpg.stats.items.attributes.stats.DurabilityStat;
import su.nightexpress.quantumrpg.utils.ItemUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ItemAbilityHandler extends IListener<QuantumRPG> implements Loadable {

    private final ItemGeneratorManager                           itemGen;
    private final List<UUID>                                     noSpam       = new ArrayList<>();

    ItemAbilityHandler(@NotNull ItemGeneratorManager itemGen) {
        super(itemGen.plugin);
        this.itemGen = itemGen;
    }

    @Override
    public void setup() {
        this.registerListeners();
    }

    @Override
    public void shutdown() {
        this.unregisterListeners();
    }

    private boolean registerSentMessage(Player player) {
        if (noSpam.contains(player.getUniqueId())) return false;

        noSpam.add(player.getUniqueId());
        //TODO Make this cooldown configurable.
        Bukkit.getScheduler().runTaskLater(this.plugin,
                () -> noSpam.remove(player.getUniqueId()), 60L);
        return true;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        SkillAPIHK skillAPIHK = (SkillAPIHK) this.plugin.getHook(EHook.SKILL_API);
        if (skillAPIHK != null) {skillAPIHK.updateSkills(event.getPlayer());}
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemHeldEvent(PlayerItemHeldEvent event) {
        SkillAPIHK skillAPIHK = (SkillAPIHK) this.plugin.getHook(EHook.SKILL_API);
        if (skillAPIHK != null) {skillAPIHK.updateSkills(event.getPlayer());}
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onArmorEquip(ArmorEquipEvent event) {
        SkillAPIHK skillAPIHK = (SkillAPIHK) this.plugin.getHook(EHook.SKILL_API);
        if (skillAPIHK != null) {skillAPIHK.updateSkills(event.getPlayer());}
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryView view = event.getView();
        for (Inventory inventory : new Inventory[]{view.getTopInventory(), view.getBottomInventory()}) {
            if (!(inventory instanceof PlayerInventory)) { continue; }
            PlayerInventory playerInventory = (PlayerInventory) inventory;
            HumanEntity humanEntity = playerInventory.getHolder();
            if (!(humanEntity instanceof Player)) { return; }
            SkillAPIHK skillAPIHK = (SkillAPIHK) this.plugin.getHook(EHook.SKILL_API);
            if (skillAPIHK != null) {skillAPIHK.updateSkills((Player) humanEntity);}
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        InventoryView view = event.getView();
        for (Inventory inventory : new Inventory[]{view.getTopInventory(), view.getBottomInventory()}) {
            if (!(inventory instanceof PlayerInventory)) { continue; }
            PlayerInventory playerInventory = (PlayerInventory) inventory;
            HumanEntity humanEntity = playerInventory.getHolder();
            if (!(humanEntity instanceof Player)) { return; }
            SkillAPIHK skillAPIHK = (SkillAPIHK) this.plugin.getHook(EHook.SKILL_API);
            if (skillAPIHK != null) {skillAPIHK.updateSkills((Player) humanEntity);}
        }
    }

    private final boolean useItem(@NotNull Player player, @NotNull ItemStack item) {
        int uses = this.itemGen.getItemCharges(item);
        if (uses == 0) {
            if (registerSentMessage(player)) {
                plugin.lang().Module_Item_Usage_NoCharges
                        .replace("%item%", ItemUT.getItemName(item))
                        .send(player);
            }
            return false;
        }

        // TODO Usage Event

        if (uses > 0) {
            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);

                ItemStack reuse = new ItemStack(item);
                reuse.setAmount(1);
                this.itemGen.takeItemCharge(reuse);

                if (reuse.getType() != Material.AIR) {
                    ItemUT.addItem(player, reuse);
                }
            } else {
                this.itemGen.takeItemCharge(item);
            }
        } else {
            DurabilityStat duraStat = ItemStats.getStat(DurabilityStat.class);
            if (duraStat != null) {
                duraStat.reduceDurability(player, item, 1);
            }
        }
        return true;
    }

    // ------------------------------------------------------------- //
    // E V E N T S

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onItemConsume(PlayerInteractEvent e) {
        if (e.useItemInHand() == Result.DENY) return;

        EquipmentSlot hand = e.getHand();
        if (hand == null) return;

        ItemStack item = e.getItem();
        if (item == null) return;

        // Let use the natural consume animation.
        if (item.getType().isEdible() || item.getType() == Material.POTION) return;

        GeneratorItem aItem = this.itemGen.getModuleItem(item);
        if (aItem == null) return;

        Player player = e.getPlayer();
        Action action = e.getAction();
        if (action == Action.PHYSICAL) return;

        if (!ItemUtils.isWeapon(item) && !ItemUtils.isBow(item) && item.getType() != Material.SHIELD) {
            e.setCancelled(true);
        }
        this.useItem(player, item);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onItemConsumeNatural(PlayerItemConsumeEvent e) {

        ItemStack     item  = new ItemStack(e.getItem());
        GeneratorItem aItem = this.itemGen.getModuleItem(item);
        if (aItem == null) return;

        Player    player = e.getPlayer();

        e.setCancelled(true);

        ItemStack itemMain = player.getInventory().getItemInMainHand();
        if (!ItemUT.isAir(itemMain) && itemMain.isSimilar(item)) {
            this.useItem(player, itemMain);
            return;
        }

        ItemStack itemOff = player.getInventory().getItemInOffHand();
        if (!ItemUT.isAir(itemOff) && itemOff.isSimilar(item)) {
            this.useItem(player, itemOff);
        }
    }
}
