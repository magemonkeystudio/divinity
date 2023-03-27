package su.nightexpress.quantumrpg.hooks.external;

import com.gmail.nossr50.api.ExperienceAPI;
import mc.promcteam.engine.hooks.HookState;
import mc.promcteam.engine.hooks.NHook;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.hooks.HookLevel;

public class McmmoHK extends NHook<QuantumRPG> implements HookLevel {

    public McmmoHK(@NotNull QuantumRPG plugin) {
        super(plugin);
    }

    @Override
    @NotNull
    public HookState setup() {
        return HookState.SUCCESS;
    }

    @Override
    public void shutdown() {

    }

    @Override
    public int getLevel(@NotNull Player player) {
        return ExperienceAPI.getPowerLevel(player);
    }

    @Override
    public void giveExp(@NotNull Player player, int amount) {
        // Don't really know which skill would be right here since exp can come from several sources,
        // will leave empty unless this is brought up again
    }
}
