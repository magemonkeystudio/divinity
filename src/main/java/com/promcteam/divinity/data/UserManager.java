package com.promcteam.divinity.data;

import com.promcteam.codex.data.users.IUserManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import com.promcteam.divinity.QuantumRPG;
import com.promcteam.divinity.data.api.RPGUser;

public class UserManager extends IUserManager<QuantumRPG, RPGUser> {

    public UserManager(@NotNull QuantumRPG plugin) {
        super(plugin);
    }

    @Override
    @NotNull
    protected RPGUser createData(@NotNull Player player) {
        return new RPGUser(this.plugin, player);
    }
}
