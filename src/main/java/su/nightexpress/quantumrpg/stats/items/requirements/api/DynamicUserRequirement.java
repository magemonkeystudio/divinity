package su.nightexpress.quantumrpg.stats.items.requirements.api;

import mc.promcteam.engine.utils.ItemUT;
import mc.promcteam.engine.utils.StringUT;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.quantumrpg.config.EngineCfg;

import java.util.List;

public abstract class DynamicUserRequirement<Z> extends UserRequirement<Z> {

    public DynamicUserRequirement(
            @NotNull String id,
            @NotNull String name,
            @NotNull String format,
            @NotNull String placeholder,
            @NotNull String uniqueTag,
            @NotNull PersistentDataType<?, Z> dataType
    ) {
        super(id, name, format, placeholder, uniqueTag, dataType);
    }

    @Override
    @NotNull
    public String getFormat(@NotNull ItemStack item, @NotNull Z value) {
        return this.getFormat(null, item, value);
    }

    @NotNull
    public String getFormat(@Nullable Player p, @NotNull ItemStack item, @NotNull Z value) {
        String state = "";
        if (EngineCfg.LORE_STYLE_REQ_USER_DYN_UPDATE) {
            boolean canUse = p != null && this.canUse(p, item);
            state = EngineCfg.getDynamicRequirementState(canUse);
        }

        return StringUT.colorFix(super.getFormat(item, value).replace("%state%", state));
    }

    @NotNull
    public ItemStack updateItem(@Nullable Player p, @NotNull ItemStack item) {
        if (!EngineCfg.LORE_STYLE_REQ_USER_DYN_UPDATE) return item;

        int pos = this.getLoreIndex(item);
        if (pos < 0) return item;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        List<String> lore = meta.getLore();
        if (lore == null) return item;

        @Nullable Z arr = this.getRaw(item);
        if (arr == null) return item;

        String formatNew = this.getFormat(p, item, arr);

        lore.set(pos, formatNew);
        meta.setLore(lore);
        item.setItemMeta(meta);

        ItemUT.addLoreTag(item, this.getMetaId(item), formatNew);

        return item;
    }
}
