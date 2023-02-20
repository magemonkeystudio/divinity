package su.nightexpress.quantumrpg.modules.list.itemgenerator;

import mc.promcteam.engine.api.armor.ArmorEquipEvent;
import mc.promcteam.engine.manager.IListener;
import mc.promcteam.engine.manager.api.Loadable;
import mc.promcteam.engine.utils.DataUT;
import mc.promcteam.engine.utils.ItemUT;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.hooks.EHook;
import su.nightexpress.quantumrpg.hooks.external.SkillAPIHK;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.ItemGeneratorManager.GeneratorItem;
import su.nightexpress.quantumrpg.stats.items.ItemStats;
import su.nightexpress.quantumrpg.stats.items.attributes.stats.DurabilityStat;
import su.nightexpress.quantumrpg.utils.ItemUtils;

import java.util.*;

public class ItemAbilityHandler extends IListener<QuantumRPG> implements Loadable {

    public static NamespacedKey                                  ABILITY_KEY;
    private final ItemGeneratorManager                           itemGen;
    private final List<UUID>                                     noSpam       = new ArrayList<>();
    private       SkillAPIHK                                     skillAPIHK;

    ItemAbilityHandler(@NotNull ItemGeneratorManager itemGen) {
        super(itemGen.plugin);
        this.itemGen = itemGen;
        ItemAbilityHandler.ABILITY_KEY = NamespacedKey.fromString("skills", itemGen.plugin);
    }

    @Override
    public void setup() {
        this.skillAPIHK = (SkillAPIHK) QuantumRPG.getInstance().getHook(EHook.SKILL_API);
        if (this.skillAPIHK == null) { return; }
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
        Bukkit.getScheduler().runTaskLater(QuantumRPG.getInstance(),
                () -> noSpam.remove(player.getUniqueId()), 60L);
        return true;
    }

    private Map<String,Integer> getAbilities(ItemStack item) {
        Map<String,Integer> map = new HashMap<>();
        if (item == null) { return map; }
        String[] stringAbilities = DataUT.getStringArrayData(item, ABILITY_KEY);
        if (stringAbilities == null) { return map; }
        for (String stringAbility : stringAbilities) {
            int i = stringAbility.lastIndexOf(':');
            int level;
            try {
                level = Integer.parseInt(stringAbility.substring(i+1));
            } catch (NumberFormatException e) {
                continue;
            }
            map.put(stringAbility.substring(0, i), level);
        }
        return map;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onArmorEquip(ArmorEquipEvent event) {
        Map<String,Integer> abilities = getAbilities(event.getOldArmorPiece());
        for (String id : abilities.keySet()) {
            skillAPIHK.removeSkill(event.getPlayer(), id);
        }
        abilities = getAbilities(event.getNewArmorPiece());
        for (Map.Entry<String,Integer> entry : abilities.entrySet()) {
            skillAPIHK.addSkill(event.getPlayer(), entry.getKey(), entry.getValue());
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

        Map<String,Integer> abilities = getAbilities(item);
        for (Map.Entry<String,Integer> entry : abilities.entrySet()) {
            skillAPIHK.castSkill(player, entry.getKey(), entry.getValue());
        }

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
