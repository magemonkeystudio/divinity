package su.nightexpress.quantumrpg.utils.actions.params;

import mc.promcteam.engine.utils.actions.params.IAutoValidated;
import mc.promcteam.engine.utils.actions.params.IParamValue;
import mc.promcteam.engine.utils.actions.params.defaults.IParamBoolean;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.api.PartyAPI;
import su.nightexpress.quantumrpg.modules.list.party.PartyManager;
import su.nightexpress.quantumrpg.modules.list.party.PartyManager.Party;

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
