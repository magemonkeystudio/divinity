package com.promcteam.divinity.hooks.external;

import com.promcteam.codex.hooks.HookState;
import com.promcteam.codex.hooks.NHook;
import com.promcteam.divinity.Divinity;
import com.shampaggon.crackshot.CSDirector;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CrackShotHK extends NHook<Divinity> {

    private CSDirector main;

    public CrackShotHK(@NotNull Divinity plugin) {
        super(plugin);
    }

    @Override
    @NotNull
    protected HookState setup() {
        try {
            this.main = ((CSDirector) plugin.getPluginManager().getPlugin(this.getPlugin()));
            return HookState.SUCCESS;
        } catch (Exception ex) {
            ex.printStackTrace();
            return HookState.ERROR;
        }
    }

    @Override
    protected void shutdown() {

    }

    public double getWeaponMeleeDamage(@NotNull Player player) {
        String wpnName = this.main.returnParentNode(player);
        double wpnDmg  = this.main.getInt(wpnName + ".Shooting.Projectile_Damage");

        return wpnDmg;
    }
}