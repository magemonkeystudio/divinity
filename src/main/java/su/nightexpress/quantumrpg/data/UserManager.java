package su.nightexpress.quantumrpg.data;

import mc.promcteam.engine.data.users.IUserManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.data.api.RPGUser;

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
