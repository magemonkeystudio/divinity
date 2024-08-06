package studio.magemonkey.divinity.stats.items.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface DynamicStat<Z> {

    @NotNull
    ItemStack updateItem(@Nullable Player p, @NotNull ItemStack item);

    @NotNull
    String getFormat(@Nullable Player p, @NotNull ItemStack item, @NotNull Z value);
}
