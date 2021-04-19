package su.nightexpress.quantumrpg.modules.list.drops.object;

import java.util.Set;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface DropCalculator {
	 
	int dropCalculator(
			@NotNull Player killer, 
			@NotNull LivingEntity npc, 
			@NotNull Set<Drop> result, 
			int index, 
			float dropModifier);
}
