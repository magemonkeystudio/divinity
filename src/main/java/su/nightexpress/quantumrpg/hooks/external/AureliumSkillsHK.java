package su.nightexpress.quantumrpg.hooks.external;

import mc.promcteam.engine.hooks.HookState;
import mc.promcteam.engine.hooks.NHook;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.QuantumRPG;

public class AureliumSkillsHK extends NHook<QuantumRPG> {

    public AureliumSkillsHK(@NotNull QuantumRPG plugin) {
        super(plugin);
    }

    @Override
    protected @NotNull HookState setup() {
        return HookState.SUCCESS;
    }

    @Override
    protected void shutdown() {

    }
}
