package com.promcteam.divinity.utils.actions.params;

import com.promcteam.codex.util.actions.params.IAutoValidated;
import com.promcteam.codex.util.actions.params.IParamValue;
import com.promcteam.codex.util.actions.params.defaults.IParamBoolean;
import com.promcteam.divinity.api.PartyAPI;
import com.promcteam.divinity.modules.list.party.PartyManager;
import com.promcteam.divinity.modules.list.party.PartyManager.Party;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class PartyMemberParam extends IParamBoolean implements IAutoValidated {

    public PartyMemberParam() {
        super("PARTY_MEMBER", "party-member");
    }

    @Override
    public void autoValidate(@NotNull Entity exe, @NotNull Set<Entity> targets, @NotNull IParamValue val) {
        if (!(exe instanceof Player)) return;

        PartyManager manager = PartyAPI.getPartyManager();
        if (manager == null) return;

        Player executor = (Player) exe;
        Party  party    = manager.getPlayerParty(executor);
        if (party == null) return;

        boolean b = val.getBoolean();

        for (Entity e : new HashSet<>(targets)) {
            if (e.getType() != EntityType.PLAYER) continue;
            Player  p      = (Player) e;
            boolean member = party.isMember(p);
            if (member != b) {
                targets.remove(e);
            }
        }
    }
}
