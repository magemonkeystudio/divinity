package com.promcteam.divinity.stats.items.requirements.api;

import com.promcteam.codex.config.api.ILangMsg;
import com.promcteam.divinity.stats.items.api.ItemLoreStat;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public abstract class ItemRequirement<Z> extends ItemLoreStat<Z> {

    public ItemRequirement(
            @NotNull String id,
            @NotNull String name,
            @NotNull String format,
            @NotNull String placeholder,
            @NotNull String uniqueTag,
            @NotNull PersistentDataType<?, Z> dataType
    ) {
        super(id, name, format, placeholder, uniqueTag, dataType);
    }

    public abstract boolean canApply(@NotNull ItemStack src, @NotNull ItemStack target);

    public abstract ILangMsg getApplyMessage(@NotNull ItemStack src, @NotNull ItemStack target);
}
