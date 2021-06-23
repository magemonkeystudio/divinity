package su.nightexpress.quantumrpg.hooks.external;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.Keyle.MyPet.api.entity.MyPetBukkitEntity;
import mc.promcteam.engine.hooks.HookState;
import mc.promcteam.engine.hooks.NHook;
import su.nightexpress.quantumrpg.QuantumRPG;

public class MyPetHK extends NHook<QuantumRPG> {

	public MyPetHK(@NotNull QuantumRPG plugin) {
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

	public boolean isPet(@NotNull Entity entity) {
		return entity instanceof MyPetBukkitEntity;
	}
	
	@Nullable
	public Player getPetOwner(@NotNull Entity entity) {
		return ((MyPetBukkitEntity)entity).getMyPet().getOwner().getPlayer();
	}
}
