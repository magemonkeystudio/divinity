package studio.magemonkey.divinity.hooks;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

public interface HookMobLevel {

    double getMobLevel(@NotNull Entity entity);
}
