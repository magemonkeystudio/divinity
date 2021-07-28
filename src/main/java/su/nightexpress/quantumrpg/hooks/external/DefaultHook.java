package su.nightexpress.quantumrpg.hooks.external;

import org.bukkit.entity.Player;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.hooks.EHook;
import su.nightexpress.quantumrpg.hooks.Hook;
import su.nightexpress.quantumrpg.hooks.HookClass;
import su.nightexpress.quantumrpg.hooks.HookLevel;

public class DefaultHook extends Hook implements HookLevel, HookClass {
    public DefaultHook(EHook type, QuantumRPG plugin) {
        super(type, plugin);
    }

    public void setup() {
    }

    public void shutdown() {
    }

    public int getLevel(Player p) {
        return p.getLevel();
    }

    public String getClass(Player p) {
        return "None";
    }
}
