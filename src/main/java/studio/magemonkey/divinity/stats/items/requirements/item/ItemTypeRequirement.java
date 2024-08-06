package studio.magemonkey.divinity.stats.items.requirements.item;

import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.config.api.ILangMsg;
import studio.magemonkey.codex.util.DataUT;
import studio.magemonkey.divinity.Divinity;
import studio.magemonkey.divinity.config.EngineCfg;
import studio.magemonkey.divinity.stats.items.ItemTags;
import studio.magemonkey.divinity.stats.items.requirements.api.ItemRequirement;
import studio.magemonkey.divinity.utils.ItemUtils;
import studio.magemonkey.divinity.utils.LoreUT;

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

        // Legacy keys
        this.keys.add(NamespacedKey.fromString("prorpgitems:req_item_type"));
        this.keys.add(NamespacedKey.fromString("prorpgitems:qrpg_req_item_typetype"));
        this.keys.add(NamespacedKey.fromString("quantumrpg:qrpg_req_item_typetype"));
    }

    @Override
    @NotNull
    public Class<String[]> getParameterClass() {
        return String[].class;
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

        return Divinity.getInstance().lang().Module_Item_Apply_Error_Type.replace("%value%",
                this.formatValue(src, arr));
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
