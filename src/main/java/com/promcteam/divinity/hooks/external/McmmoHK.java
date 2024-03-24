package com.promcteam.divinity.hooks.external;

import com.gmail.nossr50.api.ExperienceAPI;
import com.promcteam.codex.hooks.HookState;
import com.promcteam.codex.hooks.NHook;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import com.promcteam.divinity.Divinity;
import com.promcteam.divinity.hooks.HookLevel;

public class McmmoHK extends NHook<Divinity> implements HookLevel {

    public McmmoHK(@NotNull Divinity plugin) {
        super(plugin);
    }

    @Override
    @NotNull
    public HookState setup() {
        return HookState.SUCCESS;
    }

    @Override
    public void shutdown() {

    }

    @Override
    public int getLevel(@NotNull Player player) {
        return ExperienceAPI.getPowerLevel(player);
    }

    @Override
    public void giveExp(@NotNull Player player, int amount) {
        // Don't really know which skill would be right here since exp can come from several sources,
        // will leave empty unless this is brought up again
    }
}
