package su.nightexpress.quantumrpg.hooks.external.citizens.traits;

import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.modules.EModule;
import su.nightexpress.quantumrpg.modules.resolve.ResolveManager;

@TraitName("resolver")
public class ResolveTrait extends Trait {
    public ResolveTrait() {
        super("resolver");
    }

    @EventHandler
    public void click(NPCRightClickEvent e) {
        if (e.getNPC() == getNPC()) {
            QuantumRPG pl = QuantumRPG.instance;
            Player p = e.getClicker();
            ResolveManager m = (ResolveManager) pl.getMM().getModule(EModule.RESOLVE);
            m.openResolveGUI(p, p.getInventory().getItemInMainHand(), null);
        }
    }
}
