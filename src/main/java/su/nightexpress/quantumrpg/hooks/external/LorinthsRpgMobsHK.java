package su.nightexpress.quantumrpg.hooks.external;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import me.lorinth.rpgmobs.LorinthsRpgMobs;
import su.nexmedia.engine.hooks.HookState;
import su.nexmedia.engine.hooks.NHook;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.hooks.HookMobLevel;

public class LorinthsRpgMobsHK extends NHook<QuantumRPG> implements HookMobLevel {

	public LorinthsRpgMobsHK(@NotNull QuantumRPG plugin) {
		super(plugin);
	}

	@Override
	@NotNull
	protected HookState setup() {
		return HookState.SUCCESS;
	}

	@Override
	protected void shutdown() {
		
	}

	@Override
	public double getMobLevel(@NotNull Entity entity) {
		return LorinthsRpgMobs.GetLevelOfEntity(entity).intValue();
	}

}
