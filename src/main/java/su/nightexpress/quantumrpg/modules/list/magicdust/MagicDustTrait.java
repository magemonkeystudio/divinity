package su.nightexpress.quantumrpg.modules.list.magicdust;

import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import su.nightexpress.quantumrpg.api.QuantumAPI;

@TraitName("magicdust")
public class MagicDustTrait extends Trait {

    public MagicDustTrait() {
        super("magicdust");
    }

    @EventHandler
    public void click(NPCRightClickEvent e) {
        if (e.getNPC() == this.getNPC()) {
            Player p = e.getClicker();

            MagicDustManager magicDustManager = QuantumAPI.getModuleManager().getMagicDustManager();
            if (magicDustManager == null) return;

            magicDustManager.openGUIPaid(p, p.getInventory().getItemInMainHand(), false);
        }
    }
}
