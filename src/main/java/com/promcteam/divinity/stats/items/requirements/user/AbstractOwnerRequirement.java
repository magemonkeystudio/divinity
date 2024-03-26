package com.promcteam.divinity.stats.items.requirements.user;

import com.promcteam.codex.util.DataUT;
import com.promcteam.divinity.stats.items.ItemTags;
import com.promcteam.divinity.stats.items.requirements.api.DynamicUserRequirement;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class AbstractOwnerRequirement extends DynamicUserRequirement<UUID> {

    public static final UUID DUMMY_ID = UUID.fromString("00a0000a-0000-0000-0000-00aa0000a0a0");

    public AbstractOwnerRequirement(
            @NotNull String id,
            @NotNull String name,
            @NotNull String format,
            @NotNull String placeholder
    ) {
        super(
                id,
                name,
                format,
                placeholder,
                ItemTags.TAG_REQ_USER_OWNER,
                DataUT.UUID
        );

        // Legacy keys
        this.keys.add(NamespacedKey.fromString("prorpgitems:item_user_uuid" + this.getId()));
        this.keys.add(NamespacedKey.fromString("prorpgitems:qrpg_item_user_uuid" + this.getId()));
        this.keys.add(NamespacedKey.fromString("quantumrpg:qrpg_item_user_uuid" + this.getId()));
    }

    @Override
    @NotNull
    public Class<UUID> getParameterClass() {
        return UUID.class;
    }

    public boolean add(@NotNull ItemStack item, int line) {
        return this.add(item, DUMMY_ID, line);
    }

    public boolean isRequired(@NotNull ItemStack item) {
        UUID uuid = this.getRaw(item);
        return uuid != null && uuid.equals(DUMMY_ID);
    }
}
