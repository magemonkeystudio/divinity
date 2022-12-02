package su.nightexpress.quantumrpg.stats.items.requirements.item;

import mc.promcteam.engine.config.api.ILangMsg;
import mc.promcteam.engine.utils.DataUT;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.config.EngineCfg;
import su.nightexpress.quantumrpg.stats.items.ItemTags;
import su.nightexpress.quantumrpg.stats.items.requirements.api.ItemRequirement;
import su.nightexpress.quantumrpg.utils.ItemUtils;
import su.nightexpress.quantumrpg.utils.LoreUT;

public class ItemTypeRequirement extends ItemRequirement<String[]> {

    public ItemTypeRequirement(
            @NotNull String name,
            @NotNull String format
    ) {
        super(
                "type",
                name,
                format,
                ItemTags.PLACEHOLDER_REQ_ITEM_TYPE,
                ItemTags.TAG_REQ_ITEM_TYPE,
                DataUT.STRING_ARRAY);
    }

    @Override
    public boolean canApply(@NotNull ItemStack src, @NotNull ItemStack target) {
        String[] types = this.getRaw(src);
        if (types == null || types.length == 0) return true;

        return ItemUtils.compareItemGroup(target, types);
    }

    @Override
    public ILangMsg getApplyMessage(@NotNull ItemStack src, @NotNull ItemStack target) {
        String[] arr = this.getRaw(src);
        if (arr == null) throw new IllegalStateException("Item does not have stat!");

        return plugin.lang().Module_Item_Apply_Error_Type.replace("%value%", this.formatValue(src, arr));
    }

    @Override
    @NotNull
    public String formatValue(@NotNull ItemStack item, @NotNull String[] values) {
        String[] localized = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            String name = ItemUtils.getLocalizedGroupName(values[i]);
            localized[i] = name != null ? name : "";
        }
        if (ArrayUtils.isEmpty(localized)) return "";

        String sep   = EngineCfg.LORE_STYLE_REQ_ITEM_TYPE_FORMAT_SEPAR;
        String color = EngineCfg.LORE_STYLE_REQ_ITEM_TYPE_FORMAT_COLOR;

        return LoreUT.getStrSeparated(localized, sep, color);
    }
}
