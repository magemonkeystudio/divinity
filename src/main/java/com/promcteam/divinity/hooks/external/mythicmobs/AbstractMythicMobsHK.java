package com.promcteam.divinity.hooks.external.mythicmobs;

import com.promcteam.codex.hooks.NHook;
import com.promcteam.divinity.Divinity;
import com.promcteam.divinity.hooks.HookMobLevel;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractMythicMobsHK extends NHook<Divinity> implements HookMobLevel {

    public AbstractMythicMobsHK(@NotNull Divinity plugin) {
        super(plugin);
    }

    public abstract boolean isMythicMob(@NotNull Entity entity);

    @NotNull
    public abstract String getMythicNameByEntity(@NotNull Entity entity);

    public abstract double getMobLevel(@NotNull Entity entity);

    public abstract void setSkillDamage(@NotNull Entity entity, double amount);

    public abstract AbstractMythicEntity getMythicInstance(@NotNull Entity entity);

    public abstract int getMythicVersion();
}
