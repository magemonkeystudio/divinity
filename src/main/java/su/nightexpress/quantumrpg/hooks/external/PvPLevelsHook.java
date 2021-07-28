package su.nightexpress.quantumrpg.hooks.external;

import me.MathiasMC.PvPLevels.PvPLevels;
import org.bukkit.entity.Player;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.hooks.EHook;
import su.nightexpress.quantumrpg.hooks.Hook;
import su.nightexpress.quantumrpg.hooks.HookLevel;

public class PvPLevelsHook extends Hook implements HookLevel {
    private PvPLevels api;

    public PvPLevelsHook(EHook type, QuantumRPG plugin) {
        super(type, plugin);
    }

    public void setup() {
        this.api = new PvPLevels();
    }

    public void shutdown() {
    }

    public int getLevel(Player p) {
        return (int) this.api.getPlayerConnect(p.getUniqueId().toString()).getLevel();
    }
}
