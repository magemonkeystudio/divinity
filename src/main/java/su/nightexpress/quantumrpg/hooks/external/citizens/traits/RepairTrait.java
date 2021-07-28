package su.nightexpress.quantumrpg.hooks.external.citizens.traits;

import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.modules.EModule;
import su.nightexpress.quantumrpg.modules.repair.RepairManager;

@TraitName("repair")
public class RepairTrait extends Trait {
    public RepairTrait() {
        super("repair");
    }

    @EventHandler
    public void click(NPCRightClickEvent e) {
        if (e.getNPC() == getNPC()) {
            QuantumRPG pl = QuantumRPG.instance;
            Player p = e.getClicker();
            RepairManager m = (RepairManager) pl.getMM().getModule(EModule.REPAIR);
            m.openRepairGUI(p, p.getInventory().getItemInMainHand(), null, null);
        }
    }
}
