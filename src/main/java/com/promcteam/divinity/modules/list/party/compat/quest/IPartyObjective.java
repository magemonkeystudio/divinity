package com.promcteam.divinity.modules.list.party.compat.quest;

import com.promcteam.codex.manager.IListener;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;
import com.promcteam.divinity.QuantumRPG;
import com.promcteam.divinity.modules.list.party.PartyManager;
import com.promcteam.divinity.modules.list.party.PartyManager.Party;
import com.promcteam.divinity.modules.list.party.PartyManager.PartyMember;

import java.util.Set;

public abstract class IPartyObjective extends IListener<QuantumRPG> {

    protected PartyManager partyManager;

    public IPartyObjective(@NotNull QuantumRPG plugin, @NotNull PartyManager partyManager) {
        super(plugin);
        this.partyManager = partyManager;
    }

    public void setup() {
        this.registerListeners();
    }

    public void shutdown() {
        this.partyManager = null;
        this.unregisterListeners();
    }

    @EventHandler
    public void onMobKillObjective(EntityDeathEvent e) {
        if (!this.partyManager.getSettings().isQuestMobKillEnabled()) return;

        LivingEntity entity = e.getEntity();
        Player       killer = entity.getKiller();
        if (killer == null) return;

        PartyMember member = partyManager.getPartyMember(killer);
        if (member == null) return;

        Party            party        = member.getParty();
        int              killDistance = partyManager.getSettings().getMaxQuestMobKillDistance();
        Set<PartyMember> objGeters    = party.getMembersByDistance(member, killDistance);

        this.progressObjective(killer, objGeters, e);
    }

    public abstract void progressObjective(@NotNull Player exec, @NotNull Set<PartyMember> objGeters, @NotNull Event e);
}
