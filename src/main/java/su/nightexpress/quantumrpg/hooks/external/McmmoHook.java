package su.nightexpress.quantumrpg.hooks.external;

import com.gmail.nossr50.api.ExperienceAPI;
import org.bukkit.entity.Player;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.hooks.EHook;
import su.nightexpress.quantumrpg.hooks.Hook;
import su.nightexpress.quantumrpg.hooks.HookLevel;

public class McmmoHook extends Hook implements HookLevel {
    public McmmoHook(EHook type, QuantumRPG plugin) {
        super(type, plugin);
    }

    public void setup() {
    }

    public void shutdown() {
    }

    public int getLevel(Player p) {
        return ExperienceAPI.getPowerLevel(p);
    }
}
