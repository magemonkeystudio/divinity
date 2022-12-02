package su.nightexpress.quantumrpg.modules.list.dismantle;

import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import su.nightexpress.quantumrpg.api.QuantumAPI;

@TraitName("dismantle")
public class DismantleTrait extends Trait {

    public DismantleTrait() {
        super("dismantle");
    }

    @EventHandler
    public void click(NPCRightClickEvent e) {
        if (e.getNPC() == this.getNPC()) {
            Player           player           = e.getClicker();
            DismantleManager dismantleManager = QuantumAPI.getModuleManager().getResolveManager();
            if (dismantleManager == null) return;

            dismantleManager.openDismantleGUI(player, false);
        }
    }
}
