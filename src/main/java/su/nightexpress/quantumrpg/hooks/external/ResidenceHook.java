package su.nightexpress.quantumrpg.hooks.external;

import com.bekvon.bukkit.residence.api.ResidenceApi;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.entity.Entity;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.hooks.EHook;
import su.nightexpress.quantumrpg.hooks.Hook;

public class ResidenceHook extends Hook {
    public ResidenceHook(EHook type, QuantumRPG plugin) {
        super(type, plugin);
    }

    public void setup() {
    }

    public void shutdown() {
    }

    public boolean canFights(Entity e1, Entity e2) {
        ClaimedResidence cr = ResidenceApi.getResidenceManager().getByLoc(e1.getLocation());
        if (cr != null &&
                cr.getPermissions().getFlags().containsKey("pvp"))
            return ((Boolean) cr.getPermissions().getFlags().get("pvp")).booleanValue();
        return true;
    }
}
