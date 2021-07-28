package su.nightexpress.quantumrpg.hooks.external;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.RegisteredServiceProvider;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.hooks.EHook;
import su.nightexpress.quantumrpg.hooks.Hook;
import su.nightexpress.quantumrpg.hooks.HookClass;

public class VaultHook extends Hook implements HookClass {
    private Economy economy;

    private Permission perm;

    public VaultHook(EHook type, QuantumRPG plugin) {
        super(type, plugin);
    }

    public void setup() {
        setupEconomy();
        setupPerms();
    }

    public void shutdown() {
        this.economy = null;
    }

    public String getClass(Player p) {
        String klass = "";
        for (PermissionAttachmentInfo pio : p.getEffectivePermissions()) {
            String perm = pio.getPermission();
            if (perm.startsWith("qrpg.class")) {
                String ending = perm.substring(perm.lastIndexOf("."), perm.length());
                klass = ending.replace(".", "");
            }
        }
        return klass;
    }

    public double getBalans(Player p) {
        double r = this.economy.getBalance((OfflinePlayer) p);
        return r;
    }

    public boolean give(Player p, double amount) {
        EconomyResponse r = this.economy.depositPlayer((OfflinePlayer) p, amount);
        return r.transactionSuccess();
    }

    public boolean take(Player p, double amount) {
        EconomyResponse r = this.economy.withdrawPlayer((OfflinePlayer) p, amount);
        return r.transactionSuccess();
    }

    private void setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = ((QuantumRPG) this.plugin).getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null)
            return;
        this.economy = (Economy) rsp.getProvider();
    }

    private void setupPerms() {
        RegisteredServiceProvider<Permission> pp = ((QuantumRPG) this.plugin).getServer().getServicesManager().getRegistration(Permission.class);
        if (pp == null)
            return;
        this.perm = (Permission) pp.getProvider();
    }

    public String getPlayerGroup(Player p) {
        if (this.perm == null)
            return "";
        if (!this.perm.hasGroupSupport())
            return "";
        return this.perm.getPrimaryGroup(p);
    }
}
