package com.promcteam.divinity.modules.list.identify.event;

import com.promcteam.codex.manager.api.event.ICancellableEvent;
import com.promcteam.divinity.modules.list.identify.IdentifyManager.IdentifyTome;
import com.promcteam.divinity.modules.list.identify.IdentifyManager.UnidentifiedItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PlayerIdentifyItemEvent extends ICancellableEvent {

    private UnidentifiedItem target;
    private IdentifyTome     tome;
    private ItemStack        result;
    private Player           player;

    public PlayerIdentifyItemEvent(
            @NotNull UnidentifiedItem target,
            @NotNull IdentifyTome tome,
            @NotNull ItemStack result,
            @NotNull Player player
    ) {
        this.target = target;
        this.tome = tome;
        this.result = result;
        this.player = player;
    }

    @NotNull
    public IdentifyTome getTome() {
        return this.tome;
    }

    @NotNull
    public UnidentifiedItem getTargetItem() {
        return this.target;
    }

    @NotNull
    public ItemStack getResult() {
        return this.result;
    }

    @NotNull
    public Player getPlayer() {
        return this.player;
    }
}
