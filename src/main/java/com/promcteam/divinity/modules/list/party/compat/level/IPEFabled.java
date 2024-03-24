package com.promcteam.divinity.modules.list.party.compat.level;

import com.promcteam.fabled.Fabled;
import com.promcteam.fabled.api.enums.ExpSource;
import com.promcteam.fabled.api.event.PlayerExperienceGainEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;
import com.promcteam.divinity.Divinity;
import com.promcteam.divinity.modules.list.party.PartyManager;

public class IPEFabled extends IPartyLevelManager {

    public IPEFabled(@NotNull Divinity plugin, @NotNull PartyManager partyManager) {
        super(plugin, partyManager);
    }

    @EventHandler
    public void onExpFabled(PlayerExperienceGainEvent e) {
        if (e.getSource() == ExpSource.PLUGIN) return;

        Player player = e.getPlayerData().getPlayer();
        int    exp    = this.getBalancedExp(player, (int) e.getExp());
        if (exp <= 0) return;

        e.setExp(exp);
    }

    @Override
    public void giveExp(@NotNull Player player, int amount) {
        Fabled.getPlayerAccountData(player).getActiveData().giveExp(amount, ExpSource.PLUGIN);
    }
}
