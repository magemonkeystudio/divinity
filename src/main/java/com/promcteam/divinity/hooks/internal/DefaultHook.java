package com.promcteam.divinity.hooks.internal;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.jetbrains.annotations.NotNull;
import com.promcteam.divinity.Perms;
import com.promcteam.divinity.QuantumRPG;
import com.promcteam.divinity.hooks.HookClass;
import com.promcteam.divinity.hooks.HookLevel;
import com.promcteam.divinity.hooks.HookMobLevel;

public class DefaultHook implements HookLevel, HookClass, HookMobLevel {

    //private QuantumRPG plugin;

    public DefaultHook(@NotNull QuantumRPG plugin) {
        //this.plugin = plugin;
    }

    @Override
    public int getLevel(@NotNull Player player) {
        return player.getLevel();
    }

    @Override
    public void giveExp(@NotNull Player player, int amount) {
        player.giveExp(amount);
    }

    @Override
    @NotNull
    public String getClass(@NotNull Player player) {
        String klass = "";
        for (PermissionAttachmentInfo pio : player.getEffectivePermissions()) {
            String perm = pio.getPermission();
            if (perm.startsWith(Perms.CLASS)) {
                String ending = perm.substring(perm.lastIndexOf("."), perm.length());
                klass = ending.replace(".", "");
            }
        }
        return klass;
    }

    @Override
    public void takeMana(@NotNull Player player, double amount, boolean ofMax) {
        throw new UnsupportedOperationException("Your class plugin does not provides mana function.");
    }

    @Override
    public double getMobLevel(@NotNull Entity entity) {
        return 1;
    }
}
