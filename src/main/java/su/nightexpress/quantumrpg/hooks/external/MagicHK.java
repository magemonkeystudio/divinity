package su.nightexpress.quantumrpg.hooks.external;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import com.elmakers.mine.bukkit.api.magic.MagicAPI;

import mc.promcteam.engine.hooks.HookState;
import mc.promcteam.engine.hooks.NHook;
import su.nightexpress.quantumrpg.QuantumRPG;

public class MagicHK extends NHook<QuantumRPG> {

	private MagicAPI api;
	
	public MagicHK(@NotNull QuantumRPG plugin) {
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
