package com.promcteam.divinity.hooks.external;

import com.promcteam.codex.hooks.HookState;
import com.promcteam.codex.hooks.NHook;
import me.lorinth.rpgmobs.LorinthsRpgMobs;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import com.promcteam.divinity.QuantumRPG;
import com.promcteam.divinity.hooks.HookMobLevel;

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
