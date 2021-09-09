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
import su.nightexpress.quantumrpg.stats.items.ItemStats;
import su.nightexpress.quantumrpg.stats.items.attributes.stats.SimpleStat;
import su.nightexpress.quantumrpg.utils.LoreUT;

import java.util.ArrayList;
import java.util.List;

public abstract class ItemLoreStat<Z> {

    protected static QuantumRPG               plugin = QuantumRPG.getInstance();
    protected final  String                   format;
    protected final  String                   placeholder;
    protected final  String                   metaId;
    private final    String                   id;
    private final    String                   uniqueMetaTag;
    private final    NamespacedKey            key;
    private final    NamespacedKey            key2;
    private final    boolean                  isMulti;
    protected        String                   name;
    protected        PersistentDataType<?, Z> dataType;

    public ItemLoreStat(
            @NotNull String id,
            @NotNull String name,
            @NotNull String format,
            @NotNull String placeholder,
            @NotNull String uniqueTag,
            @NotNull PersistentDataType<?, Z> dataType
    ) {
        this.id = id.toLowerCase();
        this.uniqueMetaTag = uniqueTag.toLowerCase();
        this.dataType = dataType;
        this.key = new NamespacedKey(plugin, this.getMetaTag() + this.getId());
        this.key2 = NamespacedKey.fromString("quantumrpg:" + this.getMetaTag() + this.getId());
        this.metaId = this.getMetaTag() + this.getId();

        this.name = StringUT.color(name);
        this.format = StringUT.color(format.replace("%name%", this.getName()));
        this.placeholder = placeholder.toUpperCase();

        this.isMulti = this instanceof DuplicableItemLoreStat;
    }

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
        if (this.isMulti) {
            DuplicableItemLoreStat<?> duplic = (DuplicableItemLoreStat<?>) this;
            id += duplic.getAmount(item);
        }
        return id;
    }

    public final String getMetaId(@NotNull ItemStack item, int index) {
        if (!this.isMulti) {
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
    protected final NamespacedKey getKey() {
        this.validateMethod();
        return this.key;
    }

    @NotNull
    protected final NamespacedKey getKey2() {
        this.validateMethod();
        return this.key2;
    }


    @NotNull
    protected final String getMetaTag() {
        return this.uniqueMetaTag;
    }

    @NotNull
    public final String getPlaceholder() {
        return this.placeholder;
    }

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
        if (!lore.contains(this.getPlaceholder())) {
            LoreUT.addOrReplace(lore, pos, line, this.getPlaceholder());
        }

        pos = lore.indexOf(this.getPlaceholder());

        // *** REMOVE() ****
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(this.getKey(), this.dataType)) {
            container.remove(this.getKey());
        }

        String format = StringUT.colorFix(this.getFormat(item, value));
        if (!format.isEmpty()) {
            container.set(this.getKey(), this.dataType, value);
            lore.set(pos, format);
        } else {
            lore.remove(pos);
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        ItemUT.delLoreTag(item, this.getMetaId(item));

        if (!format.isEmpty()) {
            ItemUT.addLoreTag(item, this.getMetaId(item), format);
        }

//		if (this instanceof SimpleStat) {
        ItemStats.updateVanillaAttributes(item);
//		}
        return !format.isEmpty();
    }

    @NotNull
    public final void remove(@NotNull ItemStack item) {
        this.validateMethod();

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(this.getKey(), this.dataType)) {
            container.remove(this.getKey());

            List<String> lore = meta.getLore();
            if (lore != null && !lore.contains(this.getPlaceholder())) {
                int pos = this.getLoreIndex(item);
                if (pos >= 0) {
                    lore.remove(pos);
                    meta.setLore(lore);
                }
            }

            item.setItemMeta(meta);

            if (this instanceof SimpleStat) {
                ItemStats.updateVanillaAttributes(item);
            }
        }

        ItemUT.delLoreTag(item, this.getMetaId(item));

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
        if (container.has(this.getKey(), this.dataType)) {
            return container.get(this.getKey(), this.dataType);
        } else if (container.has(this.getKey2(), this.dataType)) {
            return container.get(this.getKey2(), this.dataType);
        }
        return null;
    }

    public final int getLoreIndex(@NotNull ItemStack item) {
        this.validateMethod();
        return ItemUT.getLoreIndex(item, this.getMetaId(item));
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
        if (this.isMulti) {
            throw new UnsupportedOperationException("Attempt to manage duplicable stat at NULL index. Index must be provided.");
        }
    }
}
