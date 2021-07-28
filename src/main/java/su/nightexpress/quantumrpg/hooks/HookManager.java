package su.nightexpress.quantumrpg.hooks;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import su.nightexpress.quantumrpg.QuantumRPG;

public class HookManager {
    private QuantumRPG plugin;

    public HookManager(QuantumRPG plugin) {
        this.plugin = plugin;
    }

    public void setup() {
        PluginManager pm = this.plugin.getPluginManager();
        byte b;
        int i;
        EHook[] arrayOfEHook;
        for (i = (arrayOfEHook = EHook.values()).length, b = 0; b < i; ) {
            EHook h = arrayOfEHook[b];
            Plugin p = pm.getPlugin(h.getPluginName());
            if ((p != null && p.isEnabled()) || h == EHook.NONE)
                h.enable();
            b++;
        }
        sendStatus();
    }

    private void sendStatus() {
        this.plugin.getServer().getConsoleSender().sendMessage("§3---------[ §bHooks Initializing §3]---------");
        byte b;
        int i;
        EHook[] arrayOfEHook;
        for (i = (arrayOfEHook = EHook.values()).length, b = 0; b < i; ) {
            EHook h = arrayOfEHook[b];
            if (h != EHook.NONE)
                this.plugin.getServer().getConsoleSender().sendMessage("§7> §f" + h.getPluginName() + ": " + getColorStatus(h.isEnabled()));
            b++;
        }
    }

    private String getColorStatus(boolean b) {
        if (b)
            return "§aSuccess!";
        return "§cNot found / Error.";
    }

    public void disable() {
        byte b;
        int i;
        EHook[] arrayOfEHook;
        for (i = (arrayOfEHook = EHook.values()).length, b = 0; b < i; ) {
            EHook h = arrayOfEHook[b];
            if (h.isEnabled())
                h.disable();
            b++;
        }
    }
}
