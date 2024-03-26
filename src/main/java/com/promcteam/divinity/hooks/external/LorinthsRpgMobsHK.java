package com.promcteam.divinity.hooks.external;

import com.promcteam.codex.hooks.HookState;
import com.promcteam.codex.hooks.NHook;
import com.promcteam.divinity.Divinity;
import com.promcteam.divinity.hooks.HookMobLevel;
import me.lorinth.rpgmobs.LorinthsRpgMobs;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class LorinthsRpgMobsHK extends NHook<Divinity> implements HookMobLevel {

    public LorinthsRpgMobsHK(@NotNull Divinity plugin) {
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
