package com.promcteam.divinity.hooks.external;

import de.Keyle.MyPet.api.entity.MyPetBukkitEntity;
import com.promcteam.codex.hooks.HookState;
import com.promcteam.codex.hooks.NHook;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.promcteam.divinity.QuantumRPG;

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
        return ((MyPetBukkitEntity) entity).getMyPet().getOwner().getPlayer();
    }
}
