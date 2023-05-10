package su.nightexpress.quantumrpg.modules.list.party.compat.level;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.enums.ExpSource;
import com.sucy.skill.api.event.PlayerExperienceGainEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.modules.list.party.PartyManager;

public class IPESkillAPI extends IPartyLevelManager {

    public IPESkillAPI(@NotNull QuantumRPG plugin, @NotNull PartyManager partyManager) {
        super(plugin, partyManager);
    }

    @EventHandler
    public void onExpSkillapi(PlayerExperienceGainEvent e) {
        if (e.getSource() == ExpSource.PLUGIN) return;

        Player player = e.getPlayerData().getPlayer();
        int    exp    = this.getBalancedExp(player, (int) e.getExp());
        if (exp <= 0) return;

        e.setExp(exp);
    }

    @Override
    public void giveExp(@NotNull Player player, int amount) {
        SkillAPI.getPlayerAccountData(player).getActiveData().giveExp(amount, ExpSource.PLUGIN);
    }
}
