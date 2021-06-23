package su.nightexpress.quantumrpg.modules.list.itemgenerator;

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
import mc.promcteam.engine.manager.IListener;
import mc.promcteam.engine.manager.api.Loadable;
import mc.promcteam.engine.manager.types.ClickType;
import mc.promcteam.engine.utils.DataUT;
import mc.promcteam.engine.utils.ItemUT;
import mc.promcteam.engine.utils.TimeUT;
import mc.promcteam.engine.utils.actions.ActionManipulator;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.ItemGeneratorManager.GeneratorItem;
import su.nightexpress.quantumrpg.modules.list.itemgenerator.generators.AbilityGenerator;
import su.nightexpress.quantumrpg.stats.items.ItemStats;
import su.nightexpress.quantumrpg.stats.items.attributes.stats.DurabilityStat;
import su.nightexpress.quantumrpg.utils.ItemUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ItemAbilityHandler extends IListener<QuantumRPG> implements Loadable {

    public static final Map<ClickType, NamespacedKey> ABILITY_KEYS = new HashMap<>();
    private ItemGeneratorManager itemGen;
    private Map<String, Map<String, Map<ClickType, Long>>> itemCooldown;

    ItemAbilityHandler(@NotNull ItemGeneratorManager itemGen) {
        super(itemGen.plugin);
        this.itemGen = itemGen;
    }

    @Override
    public void setup() {
        this.itemCooldown = new HashMap<>();

        for (ClickType type : ClickType.values()) {
            NamespacedKey key = new NamespacedKey(plugin, "itemgen-ability-" + type.name().toLowerCase());
            ABILITY_KEYS.put(type, key);
        }

        this.registerListeners();
    }

    @Override
    public void shutdown() {
        this.unregisterListeners();

        ABILITY_KEYS.clear();

        if (this.itemCooldown != null) {
            this.itemCooldown.clear();
            this.itemCooldown = null;
        }
    }

    private final boolean useItem(
            @NotNull Player player,
            @NotNull ItemStack item,
            @NotNull GeneratorItem uItem,
            @NotNull ClickType clickType) {

        String abilityId = DataUT.getStringData(item, ABILITY_KEYS.get(clickType));
        if (abilityId == null) return false;

        AbilityGenerator.Ability ability = uItem.getAbilityGenerator().getAbility(abilityId);
        if (ability == null) return false;

        ActionManipulator manipulator = ability.getActions(ItemStats.getLevel(item));
        if (manipulator == null) return false;

        long cooldownLeft = this.getCooldownLeft(player, item, clickType);
        if (cooldownLeft > 0L) {
            String name = ItemUT.getItemName(item);
            String time = TimeUT.formatTime(cooldownLeft);
            plugin.lang().Module_Item_Usage_Cooldown
                    .replace("%time%", time).replace("%item%", name)
                    .send(player);

            return false;
        }

        // TODO Usage Event

        int uses = this.itemGen.getItemCharges(item);
        if (uses == 0) {
            plugin.lang().Module_Item_Usage_NoCharges
                    .replace("%item%", ItemUT.getItemName(item))
                    .send(player);
            return false;
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

        manipulator.process(player);

        // Add Item Ability Cooldown
        String uuid = player.getUniqueId().toString();
        String itemId = uItem.getId();

        Map<String, Map<ClickType, Long>> mapItem = this.itemCooldown.computeIfAbsent(uuid, map -> new HashMap<>());
        mapItem.computeIfAbsent(itemId, map -> new HashMap<>()).put(clickType, ability.getCooldownDate());

        return true;
    }

    public boolean isOnCooldown(@NotNull Player player, @NotNull ItemStack item, @NotNull ClickType type) {
        return this.getCooldownLeft(player, item, type) > 0L;
    }

    /**
     * @param player
     * @param item
     * @param type
     * @return Returns the amount of time until cooldown ends in Miliseconds.
     */
    private final long getCooldownLeft(@NotNull Player player, @NotNull ItemStack item, @NotNull ClickType type) {
        if (!this.itemGen.isItemOfThisModule(item)) return 0L;

        String uuid = player.getUniqueId().toString();
        String itemId = this.itemGen.getItemId(item);

        Map<String, Map<ClickType, Long>> mapItems = this.itemCooldown.getOrDefault(uuid, Collections.emptyMap());

        // Remove expired cooldowns.
        mapItems.values().forEach(mapType -> mapType.entrySet().removeIf(entry -> entry.getValue() <= System.currentTimeMillis()));

        long date = mapItems.getOrDefault(itemId, Collections.emptyMap()).getOrDefault(type, 0L);
        return date > 0L ? date - System.currentTimeMillis() : date;
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
        boolean shift = player.isSneaking();
        ClickType type = ClickType.from(action, shift);

        if (!ItemUtils.isWeapon(item) && !ItemUtils.isBow(item) && item.getType() != Material.SHIELD) {
            e.setCancelled(true);
        }
        this.useItem(player, item, aItem, type);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onItemConsumeNatural(PlayerItemConsumeEvent e) {

        ItemStack item = new ItemStack(e.getItem());
        GeneratorItem aItem = this.itemGen.getModuleItem(item);
        if (aItem == null) return;

        Player player = e.getPlayer();
        ClickType type = ClickType.RIGHT;

        e.setCancelled(true);

        ItemStack itemMain = player.getInventory().getItemInMainHand();
        if (!ItemUT.isAir(itemMain) && itemMain.isSimilar(item)) {
            this.useItem(player, itemMain, aItem, type);
            return;
        }

        ItemStack itemOff = player.getInventory().getItemInOffHand();
        if (!ItemUT.isAir(itemOff) && itemOff.isSimilar(item)) {
            this.useItem(player, itemOff, aItem, type);
        }
    }
}
