package studio.magemonkey.divinity.manager.stats;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.InvalidConfigurationException;
import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.commands.api.IGeneralCommand;
import studio.magemonkey.divinity.Divinity;
import studio.magemonkey.divinity.stats.EntityStats;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public final class StatsCommand extends IGeneralCommand<Divinity> implements Listener {

    private StatsCatalog catalog;

    public StatsCommand(@NotNull Divinity plugin) {
        super(plugin, new String[]{"stats"}, null);
        try {
            this.catalog = new StatsCatalog(plugin);
        } catch (Exception e) {
            plugin.error("Failed to load stats settings (" + plugin.getName() + "/stats/settings.yml)");
            e.printStackTrace();
        }
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    @NotNull
    public String usage() {
        return "";
    }

    @Override
    @NotNull
    public String description() {
        return "View your active base, bonus, and total stats.";
    }

    @Override
    public boolean playersOnly() {
        return true;
    }

    @Override
    protected void perform(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (this.catalog == null) {
            sender.sendMessage("§cThe stats menu is disabled or could not be loaded.");
            return;
        }
        this.open((Player) sender, 0);
    }

    public void shutdown() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof StatsHolder)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) return;
        int rawSlot = event.getRawSlot();
        if (rawSlot < 0 || rawSlot >= event.getView().getTopInventory().getSize()) return;

        Player player = (Player) event.getWhoClicked();
        StatsHolder holder = (StatsHolder) event.getView().getTopInventory().getHolder();
        if (rawSlot == this.catalog.getPreviousSlot() && holder.page > 0) {
            this.open(player, holder.page - 1);
        } else if (rawSlot == this.catalog.getNextSlot() && holder.page + 1 < holder.pageCount) {
            this.open(player, holder.page + 1);
        } else if (rawSlot == this.catalog.getCloseSlot()) {
            player.closeInventory();
        }
    }

    @EventHandler
    public void onInventoryDrag(@NotNull InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof StatsHolder) {
            event.setCancelled(true);
        }
    }

    private void open(@NotNull Player player, int requestedPage) {
        try {
            if (!this.catalog.reloadSettings()) {
                this.plugin.getLogger().severe("Could not reload stats settings: configuration file is missing.");
                player.sendMessage("§cThe stats menu settings could not be loaded.");
                return;
            }
        } catch (InvalidConfigurationException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not reload stats settings.", e);
            player.sendMessage("§cThe stats menu settings contain invalid YAML.");
            return;
        }
        if (!this.catalog.isEnabled()) {
            player.sendMessage("§cThe stats menu is disabled.");
            return;
        }

        EntityStats stats = EntityStats.get(player);
        stats.updateAll();
        List<StatsCatalog.Row> rows = this.catalog.getRows(player, stats);
        LinkedHashMap<Integer, StatsCatalog.Row> staticRows = new LinkedHashMap<>();
        List<StatsCatalog.Row> dynamicRows = new ArrayList<>();
        int contentLimit = this.catalog.getInventorySize() - 9;

        for (StatsCatalog.Row row : rows) {
            if (!row.isStatic) {
                dynamicRows.add(row);
                continue;
            }
            if (row.slot < 0 || row.slot >= contentLimit) {
                this.plugin.getLogger().warning("Ignoring invalid static stats slot " + row.slot + " for " + row.path);
                continue;
            }
            if (staticRows.putIfAbsent(row.slot, row) != null) {
                this.plugin.getLogger().warning("Ignoring duplicate static stats slot " + row.slot + " for " + row.path);
            }
        }

        dynamicRows.sort((first, second) -> Integer.compare(first.priority, second.priority));

        List<Integer> dynamicSlots = this.catalog.getDynamicSlots();
        dynamicSlots.removeIf(staticRows::containsKey);
        int pageCapacity = dynamicSlots.size();
        int pageCount = pageCapacity == 0 ? 1
                : Math.max(1, (dynamicRows.size() + pageCapacity - 1) / pageCapacity);
        int page = Math.max(0, Math.min(requestedPage, pageCount - 1));
        StatsHolder holder = new StatsHolder(page, pageCount);
        Inventory inventory = Bukkit.createInventory(holder, this.catalog.getInventorySize(), this.catalog.getTitle());
        holder.inventory = inventory;

        for (Map.Entry<Integer, StatsCatalog.Row> entry : staticRows.entrySet()) {
            inventory.setItem(entry.getKey(), entry.getValue().toItem(this.catalog));
        }

        if (pageCapacity > 0) {
            int start = page * pageCapacity;
            int end = Math.min(start + pageCapacity, dynamicRows.size());
            for (int i = start; i < end; i++) {
                inventory.setItem(dynamicSlots.get(i - start), dynamicRows.get(i).toItem(this.catalog));
            }
        }

        int previousSlot = this.catalog.getPreviousSlot();
        int closeSlot = this.catalog.getCloseSlot();
        int nextSlot = this.catalog.getNextSlot();
        if (previousSlot != closeSlot && previousSlot != nextSlot && page > 0) {
            inventory.setItem(previousSlot, this.button(Material.ARROW, this.catalog.getLabel("previous", "Previous page")));
        }
        inventory.setItem(closeSlot, this.button(Material.BARRIER, this.catalog.getLabel("close", "Close")));
        if (nextSlot != closeSlot && nextSlot != previousSlot && page + 1 < pageCount) {
            inventory.setItem(nextSlot, this.button(Material.ARROW, this.catalog.getLabel("next", "Next page")));
        }
        player.openInventory(inventory);
    }

    private ItemStack button(@NotNull Material material, @NotNull String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static final class StatsHolder implements InventoryHolder {
        private final int page;
        private final int pageCount;
        private Inventory inventory;

        private StatsHolder(int page, int pageCount) {
            this.page = page;
            this.pageCount = pageCount;
        }

        @Override
        public Inventory getInventory() {
            return this.inventory;
        }
    }

}