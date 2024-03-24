package com.promcteam.divinity.hooks;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface HookLevel {

    int getLevel(@NotNull Player player);

    void giveExp(@NotNull Player player, int amount);
}
