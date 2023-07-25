package su.nightexpress.quantumrpg.stats.items.requirements.user.hooks;

import com.archyx.aureliumskills.api.AureliumAPI;
import com.archyx.aureliumskills.skills.Skill;
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

public class AureliumSkillsSkillRequirement extends DynamicUserRequirement<String[]> {

    public AureliumSkillsSkillRequirement(@NotNull String name,
                                          @NotNull String format
    ) {
        super("aurelium-skill",
                name,
                format,
                ItemTags.PLACEHOLDER_REQ_USER_AURELIUM_SKILLS_SKILL,
                ItemTags.TAG_REQ_USER_AURELIUM_SKILLS_SKILL,
                DataUT.STRING_ARRAY);
    }


    @Override
    public @NotNull String getBypassPermission() {
        return Perms.BYPASS_REQ_USER_AURELIUM_SKILLS_SKILL;
    }

    @Override
    public boolean canUse(@NotNull Player player, @NotNull ItemStack item) {
        String[] itemClass = this.getRaw(item);
        if (itemClass == null || itemClass.length == 0) return true;

        int min = StringUT.getInteger(itemClass[1], -1);
        int max = StringUT.getInteger(itemClass[2], 0);
        int jobLevel = AureliumAPI.getSkillLevel(player, AureliumAPI.getPlugin().getSkillRegistry().getSkill(itemClass[0]));
        return min == max ? (jobLevel >= min) : (jobLevel >= min && jobLevel <= max);
    }

    @Override
    public @NotNull String formatValue(@NotNull ItemStack item, @NotNull String[] value) {
        Skill skill = AureliumAPI.getPlugin().getSkillRegistry().getSkill(value[0]);
        int v1 = StringUT.getInteger(value[1], -1);
        int v2 = StringUT.getInteger(value[2], -1);
        int min = Math.min(v1, v2);
        int max = Math.max(v1, v2);
        if (min <= 0 || skill == null)
            return "";

        String lore;
        if (min == max) {
            lore = EngineCfg.LORE_STYLE_REQ_USER_AURELIUM_SKILLS_SKILL_FORMAT_SINGLE.replace("%skill%", skill.getDisplayName(Locale.ENGLISH)).replace("%min%", String.valueOf(min));
        } else {
            lore = EngineCfg.LORE_STYLE_REQ_USER_AURELIUM_SKILLS_SKILL_FORMAT_RANGE.replace("%skill%", skill.getDisplayName(Locale.ENGLISH)).replace("%max%", String.valueOf(max)).replace("%min%", String.valueOf(min));
        }
        return ChatColor.WHITE + lore;
    }

    @Override
    public @NotNull ILangMsg getDenyMessage(@NotNull Player player, @NotNull ItemStack src) {
        return plugin.lang().Module_Item_Interact_Error_Aurelium_Skills_Skill;
    }
}
