package su.nightexpress.quantumrpg.hooks.external;

import mc.promcteam.engine.hooks.HookState;
import mc.promcteam.engine.hooks.NHook;
import me.lokka30.levelledmobs.LevelledMobs;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.hooks.HookMobLevel;

public class LevelledMobsHK extends NHook<QuantumRPG> implements HookMobLevel {

    public LevelledMobsHK(@NotNull QuantumRPG plugin) {
        super(plugin);
    }

    @Override
    @NotNull
    protected HookState setup() {
        return HookState.SUCCESS;
    }

    @Override
    protected void shutdown() {}

    @Override
    public double getMobLevel(@NotNull Entity entity) {
        if (!(entity instanceof LivingEntity)) return 0.0D;
        if (Bukkit.getPluginManager().getPlugin("LevelledMobs") == null || !LevelledMobs.getInstance().isEnabled())
            return 0.0D;

        return LevelledMobs.getInstance().levelManager.getLevelOfMob((LivingEntity) entity);
    }
}
