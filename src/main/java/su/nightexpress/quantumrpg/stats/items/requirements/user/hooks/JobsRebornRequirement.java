package su.nightexpress.quantumrpg.stats.items.requirements.user.hooks;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.container.Job;
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

public class JobsRebornRequirement extends DynamicUserRequirement<String[]> {

    public JobsRebornRequirement(@NotNull String name,
                                 @NotNull String format
    ) {
        super("jobs-job",
                name,
                format,
                ItemTags.PLACEHOLDER_REQ_USER_JOBS_JOB,
                ItemTags.TAG_REQ_USER_JOBS_JOB,
                DataUT.STRING_ARRAY);
    }


    @Override
    public @NotNull String getBypassPermission() {
        return Perms.BYPASS_REQ_USER_JOBS_JOB;
    }

    @Override
    public boolean canUse(@NotNull Player player, @NotNull ItemStack item) {
        String[] itemClass = this.getRaw(item);
        if (itemClass == null || itemClass.length == 0) return true;

        int min = StringUT.getInteger(itemClass[1], -1);
        int max = StringUT.getInteger(itemClass[2], 0);
        int jobLevel = Jobs.getPlayerManager().getJobsPlayer(player).getJobProgression(Jobs.getJob(itemClass[0])).getLevel();
        return min == max ? (jobLevel >= min) : (jobLevel >= min && jobLevel <= max);
    }

    @Override
    public @NotNull String formatValue(@NotNull ItemStack item, @NotNull String[] value) {
        Job job = Jobs.getJob(value[0]);
        int v1 = StringUT.getInteger(value[1], -1);
        int v2 = StringUT.getInteger(value[2], -1);
        int min = Math.min(v1, v2);
        int max = Math.max(v1, v2);
        if (min <= 0 || job == null)
            return "";

        String lore;
        if (min == max) {
            lore = EngineCfg.LORE_STYLE_REQ_USER_JOBS_JOB_FORMAT_SINGLE.replace("%job%", job.getName()).replace("%min%", String.valueOf(min));
        } else {
            lore = EngineCfg.LORE_STYLE_REQ_USER_JOBS_JOB_FORMAT_RANGE.replace("%job%", job.getName()).replace("%max%", String.valueOf(max)).replace("%min%", String.valueOf(min));
        }
        return ChatColor.WHITE + lore;
    }

    @Override
    public @NotNull ILangMsg getDenyMessage(@NotNull Player player, @NotNull ItemStack src) {
        return plugin.lang().Module_Item_Interact_Error_Jobs_Job;
    }
}
