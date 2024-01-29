package su.nightexpress.quantumrpg.stats.items.attributes;

import mc.promcteam.engine.utils.NumberUT;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.config.EngineCfg;
import su.nightexpress.quantumrpg.stats.items.ItemTags;
import su.nightexpress.quantumrpg.stats.items.api.ItemLoreStat;

public class SkillAPIAttribute extends ItemLoreStat<Integer> {

    public SkillAPIAttribute(
            @NotNull String id,
            @NotNull String name,
            @NotNull String format) {
        super(id, name, format.replace("%name%", name), "%SKILLAPI_ATTRIBUTE_"+id+"%", ItemTags.TAG_ITEM_SKILLAPI_ATTR, PersistentDataType.INTEGER);
    }

    @Override
    public @NotNull String formatValue(@NotNull ItemStack item, @NotNull Integer value) {
        return (value > 0 ? EngineCfg.LORE_CHAR_POSITIVE : EngineCfg.LORE_CHAR_NEGATIVE) + NumberUT.format(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!obj.getClass().equals(this.getClass())) return false;
        SkillAPIAttribute other = (SkillAPIAttribute) obj;
        return this.getId().equalsIgnoreCase(other.getId());
    }

    @Override
    public int hashCode() {
        return this.getId().hashCode();
    }
}
