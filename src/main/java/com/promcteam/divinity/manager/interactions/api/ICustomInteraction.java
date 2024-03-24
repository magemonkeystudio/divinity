package com.promcteam.divinity.manager.interactions.api;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import com.promcteam.divinity.QuantumRPG;

public abstract class ICustomInteraction {

    protected QuantumRPG plugin;
    protected Player     player;

    public ICustomInteraction(@NotNull QuantumRPG plugin) {
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
