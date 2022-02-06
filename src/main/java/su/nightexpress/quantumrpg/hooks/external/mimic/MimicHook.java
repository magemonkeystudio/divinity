package su.nightexpress.quantumrpg.hooks.external.mimic;

import org.bukkit.Bukkit;
import ru.endlesscode.mimic.Mimic;
import ru.endlesscode.mimic.MimicApiLevel;
import su.nightexpress.quantumrpg.QuantumRPG;

public class MimicHook {

    public static void hook(QuantumRPG quantumRpg) {
        if (Bukkit.getPluginManager().getPlugin("Mimic") == null) return;
        if (!MimicApiLevel.checkApiLevel(MimicApiLevel.VERSION_0_7)) {
            quantumRpg.getLogger().severe(
                    "At least Mimic 0.7 is required. " +
                            "Please download it from https://www.spigotmc.org/resources/82515/"
            );
            return;
        }

        try {
            registerMimicServices(quantumRpg);
        } catch (Exception ex) {
            quantumRpg.getLogger().severe("Mimic hook failed: " + ex.getLocalizedMessage());
        }
    }

    private static void registerMimicServices(QuantumRPG quantumRpg) {
        Mimic mimic = Mimic.getInstance();
        mimic.registerLevelSystem(ProRpgItemsLevelSystem::new, MimicApiLevel.CURRENT, quantumRpg);
        mimic.registerClassSystem(ProRpgItemsClassSystem::new, MimicApiLevel.CURRENT, quantumRpg);
        mimic.registerItemsRegistry(new ProRpgItemsItemsRegistry(quantumRpg), MimicApiLevel.CURRENT, quantumRpg);
    }
}
