package su.nightexpress.quantumrpg.hooks;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface HookLevel {

    int getLevel(@NotNull Player player);
}
