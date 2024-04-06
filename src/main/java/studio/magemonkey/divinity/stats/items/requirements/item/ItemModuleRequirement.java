package studio.magemonkey.divinity.stats.items.requirements.item;

import studio.magemonkey.codex.config.api.ILangMsg;
import studio.magemonkey.codex.modules.IModule;
import studio.magemonkey.codex.util.DataUT;
import studio.magemonkey.divinity.Divinity;
import studio.magemonkey.divinity.config.EngineCfg;
import studio.magemonkey.divinity.modules.api.QModule;
import studio.magemonkey.divinity.modules.api.QModuleDrop;
import studio.magemonkey.divinity.stats.items.ItemStats;
import studio.magemonkey.divinity.stats.items.ItemTags;
import studio.magemonkey.divinity.stats.items.requirements.api.ItemRequirement;
import studio.magemonkey.divinity.utils.LoreUT;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ItemModuleRequirement extends ItemRequirement<String[]> {

    public ItemModuleRequirement(
            @NotNull String name,
            @NotNull String format
    ) {
        super(
                "module",
                name,
                format,
                ItemTags.PLACEHOLDER_REQ_ITEM_MODULE,
                ItemTags.TAG_REQ_ITEM_LEVEL,
                DataUT.STRING_ARRAY);

        // Legacy keys
        this.keys.add(NamespacedKey.fromString("prorpgitems:qrpg_req_item_levelmodule"));
        this.keys.add(NamespacedKey.fromString("quantumrpg:qrpg_req_item_levelmodule"));
    }

    @Override
    @NotNull
    public Class<String[]> getParameterClass() {
        return String[].class;
    }

    @Override
    public boolean canApply(@NotNull ItemStack src, @NotNull ItemStack target) {
        String[] modules = this.getRaw(src);
        if (modules == null || modules.length == 0) return true;

        QModule targetModule = ItemStats.getModule(target);
        if (targetModule == null) return false;

        return ArrayUtils.contains(modules, targetModule.getId());
    }

    @Override
    public ILangMsg getApplyMessage(@NotNull ItemStack src, @NotNull ItemStack target) {
        String[] arr = this.getRaw(src);
        if (arr == null) throw new IllegalStateException("Item does not have stat!");

        return Divinity.getInstance().lang().Module_Item_Apply_Error_Module.replace("%value%",
                this.formatValue(src, arr));
    }

    @Override
    @NotNull
    public String formatValue(@NotNull ItemStack item, @NotNull String[] values) {
        List<String> valid = new ArrayList<>();
        for (String module : values) {
            IModule<?> mod = Divinity.getInstance().getModuleManager().getModule(module);
            if (mod == null || !(mod instanceof QModuleDrop<?>)) continue;

            valid.add(mod.name());
        }

        String[] modNames = valid.toArray(new String[valid.size()]);
        String   sep      = EngineCfg.LORE_STYLE_REQ_ITEM_MODULE_FORMAT_SEPAR;
        String   color    = EngineCfg.LORE_STYLE_REQ_ITEM_MODULE_FORMAT_COLOR;

        return LoreUT.getStrSeparated(modNames, sep, color);
    }
}
