package su.nightexpress.quantumrpg.gui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.quantumrpg.QListener;
import su.nightexpress.quantumrpg.QuantumRPG;

import java.util.LinkedHashMap;

public abstract class GUI extends QListener<QuantumRPG> implements InventoryHolder {
    protected String title;

    protected int size;

    protected LinkedHashMap<String, GUIItem> items;

    public GUI(QuantumRPG plugin, String title, int size, LinkedHashMap<String, GUIItem> items) {
        super(plugin);
        setTitle(title);
        setSize(size);
        LinkedHashMap<String, GUIItem> map = new LinkedHashMap<>();
        for (GUIItem gi : items.values())
            map.put(gi.getId(), new GUIItem(gi));
        this.items = map;
        registerListeners();
    }

    public void shutdown() {
        unregisterListeners();
    }

    protected boolean ignoreNullClick() {
        return true;
    }

    protected ItemStack getItem(Inventory inv, int slot) {
        ItemStack i = inv.getItem(slot);
        if (i == null)
            return new ItemStack(Material.AIR);
        return new ItemStack(i);
    }

    public final Inventory getInventory() {
        return this.plugin.getServer().createInventory(this, getSize(), getTitle());
    }

    public void open(Player p) {
        p.openInventory(build());
    }

    protected Inventory build(Object... objects) {
        Inventory inv = getInventory();
        for (GUIItem gi : getContent().values()) {
            byte b;
            int i;
            int[] arrayOfInt;
            for (i = (arrayOfInt = gi.getSlots()).length, b = 0; b < i; ) {
                int slot = arrayOfInt[b];
                inv.setItem(slot, gi.getItem());
                b++;
            }
        }
        return inv;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getSize() {
        return this.size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public LinkedHashMap<String, GUIItem> getContent() {
        return this.items;
    }

    public boolean click(Player p, ItemStack item, ContentType type, int slot, InventoryClickEvent e) {
        if (type == ContentType.EXIT) {
            p.closeInventory();
            return false;
        }
        return true;
    }

    public boolean onClose(Player p, InventoryCloseEvent e) {
        return true;
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent e) {
        InventoryHolder ih = e.getInventory().getHolder();
        if (ih == null || !ih.getClass().isInstance(this))
            return;
        String t1 = ChatColor.stripColor(e.getInventory().getTitle());
        String t2 = ChatColor.stripColor(getTitle());
        if (!t1.equalsIgnoreCase(t2))
            return;
        e.setCancelled(true);
        ItemStack item = e.getCurrentItem();
        if (item == null || item.getType() == Material.AIR)
            return;
        click((Player) e.getWhoClicked(), item, GUIUtils.getItemType(item), e.getRawSlot(), e);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        InventoryHolder ih = e.getInventory().getHolder();
        if (ih == null || !ih.getClass().isInstance(this))
            return;
        String t1 = ChatColor.stripColor(e.getInventory().getTitle());
        String t2 = ChatColor.stripColor(getTitle());
        if (!t1.equalsIgnoreCase(t2))
            return;
        onClose((Player) e.getPlayer(), e);
    }
}
