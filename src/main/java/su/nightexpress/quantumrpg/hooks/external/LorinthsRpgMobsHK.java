package su.nightexpress.quantumrpg.hooks.external;

import mc.promcteam.engine.hooks.HookState;
import mc.promcteam.engine.hooks.NHook;
import me.lorinth.rpgmobs.LorinthsRpgMobs;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.hooks.HookMobLevel;

public class LorinthsRpgMobsHK extends NHook<QuantumRPG> implements HookMobLevel {

    public LorinthsRpgMobsHK(@NotNull QuantumRPG plugin) {
        super(plugin);
    }

    @Override
    @NotNull
    protected HookState setup() {
        return HookState.SUCCESS;
    }

    @Override
    protected void shutdown() {

    }

    @Override
    public double getMobLevel(@NotNull Entity entity) {
        return LorinthsRpgMobs.GetLevelOfEntity(entity).intValue();
    }

}
