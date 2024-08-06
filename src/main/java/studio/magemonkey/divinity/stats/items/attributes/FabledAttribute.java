package studio.magemonkey.divinity.stats.items.attributes;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.util.NumberUT;
import studio.magemonkey.divinity.config.EngineCfg;
import studio.magemonkey.divinity.stats.items.ItemTags;
import studio.magemonkey.divinity.stats.items.api.ItemLoreStat;

public class FabledAttribute extends ItemLoreStat<Integer> {

    public FabledAttribute(
            @NotNull String id,
            @NotNull String name,
            @NotNull String format) {
        super(id,
                name,
                format.replace("%name%", name),
                "%FABLED_ATTRIBUTE_" + id + "%",
                ItemTags.TAG_ITEM_FABLED_ATTR,
                PersistentDataType.INTEGER);

        // Legacy keys
        this.keys.add(NamespacedKey.fromString("prorpgitems:item_skillapi_attr_" + this.getId()));
        this.keys.add(NamespacedKey.fromString("prorpgitems:qrpg_item_skillapi_attr_" + this.getId()));
    }

    @Override
    @NotNull
    public Class<Integer> getParameterClass() {
        return Integer.class;
    }

    @Override
    public @NotNull String formatValue(@NotNull ItemStack item, @NotNull Integer value) {
        return (value > 0 ? EngineCfg.LORE_CHAR_POSITIVE : EngineCfg.LORE_CHAR_NEGATIVE) + NumberUT.format(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!obj.getClass().equals(this.getClass())) return false;
        FabledAttribute other = (FabledAttribute) obj;
        return this.getId().equalsIgnoreCase(other.getId());
    }

    @Override
    public int hashCode() {
        return this.getId().hashCode();
    }
}
