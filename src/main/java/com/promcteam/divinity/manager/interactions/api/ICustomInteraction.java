package com.promcteam.divinity.manager.interactions.api;

import com.promcteam.divinity.Divinity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public abstract class ICustomInteraction {

    protected Divinity plugin;
    protected Player   player;

    public ICustomInteraction(@NotNull Divinity plugin) {
        this.plugin = plugin;
    }

    public final boolean act(@NotNull Player player) {
        this.player = player;
        if (!this.plugin.getInteractionManager().isInAction(player) && this.doAction()) {
            this.plugin.getInteractionManager().addInAction(player);
            return true;
        }
        return false;
    }

    protected abstract boolean doAction();

    protected final void endAction() {
        this.plugin.getInteractionManager().removeFromAction(this.player);
    }
}
