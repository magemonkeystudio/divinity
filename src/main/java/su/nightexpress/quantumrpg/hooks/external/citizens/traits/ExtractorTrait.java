package su.nightexpress.quantumrpg.hooks.external.citizens.traits;

import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.modules.EModule;
import su.nightexpress.quantumrpg.modules.extractor.ExtractorManager;

@TraitName("extractor")
public class ExtractorTrait extends Trait {
    public ExtractorTrait() {
        super("extractor");
    }

    @EventHandler
    public void click(NPCRightClickEvent e) {
        if (e.getNPC() == getNPC()) {
            QuantumRPG pl = QuantumRPG.instance;
            Player p = e.getClicker();
            ExtractorManager m = (ExtractorManager) pl.getMM().getModule(EModule.EXTRACTOR);
            m.openExtractGUI(p, p.getInventory().getItemInMainHand(), null, null, -1);
        }
    }
}
