package studio.magemonkey.divinity.modules.list.drops.object;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface DropCalculator {

    Set<Drop> dropCalculator(
            @Nullable Player killer,
            @NotNull LivingEntity npc,
            float dropModifier);
}
