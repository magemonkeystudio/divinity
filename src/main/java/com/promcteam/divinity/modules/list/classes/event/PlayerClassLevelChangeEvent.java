package com.promcteam.divinity.modules.list.classes.event;

import com.promcteam.divinity.modules.list.classes.api.UserClassData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlayerClassLevelChangeEvent extends PlayerClassEvent {

    private int lvlNew;

    public PlayerClassLevelChangeEvent(
            @NotNull Player player,
            @NotNull UserClassData data,
            int lvlNew
    ) {
        super(player, data);
        this.lvlNew = lvlNew;
    }

    public int getNewLevel() {
        return this.lvlNew;
    }
}
