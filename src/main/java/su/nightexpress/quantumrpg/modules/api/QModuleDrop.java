package su.nightexpress.quantumrpg.modules.api;

import mc.promcteam.engine.config.api.JYML;
import mc.promcteam.engine.utils.ItemUT;
import mc.promcteam.engine.utils.StringUT;
import mc.promcteam.engine.utils.random.Rnd;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.config.EngineCfg;
import su.nightexpress.quantumrpg.modules.ModuleItem;
import su.nightexpress.quantumrpg.stats.items.ItemStats;
import su.nightexpress.quantumrpg.stats.items.ItemTags;
import su.nightexpress.quantumrpg.stats.items.attributes.ChargesAttribute;
import su.nightexpress.quantumrpg.stats.items.requirements.ItemRequirements;

import java.lang.reflect.Constructor;
import java.util.*;

public abstract class QModuleDrop<I extends ModuleItem> extends QModule {

    public static final String RANDOM_ID = "random";

    protected Class<I>         clazz;
    protected Map<String, I>   items;
    protected ChargesAttribute chargesAtt;

    private String       itemNameFormat;
    private List<String> itemLoreFormat;

    public QModuleDrop(@NotNull QuantumRPG plugin, @NotNull Class<I> clazz) {
        super(plugin);
        this.clazz = clazz;
        this.chargesAtt = ItemStats.getAttribute(ChargesAttribute.class);
    }

    /**
     * @return Path to module items folder without the path of plugin folder.
     */
    @NotNull
    public final String getItemsFolder() {
        return this.getPath() + "items";
    }

    protected void loadSettings() {
        String path = "item-format.";
        cfg.addMissing(path + "name", ItemTags.PLACEHOLDER_ITEM_NAME);
        cfg.addMissing(path + "lore", Arrays.asList(ItemTags.PLACEHOLDER_ITEM_LORE));
        cfg.saveChanges();

        this.itemNameFormat = StringUT.color(cfg.getString(path + "name", ItemTags.PLACEHOLDER_ITEM_NAME));
        this.itemLoreFormat = StringUT.color(cfg.getStringList(path + "lore"));
    }

    protected void loadItems() {
        this.items = new TreeMap<>();
        this.plugin.getConfigManager().extractFullPath(plugin.getDataFolder() + this.getItemsFolder());

        for (JYML cfg : JYML.loadAll(plugin.getDataFolder() + this.getItemsFolder(), true)) {
            @Nullable I item;
            try {
                Constructor<I> ctor = clazz.getDeclaredConstructor(this.getClass(), QuantumRPG.class, JYML.class);
                item = ctor.newInstance(this, plugin, cfg);
                if (item == null) continue;
            } catch (Exception e) {
                this.error("Could not load item: " + cfg.getFile().getName());
                e.printStackTrace();
                continue;
            }

            this.items.put(item.getId().toLowerCase(), item);
        }
    }

    @NotNull
    public String getItemNameFormat() {
        return this.itemNameFormat;
    }

    @NotNull
    public List<String> getItemLoreFormat() {
        return this.itemLoreFormat;
    }

    @Nullable
    public I getItemById(@NotNull String id) {
        if (this.items.isEmpty()) return null;

        if (id.equalsIgnoreCase(RANDOM_ID)) {
            return Rnd.get(new ArrayList<>(this.getItems()));
        }
        return items.get(id.toLowerCase());
    }

    @NotNull
    public Collection<I> getItems() {
        if (items == null) return Collections.emptyList();
        return this.items.values();
    }

    @NotNull
    public List<String> getItemIds() {
        List<String> list = new ArrayList<>(this.items.keySet());
        list.add(RANDOM_ID);
        return list;
    }

    @Nullable
    public final String getItemId(@NotNull ItemStack item) {
        if (!this.isItemOfThisModule(item)) return null;

        return ItemStats.getId(item);
    }

    @Nullable
    public I getModuleItem(@NotNull ItemStack item) {
        String id = this.getItemId(item);
        if (id == null) return null;

        return this.getItemById(id);
    }

    public final boolean isItemOfThisModule(@NotNull ItemStack item) {
        QModuleDrop<?> e = ItemStats.getModule(item);
        return e != null && e.equals(this);
    }

    @Override
    public void unload() {
        if (this.items != null) {
            this.items.clear();
            this.items = null;
        }

        super.unload();
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public final void onDragDropEvent(InventoryClickEvent e) {
        if (e.getInventory().getType() != InventoryType.CRAFTING) return;
        if (e.getSlotType() == SlotType.CRAFTING) return;
        if (e.getSlotType() == SlotType.ARMOR || e.getSlot() == 40) return;

        ItemStack target = e.getCurrentItem();
        if (target == null || target.getType() == Material.AIR) return;

        ItemStack src = e.getCursor();
        if (src == null || src.getType() == Material.AIR) return;

        if (target.isSimilar(src)) return;

        @Nullable I mItem = this.getModuleItem(src);
        if (mItem == null) return;

        Player p = (Player) e.getWhoClicked();
        if (p.getGameMode() == GameMode.CREATIVE) {
            plugin.lang().Module_Item_Interact_Error_Creative.send(p);
            return;
        }

        if (!ItemRequirements.canApply(p, src, target)) {
            return;
        }

        ItemStack current = src.clone();
        if (this.onDragDrop(p, current, target.clone(), mItem, e)) {
            e.setCancelled(true);
            if (current.getAmount() > 0) e.getView().setCursor(current);
        }
    }

    public final void splitDragItem(@NotNull Player p, @Nullable ItemStack src, @Nullable ItemStack target) {
        if (src != null) {
            ItemStack srcConsumed = new ItemStack(src);
            srcConsumed.setAmount(src.getAmount() - 1);
            src.setAmount(1);
            ItemUT.addItem(p, srcConsumed);
        }
        if (target != null) {
            p.getInventory().removeItem(target);
            this.splitDragItem(p, target, null);
        }
    }

    protected boolean onDragDrop(
            @NotNull Player p,
            @NotNull ItemStack src,
            @NotNull ItemStack target,
            @NotNull I mItem,
            @NotNull InventoryClickEvent e) {

        return false;
    }

    protected final void takeChargeClickEvent(
            @NotNull Player p, @NotNull ItemStack src, @NotNull InventoryClickEvent e) {
        int uses = this.getItemCharges(src);
        if (uses < 0) return; // Unlimited or Exhausted

        ItemStack toModify;
        ItemStack toSave;

        if (src.getAmount() > 1) {
            toSave = new ItemStack(src);
            toSave.setAmount(src.getAmount() - 1);

            toModify = src;
            toModify.setAmount(1);
        } else {
            toModify = src;
            toSave = null;
        }

        if (uses == 0)
            toModify.setAmount(0);
        else
            this.takeItemCharge(toModify);

        if (!ItemUT.isAir(toModify)) {
            e.getView().setCursor(toModify);
            if (toSave != null) {
                ItemUT.addItem(p, toSave);
            }
        } else {
            if (toSave != null) {
                e.getView().setCursor(toSave);
            }
        }
    }

    @Nullable
    public final void takeItemCharge(@NotNull ItemStack item) {
        if (this.chargesAtt == null) return;

        boolean doBreak = EngineCfg.CHARGES_BREAK_ITEMS_ENABLED
                && !EngineCfg.CHARGES_BREAK_ITEMS_STOP_MODULES.contains(this.getId());

        this.chargesAtt.takeCharges(item, 1, doBreak);
    }

    public final int getItemCharges(@NotNull ItemStack stack) {
        if (this.chargesAtt == null) return 0;

        int[] values = this.chargesAtt.getRaw(stack);
        if (values == null) return 0;

        return values[0];
    }
}
