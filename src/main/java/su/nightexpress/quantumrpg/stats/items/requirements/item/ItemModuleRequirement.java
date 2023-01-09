package su.nightexpress.quantumrpg.stats.items.requirements.item;

import mc.promcteam.engine.config.api.ILangMsg;
import mc.promcteam.engine.modules.IModule;
import mc.promcteam.engine.utils.DataUT;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.config.EngineCfg;
import su.nightexpress.quantumrpg.modules.api.QModule;
import su.nightexpress.quantumrpg.modules.api.QModuleDrop;
import su.nightexpress.quantumrpg.stats.items.ItemStats;
import su.nightexpress.quantumrpg.stats.items.ItemTags;
import su.nightexpress.quantumrpg.stats.items.requirements.api.ItemRequirement;
import su.nightexpress.quantumrpg.utils.LoreUT;

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

        return plugin.lang().Module_Item_Apply_Error_Module.replace("%value%", this.formatValue(src, arr));
    }

    @Override
    @NotNull
    public String formatValue(@NotNull ItemStack item, @NotNull String[] values) {
        List<String> valid = new ArrayList<>();
        for (String module : values) {
            IModule<?> mod = plugin.getModuleManager().getModule(module);
            if (mod == null || !(mod instanceof QModuleDrop<?>)) continue;

            valid.add(mod.name());
        }

        String[] modNames = valid.toArray(new String[valid.size()]);
        String   sep      = EngineCfg.LORE_STYLE_REQ_ITEM_MODULE_FORMAT_SEPAR;
        String   color    = EngineCfg.LORE_STYLE_REQ_ITEM_MODULE_FORMAT_COLOR;

        return LoreUT.getStrSeparated(modNames, sep, color);
    }
}
