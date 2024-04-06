package studio.magemonkey.divinity.stats.items.attributes.api;

import org.jetbrains.annotations.NotNull;

public interface Emptible<Z> {

    boolean isEmpty(@NotNull Z value);

    public @NotNull Z getDefaultValue();
}
