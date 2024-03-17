package su.nightexpress.quantumrpg.stats.items.api;

import mc.promcteam.engine.utils.ItemUT;
import mc.promcteam.engine.utils.StringUT;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.config.EngineCfg;
import su.nightexpress.quantumrpg.stats.items.ItemStats;
import su.nightexpress.quantumrpg.stats.items.attributes.api.TypedStat;
import su.nightexpress.quantumrpg.utils.LoreUT;

import java.util.ArrayList;
import java.util.List;

public abstract class ItemLoreStat<Z> {
    private final    String                   id;
    protected        String                   name;
    protected final  String                   format;
    protected final  String                   placeholder;
    protected final  List<NamespacedKey>      keys;
    protected        PersistentDataType<?, Z> dataType;
    protected final  String                   metaId;

    public ItemLoreStat(
            @NotNull String id,
            @NotNull String name,
            @NotNull String format,
            @NotNull String placeholder,
            @NotNull String uniqueTag,
            @NotNull PersistentDataType<?, Z> dataType) {
        this.id = id.toLowerCase();
        this.name = StringUT.color(name);
        this.format = StringUT.color(format.replace("%name%", this.getName()));
        this.placeholder = placeholder.toUpperCase();

        this.keys = new ArrayList<>();
        uniqueTag = uniqueTag.toLowerCase();
        this.metaId = uniqueTag.endsWith(this.id) ? uniqueTag : uniqueTag + this.id;
        keys.add(new NamespacedKey(QuantumRPG.getInstance(), this.metaId));

        this.dataType = dataType;
    }

    @NotNull
    public abstract Class<Z> getParameterClass();

    @NotNull
    public final String getId() {
        return this.id;
    }

    @NotNull
    public final String getName() {
        return this.name;
    }

    public final String getMetaId(@NotNull ItemStack item) {
        String id = this.metaId;
        if (this instanceof DuplicableItemLoreStat) {
            DuplicableItemLoreStat<?> duplic = (DuplicableItemLoreStat<?>) this;
            id += duplic.getAmount(item);
        }
        return id;
    }

    public final String getMetaId(@NotNull ItemStack item, int index) {
        if (!(this instanceof DuplicableItemLoreStat)) {
            return this.getMetaId(item);
        }
        return this.metaId + index;
    }


    /**
     * Defines if only ONE variant of this Item Stat can be applied to the item at the same time.
     * Ammo Stat will return 'TRUE' as bow may have only one ammo type,
     * Damage Stat will return 'FALSE' as item may have multiple damage types.
     */
    protected boolean isSingle() {
        return false;
    }

    @NotNull
    protected final List<NamespacedKey> getKeys() {
        this.validateMethod();
        return this.keys;
    }

    @NotNull
    public final NamespacedKey getKey() {
        this.validateMethod();
        return this.keys.get(0);
    }

    @NotNull
    public final String getPlaceholder() {
        return this.placeholder;
    }

    @NotNull
    public String getFormat() { return this.format; }

    @NotNull
    public String getFormat(@NotNull ItemStack item, @NotNull Z value) {
        String sVal = this.formatValue(item, value);
        if (sVal.isEmpty()) return "";

        String[] colorFixer = this.format.split("%value%");
        String   valueColor = colorFixer.length > 0 ? ChatColor.getLastColors(colorFixer[0]) : "";

        return StringUT.colorFix(this.format.replace("%value%", valueColor + sVal));
    }

    public boolean add(@NotNull ItemStack item, @NotNull Z value, int line) {
        int pos = this.getLoreIndex(item);

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        List<String> lore = meta.getLore();
        if (lore == null) lore = new ArrayList<>();

        // *** PREPARE PLACEHOLDER ***
        // Add raw placeholder to item lore or
        // replace the old requirement string
        if (!lore.contains(this.getPlaceholder()) && !this.formatValue(item, value).equals(EngineCfg.LORE_STYLE_ATT_CHARGES_FORMAT_UNLIMITED)) {
            LoreUT.addOrReplace(lore, pos, line, this.getPlaceholder());
        }

        pos = lore.indexOf(this.getPlaceholder());

        // *** REMOVE() ****
        PersistentDataContainer container = meta.getPersistentDataContainer();
        for (NamespacedKey key : this.keys) {
            if (container.has(key, this.dataType)) container.remove(key);
        }

        String[] format  = StringUT.colorFix(this instanceof DynamicStat ? ((DynamicStat<Z>) this).getFormat(null, item, value) : this.getFormat(item, value)).split("\n");
        boolean  isEmpty = true;
        for (String formatLine : format) {
            if (!formatLine.isEmpty()) {
                isEmpty = false;
                break;
            }
        }
        if (isEmpty) {
            if (pos != -1) {
                lore.remove(pos);
            }
        } else {
            container.set(this.getKey(), this.dataType, value);
            if (pos != -1) {
                lore.set(pos, format[0]);
                for (int i = 1; i < format.length; i++) {
                    lore.add(pos + i, format[i]);
                }
            }
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        if (this instanceof DuplicableItemLoreStat) {
            int amount = ((DuplicableItemLoreStat<?>) this).getAmount(item);
            for (NamespacedKey key : this.keys) {
                ItemUT.delLoreTag(item, key.getKey()+amount);
            }
        } else {
            for (NamespacedKey key : this.keys) {
                ItemUT.delLoreTag(item, key.getKey());
            }
        }

        if (!isEmpty) {
            ItemUT.addLoreTag(item, this.getMetaId(item), format[0]);
            for (int i = 1; i < format.length; i++) {
                ItemUT.addLoreTag(item, this.getMetaId(item) + i, format[i]);
            }
        }

//		if (this instanceof SimpleStat) {
        ItemStats.updateVanillaAttributes(item, null);
//		}
        return !isEmpty;
    }

    @NotNull
    public final void remove(@NotNull ItemStack item) {
        this.validateMethod();

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        boolean foundAny = false;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        for (NamespacedKey key : this.keys) {
            if (container.has(key, this.dataType)) {
                container.remove(key);
                foundAny = true;
            }
        }
        if (foundAny) {
            List<String> lore = meta.getLore();
            if (lore != null && !lore.contains(this.getPlaceholder())) {
                int pos = this.getLoreIndex(item);
                if (pos >= 0) {
                    lore.remove(pos);
                    meta.setLore(lore);
                }
            }

            item.setItemMeta(meta);

            if (this instanceof TypedStat) {
                ItemStats.updateVanillaAttributes(item, null);
            }
        }

        if (this instanceof DuplicableItemLoreStat) {
            int amount = ((DuplicableItemLoreStat<?>) this).getAmount(item);
            for (NamespacedKey key : this.keys) {
                ItemUT.delLoreTag(item, key.getKey()+amount);
            }
        } else {
            for (NamespacedKey key : this.keys) {
                ItemUT.delLoreTag(item, key.getKey());
            }
        }

        if (this.isSingle()) {
            // FIXME An issue where applying Duplicable stat without removing previous one
            // meta will contain both values and return the incorrect one.
            // If remove getId() from the key, then it will break attribute detection, because
            // multiple attributes will have the same key.
            // At the moment this is persist for AmmoAttribute and HandAttribute.

            // TODO Clean up previous value of this single stat.
        }
    }

    @Nullable
    public final Z getRaw(@NotNull ItemStack item) {
        this.validateMethod();

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        PersistentDataContainer container = meta.getPersistentDataContainer();
        for (NamespacedKey key : this.keys) {
            if (container.has(key, this.dataType)) return container.get(key, this.dataType);
        }
        return null;
    }

    public final int getLoreIndex(@NotNull ItemStack item) {
        if (this instanceof DuplicableItemLoreStat) {
            int amount = ((DuplicableItemLoreStat <?>) this).getAmount(item);
            for (NamespacedKey key : this.keys) {
                int found = ItemUT.getLoreIndex(item, key.getKey()+amount);
                if (found != 0) return found;
            }
        } else {
            for (NamespacedKey key : this.keys) {
                int found = ItemUT.getLoreIndex(item, key.getKey());
                if (found != 0) return found;
            }
        }
        return 0;
    }

    /**
     * @param item
     * @return true if stat is present in item lore (position >= 0).
     * Does not checks for item meta.
     */
    public final boolean isApplied(@NotNull ItemStack item) {
        return this.getLoreIndex(item) >= 0;
    }

    public final boolean hasPlaceholder(@NotNull ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        List<String> lore = meta.getLore();
        if (lore == null) return false;

        return lore.contains(this.getPlaceholder());
    }

    @NotNull
    protected final void preparePlaceholder(@NotNull ItemStack item, int line) {
        this.validateMethod();

        // Add raw placeholder to item lore or
        // replace the old requirement string
        if (!this.hasPlaceholder(item)) {
            int pos = this.getLoreIndex(item);

            ItemMeta meta = item.getItemMeta();
            if (meta == null) return;
            List<String> lore = meta.getLore();
            if (lore == null) lore = new ArrayList<>();

            LoreUT.addOrReplace(lore, pos, line, this.getPlaceholder());

            meta.setLore(lore);
            item.setItemMeta(meta);
        }
    }

    @NotNull
    public abstract String formatValue(@NotNull ItemStack item, @NotNull Z value);

    private final void validateMethod() {
        if (this instanceof DuplicableItemLoreStat) {
            throw new UnsupportedOperationException("Attempt to manage duplicable stat at NULL index. Index must be provided.");
        }
    }
}
