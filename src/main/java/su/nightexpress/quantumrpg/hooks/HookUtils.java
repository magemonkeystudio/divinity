package su.nightexpress.quantumrpg.hooks;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import su.nightexpress.quantumrpg.hooks.external.CitizensHook;
import su.nightexpress.quantumrpg.hooks.external.ResidenceHook;
import su.nightexpress.quantumrpg.hooks.external.VaultHook;
import su.nightexpress.quantumrpg.hooks.external.WorldGuardHook;

public class HookUtils {
    public static <T extends Hook> T getHook(Class<T> clazz) {
        byte b;
        int i;
        EHook[] arrayOfEHook;
        for (i = (arrayOfEHook = EHook.values()).length, b = 0; b < i; ) {
            EHook e = arrayOfEHook[b];
            Hook h = e.getHook();
            if (h != null && h.getClass() != null &&
                    clazz.isAssignableFrom(h.getClass()))
                return (T) h;
            b++;
        }
        return null;
    }

    public static String getGroup(Player p) {
        if (EHook.VAULT.isEnabled()) {
            VaultHook vh = (VaultHook) EHook.VAULT.getHook();
            return vh.getPlayerGroup(p).toLowerCase();
        }
        return "";
    }

    public static boolean canFights(Entity damager, Entity zertva) {
        if (damager.equals(zertva))
            return false;
        if (zertva.isInvulnerable())
            return false;
        if (EHook.WORLD_GUARD.isEnabled()) {
            WorldGuardHook wh = (WorldGuardHook) EHook.WORLD_GUARD.getHook();
            if (!wh.canFights(damager, zertva))
                return false;
        }
        if (EHook.RESIDENCE.isEnabled()) {
            ResidenceHook r = (ResidenceHook) EHook.RESIDENCE.getHook();
            if (!r.canFights(damager, zertva))
                return false;
        }
        if (EHook.CITIZENS.isEnabled()) {
            CitizensHook cz = (CitizensHook) EHook.CITIZENS.getHook();
            if (cz.isNPC(zertva))
                return false;
        }
        return true;
    }
}
