package su.nightexpress.quantumrpg.hooks;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface HookClass {

	@NotNull String getClass(@NotNull Player player);
	
	void takeMana(@NotNull Player player, double amount, boolean ofMax);
}
