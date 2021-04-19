package su.nightexpress.quantumrpg.hooks.external;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import com.gmail.nossr50.api.ExperienceAPI;

import su.nexmedia.engine.hooks.HookState;
import su.nexmedia.engine.hooks.NHook;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.hooks.HookLevel;

public class McmmoHK extends NHook<QuantumRPG> implements HookLevel {

	public McmmoHK(@NotNull QuantumRPG plugin) {
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
}
