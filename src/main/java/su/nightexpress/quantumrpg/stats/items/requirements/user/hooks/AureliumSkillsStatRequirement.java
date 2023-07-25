package su.nightexpress.quantumrpg.stats.items.requirements.user.hooks;

import com.archyx.aureliumskills.api.AureliumAPI;
import com.archyx.aureliumskills.stats.Stat;
import mc.promcteam.engine.config.api.ILangMsg;
import mc.promcteam.engine.utils.DataUT;
import mc.promcteam.engine.utils.StringUT;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.Perms;
import su.nightexpress.quantumrpg.config.EngineCfg;
import su.nightexpress.quantumrpg.stats.items.ItemTags;
import su.nightexpress.quantumrpg.stats.items.requirements.api.DynamicUserRequirement;

import java.util.Locale;

public class AureliumSkillsStatRequirement extends DynamicUserRequirement<String[]> {

    public AureliumSkillsStatRequirement(@NotNull String name,
                                         @NotNull String format
    ) {
        super("aurelium-stat",
                name,
                format,
                ItemTags.PLACEHOLDER_REQ_USER_AURELIUM_SKILLS_STAT,
                ItemTags.TAG_REQ_USER_AURELIUM_SKILLS_STAT,
                DataUT.STRING_ARRAY);
    }


    @Override
    public @NotNull String getBypassPermission() {
        return Perms.BYPASS_REQ_USER_AURELIUM_SKILLS_STAT;
    }

    @Override
    public boolean canUse(@NotNull Player player, @NotNull ItemStack item) {
        String[] itemClass = this.getRaw(item);
        if (itemClass == null || itemClass.length == 0) return true;

        double min = StringUT.getDouble(itemClass[1], -1);
        double max = StringUT.getDouble(itemClass[2], 0);
        double jobLevel = AureliumAPI.getStatLevel(player, AureliumAPI.getPlugin().getStatRegistry().getStat(itemClass[0]));
        return min == max ? (jobLevel >= min) : (jobLevel >= min && jobLevel <= max);
    }

    @Override
    public @NotNull String formatValue(@NotNull ItemStack item, @NotNull String[] value) {
        Stat stat = AureliumAPI.getPlugin().getStatRegistry().getStat(value[0]);
        int v1 = StringUT.getInteger(value[1], -1);
        int v2 = StringUT.getInteger(value[2], -1);
        int min = Math.min(v1, v2);
        int max = Math.max(v1, v2);
        if (min <= 0 || stat == null)
            return "";

        String lore;
        if (min == max) {
            lore = EngineCfg.LORE_STYLE_REQ_USER_AURELIUM_SKILLS_STAT_FORMAT_SINGLE.replace("%stat%", stat.getDisplayName(Locale.ENGLISH)).replace("%min%", String.valueOf(min));
        } else {
            lore = EngineCfg.LORE_STYLE_REQ_USER_AURELIUM_SKILLS_STAT_FORMAT_RANGE.replace("%stat%", stat.getDisplayName(Locale.ENGLISH)).replace("%max%", String.valueOf(max)).replace("%min%", String.valueOf(min));
        }
        return ChatColor.WHITE + lore;
    }

    @Override
    public @NotNull ILangMsg getDenyMessage(@NotNull Player player, @NotNull ItemStack src) {
        return plugin.lang().Module_Item_Interact_Error_Aurelium_Skills_Stat;
    }
}
