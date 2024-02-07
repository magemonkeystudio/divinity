package su.nightexpress.quantumrpg.stats.items.requirements.user;

import mc.promcteam.engine.config.api.ILangMsg;
import mc.promcteam.engine.utils.DataUT;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.Perms;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.config.EngineCfg;
import su.nightexpress.quantumrpg.hooks.HookClass;
import su.nightexpress.quantumrpg.stats.items.ItemTags;
import su.nightexpress.quantumrpg.stats.items.requirements.api.DynamicUserRequirement;
import su.nightexpress.quantumrpg.utils.LoreUT;


public class BannedClassRequirement extends DynamicUserRequirement<String[]> {

    public BannedClassRequirement(@NotNull String name, @NotNull String format) {
        super("banned_class",
                name,
                format,
                ItemTags.PLACEHOLDER_REQ_USER_BANNED_CLASS,
                ItemTags.TAG_REQ_USER_BANNED_CLASS,
                DataUT.STRING_ARRAY);
        this.keys.add(NamespacedKey.fromString("prorpgitems:item_user_banned_classbanned-class"));
    }

    @Override
    @NotNull
    public String getBypassPermission() {
        return Perms.BYPASS_REQ_USER_CLASS;
    }

    @Override
    public boolean canUse(@NotNull Player p, @NotNull ItemStack item) {
        HookClass classPlugin = EngineCfg.HOOK_PLAYER_CLASS_PLUGIN;
        if (classPlugin == null) return true;

        String[] itemClass = this.getRaw(item);
        if (itemClass == null || itemClass.length == 0) return true;

        String playerClass = classPlugin.getClass(p);
        for (String reqClass : itemClass) {
            if (playerClass.equalsIgnoreCase(reqClass)) {
                return false;
            }
        }
        return true;
    }

    @Override
    @NotNull
    public String formatValue(@NotNull ItemStack item, @NotNull String[] values) {
        if (values.length == 0 || values[0].isEmpty()) return "";

        String sep   = EngineCfg.LORE_STYLE_REQ_USER_BANNED_CLASS_FORMAT_SEPAR;
        String color = EngineCfg.LORE_STYLE_REQ_USER_BANNED_CLASS_FORMAT_COLOR;

        return LoreUT.getStrSeparated(values, sep, color, EngineCfg.LORE_STYLE_REQ_USER_BANNED_CLASS_FORMAT_MAX, EngineCfg.LORE_STYLE_REQ_USER_BANNED_CLASS_FORMAT_NEWLINE);
    }

    @Override
    @NotNull
    public ILangMsg getDenyMessage(@NotNull Player p, @NotNull ItemStack src) {
        return QuantumRPG.getInstance().lang().Module_Item_Interact_Error_Class;
    }
}
