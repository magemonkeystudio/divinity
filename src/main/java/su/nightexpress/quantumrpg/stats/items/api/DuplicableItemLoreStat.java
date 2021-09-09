package su.nightexpress.quantumrpg.stats.items.api;

import mc.promcteam.engine.utils.ItemUT;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.quantumrpg.utils.LoreUT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class DuplicableItemLoreStat<Z> extends ItemLoreStat<Z> {

    private final Map<Integer, NamespacedKey> keys;
    private final Map<Integer, NamespacedKey> keys2;

    public DuplicableItemLoreStat(
            @NotNull String id,
            @NotNull String name,
            @NotNull String format,
            @NotNull String placeholder,
            @NotNull String uniqueTag,
            @NotNull PersistentDataType<?, Z> dataType
    ) {
        super(id, name, format, placeholder, uniqueTag, dataType);
        this.keys = new HashMap<>();
        this.keys2 = new HashMap<>();
    }

    @NotNull
    protected final NamespacedKey getKey(int index) {
        NamespacedKey key = this.keys.get(index);
        if (key == null) {
            key = new NamespacedKey(plugin, this.getMetaTag() + this.getId() + index);
            this.keys.put(index, key);
        }
        return key;
    }

    @NotNull
    protected final NamespacedKey getKey2(int index) {
        NamespacedKey key2 = this.keys2.get(index);
        if (key2 == null) {
            key2 = NamespacedKey.fromString("quantumrpg:" + this.getMetaTag() + this.getId() + index);
            this.keys2.put(index, key2);
        }
        return key2;
    }

    @Override
    public boolean add(@NotNull ItemStack item, @NotNull Z value, int line) {
        return this.add(item, value, -1, line);
    }

    public boolean add(@NotNull ItemStack item, @NotNull Z value, int index, int line) {
        if (index < 0) index = this.getAmount(item);

        //System.out.println("Adding new index: " + index);
        item = this.preparePlaceholder(item, index, line); // Replace current text with placeholder
        item = this.remove(item, index, true); // Remove meta tag. Does not removes lore as it replaced above.

        String format = this.getFormat(item, value);

        // Now replace the placeholder with requirement value
        // or delete it in case of requirement removal.
        if (format.isEmpty()) {
            LoreUT.replacePlaceholder(item, this.getPlaceholder(), null);
            ItemUT.delLoreTag(item, this.getMetaId(item, index));
        } else {
            LoreUT.replacePlaceholder(item, this.getPlaceholder(), format);

            ItemMeta meta = item.getItemMeta();
            if (meta == null) return false;

            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(this.getKey(index), this.dataType, value);
            item.setItemMeta(meta);

            ItemUT.addLoreTag(item, this.getMetaId(item, index), format);
        }

        return !format.isEmpty();
    }

    @NotNull
    public final ItemStack remove(@NotNull ItemStack item, int index, boolean onlyTag) {
        if (index < 0) index = this.getAmount(item) - 1;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        if (!onlyTag) {
            int pos = this.getLoreIndex(item, index);
            if (pos >= 0) {
                List<String> lore = meta.getLore();
                if (lore != null) {
                    lore.remove(pos);
                    meta.setLore(lore);
                }
            }
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(this.getKey(index), this.dataType)) {
            container.remove(this.getKey(index));
        } else if (container.has(this.getKey2(index), this.dataType)) {
            container.remove(this.getKey2(index));
        }

        item.setItemMeta(meta);

        ItemUT.delLoreTag(item, this.getMetaId(item, index));

        if (!onlyTag) {
            this.reorderTags(item);
        }

        return item;
    }

    // Reorder index values of current Item Stat.
    // When current Item Stat got removed from the item,
    // current stat keys on item will missing removed index (like 1,2,3,4 -> 1,2,4)
    // that may cause unexpected plugin behavior.
    private final void reorderTags(@NotNull ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        int                     amountLeft = this.getAmount(item);
        PersistentDataContainer container  = meta.getPersistentDataContainer();

        List<Z> valuesLeft = new ArrayList<>();

        //System.out.println("Reordering: " + amountLeft + " Left");

        int index = 0;
        while (valuesLeft.size() < amountLeft) {
            //System.out.println("Index: " + index + "/" + amountLeft);
            NamespacedKey key  = this.getKey(index++);
            NamespacedKey key2 = this.getKey2(index++);

            if (container.has(key, this.dataType)) {
                //System.out.println("Key found, saving value...");
                @Nullable Z val = container.get(key, this.dataType);
                if (val != null) {
                    valuesLeft.add(val);
                }
            } else if (container.has(key2, this.dataType)) {
                //System.out.println("Key found, saving value...");
                @Nullable Z val = container.get(key2, this.dataType);
                if (val != null) {
                    valuesLeft.add(val);
                }
            }
            if (index > this.keys.size()) {
                plugin.warn("Stat keys reordering: Interrupted potential infinity loop! Contact the plugin dev!");
                break;
            }
        }

        //System.out.println("Applying new values...");
        for (int newIndex = 0; newIndex < valuesLeft.size(); newIndex++) {
            //System.out.println("Applied index " + newIndex);
            container.set(this.getKey(newIndex), this.dataType, valuesLeft.get(newIndex));
        }

        item.setItemMeta(meta);
    }

    @Nullable
    public final Z getRaw(@NotNull ItemStack item, int index) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(this.getKey(index), this.dataType)) {
            return container.get(this.getKey(index), this.dataType);
        } else if (container.has(this.getKey2(index), this.dataType)) {
            return container.get(this.getKey2(index), this.dataType);
        }
        return null;
    }

    public final int getAmount(@NotNull ItemStack item) {
        int value = 0;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return value;
        List<String> lore = meta.getLore();
        if (lore == null) return value;

        PersistentDataContainer container = meta.getPersistentDataContainer();

        for (int index = 0; index < lore.size(); index++) {
            if (container.has(this.getKey(index), this.dataType)
                    || container.has(this.getKey2(index), this.dataType)) value++;
        }
        return value;
    }

    public final int getLoreIndex(@NotNull ItemStack item, int index) {
        if (index < 0) return -1;
        int count = 0;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return -1;
        List<String> lore = meta.getLore();
        if (lore == null) return -1;

        for (int i = 0; i < lore.size(); i++) {
            String id    = this.getMetaId(item, i);
            int    found = ItemUT.getLoreIndex(item, id);

            if (found >= 0 && index == count++) {
                return found;
            }
        }
        return -1;
    }

    @NotNull
    protected final ItemStack preparePlaceholder(@NotNull ItemStack item, int index, int line) {
        if (!this.hasPlaceholder(item)) {
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return item;
            List<String> lore = meta.getLore();
            if (lore == null) lore = new ArrayList<>();

            int pos = this.getLoreIndex(item, index);
            LoreUT.addOrReplace(lore, pos, line, this.getPlaceholder());

            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}
