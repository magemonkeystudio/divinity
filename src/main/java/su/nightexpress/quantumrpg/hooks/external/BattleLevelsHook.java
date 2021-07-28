package su.nightexpress.quantumrpg.hooks.external;

import me.robin.battlelevels.api.BattleLevelsAPI;
import org.bukkit.entity.Player;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.hooks.EHook;
import su.nightexpress.quantumrpg.hooks.Hook;
import su.nightexpress.quantumrpg.hooks.HookLevel;

public class BattleLevelsHook extends Hook implements HookLevel {
    public BattleLevelsHook(EHook type, QuantumRPG plugin) {
        super(type, plugin);
    }

    public void setup() {
    }

    public void shutdown() {
    }

    public int getLevel(Player p) {
        return BattleLevelsAPI.getLevel(p.getUniqueId());
    }
}
