package su.nightexpress.quantumrpg.stats.items.requirements.user.hooks;

import com.gmail.nossr50.api.ExperienceAPI;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import mc.promcteam.engine.config.api.ILangMsg;
import mc.promcteam.engine.utils.DataUT;
import mc.promcteam.engine.utils.StringUT;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.Perms;
import su.nightexpress.quantumrpg.config.EngineCfg;
import su.nightexpress.quantumrpg.stats.items.ItemTags;
import su.nightexpress.quantumrpg.stats.items.requirements.api.DynamicUserRequirement;

import java.util.Arrays;
import java.util.function.Predicate;

public class McMMORequirement extends DynamicUserRequirement<String[]> {
    private final String format;

    public McMMORequirement(@NotNull String name,
                            @NotNull String format
    ) {
        super("mcmmo-skill",
                name,
                format,
                ItemTags.PLACEHOLDER_REQ_USER_MCMMO_SKILL,
                ItemTags.TAG_REQ_USER_MCMMO_SKILL,
                DataUT.STRING_ARRAY);
        this.format = format;
    }

    /*
    TODO
     */
    @Override
    public @NotNull String getBypassPermission() {
        return Perms.BYPASS_REQ_USER_MCMMO_SKILL;
    }

    @Override
    public boolean canUse(@NotNull Player player, @NotNull ItemStack item) {
        String[] itemClass = this.getRaw(item);
        if (itemClass == null || itemClass.length == 0) return true;

        PrimarySkillType skill = PrimarySkillType.valueOf(itemClass[0].toUpperCase());
        int min       = StringUT.getInteger(itemClass[1], -1);
        int max       = StringUT.getInteger(itemClass[2], 0);
        int skillLevel = ExperienceAPI.getLevel(player, skill);

        return min == max ? (skillLevel >= min) : (skillLevel >= min && skillLevel <= max);
    }

    @Override
    public @NotNull String formatValue(@NotNull ItemStack item, @NotNull String[] value) {
        PrimarySkillType skill = PrimarySkillType.valueOf(value[0].toUpperCase());
        int v1       = StringUT.getInteger(value[1], -1);
        int v2       = StringUT.getInteger(value[2], -1);
        int min = Math.min(v1, v2);
        int max = Math.max(v1, v2);
        if(min <= 0 || Arrays.stream(PrimarySkillType.values()).noneMatch(Predicate.isEqual(skill)))
            return "";

        String lvl;
        if (min == max) {
            lvl = EngineCfg.LORE_STYLE_REQ_USER_MCMMO_SKILL_FORMAT_SINGLE.replace("%min%", String.valueOf(min));
        } else {
            lvl = EngineCfg.LORE_STYLE_REQ_USER_MCMMO_SKILL_FORMAT_RANGE.replace("%max%", String.valueOf(max)).replace("%min%", String.valueOf(min));
        }
        String skillName = EngineCfg.LORE_STYLE_REQ_USER_MCMMO_SKILL_FORMAT_SKILL.replace("%skill%", skill.getName());
        return format.replace("%name%", skillName).replace("%value%", lvl);
    }

    @Override
    public @NotNull ILangMsg getDenyMessage(@NotNull Player player, @NotNull ItemStack src) {
        return plugin.lang().Module_Item_Interact_Error_McMMO_Skill;
    }
}
