package com.promcteam.divinity.modules.list.classes.event;

import com.promcteam.codex.manager.api.event.IEvent;
import com.promcteam.divinity.modules.list.classes.api.UserClassData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public abstract class PlayerClassEvent extends IEvent {

    private Player        player;
    private UserClassData classData;

    public PlayerClassEvent(@NotNull Player player, @NotNull UserClassData classData) {
        this.player = player;
        this.classData = classData;
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

    @NotNull
    public UserClassData getClassData() {
        return classData;
    }
}
