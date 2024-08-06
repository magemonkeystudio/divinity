package studio.magemonkey.divinity.data;

import studio.magemonkey.codex.data.users.IUserManager;
import studio.magemonkey.divinity.Divinity;
import studio.magemonkey.divinity.data.api.DivinityUser;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class UserManager extends IUserManager<Divinity, DivinityUser> {

    public UserManager(@NotNull Divinity plugin) {
        super(plugin);
    }

    @Override
    @NotNull
    protected DivinityUser createData(@NotNull Player player) {
        return new DivinityUser(this.plugin, player);
    }
}
