package com.promcteam.divinity.modules.list.party.compat.level;

import com.promcteam.divinity.Divinity;
import com.promcteam.divinity.api.DivinityAPI;
import com.promcteam.divinity.modules.list.classes.ClassManager;
import com.promcteam.divinity.modules.list.classes.event.PlayerClassExpGainEvent;
import com.promcteam.divinity.modules.list.classes.object.ExpSource;
import com.promcteam.divinity.modules.list.party.PartyManager;
import com.promcteam.divinity.modules.list.party.PartyManager.Party;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;

public class IPEInternal extends IPartyLevelManager {

    public IPEInternal(@NotNull Divinity plugin, @NotNull PartyManager partyManager) {
        super(plugin, partyManager);
    }

    @EventHandler
    public void onExpGain(PlayerClassExpGainEvent e) {
        if (e.getExpSource() != ExpSource.MOB_KILL) return;

        Player player = e.getPlayer();
        int    exp    = this.getBalancedExp(player, e.getExp());
        if (exp <= 0) return;

        e.setExp(exp);
    }

    @Override
    public void giveExp(@NotNull Player player, int amount) {
        Party party = this.partyManager.getPlayerParty(player);
        if (party == null) return;

        ClassManager classManager = DivinityAPI.getModuleManager().getClassManager();
        if (classManager == null) return;

        classManager.getLevelingManager().addExp(player, amount, party.getId(), ExpSource.INTERNAL);
    }

}
