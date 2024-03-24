package com.promcteam.divinity.hooks.external;

import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.promcteam.codex.hooks.HookState;
import com.promcteam.codex.hooks.NHook;
import com.promcteam.divinity.Divinity;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class MagicHK extends NHook<Divinity> {

    private MagicAPI api;

    public MagicHK(@NotNull Divinity plugin) {
        super(plugin);
    }

    @Override
    @NotNull
    protected HookState setup() {
        Plugin magicPlugin = plugin.getPluginManager().getPlugin(this.getPlugin());
        if (magicPlugin == null || !(magicPlugin instanceof MagicAPI)) {
            return HookState.ERROR;
        }
        this.api = (MagicAPI) magicPlugin;
        return HookState.SUCCESS;
    }

    @Override
    protected void shutdown() {

    }

    @NotNull
    public MagicAPI getAPI() {
        return this.api;
    }
}
