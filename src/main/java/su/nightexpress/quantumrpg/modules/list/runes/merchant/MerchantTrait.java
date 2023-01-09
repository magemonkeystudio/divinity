package su.nightexpress.quantumrpg.modules.list.runes.merchant;

import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import su.nightexpress.quantumrpg.api.QuantumAPI;
import su.nightexpress.quantumrpg.modules.api.socketing.merchant.MerchantSocket;
import su.nightexpress.quantumrpg.modules.list.runes.RuneManager;

@TraitName("runes-merchant")
public class MerchantTrait extends Trait {

    public MerchantTrait() {
        super("runes-merchant");
    }

    @EventHandler
    public void click(NPCRightClickEvent e) {
        if (e.getNPC() == this.getNPC()) {
            Player      p           = e.getClicker();
            RuneManager runeManager = QuantumAPI.getModuleManager().getRuneManager();
            if (runeManager == null) return;

            MerchantSocket merchant = runeManager.getMerchant();
            if (merchant != null) {
                merchant.openMerchantGUI(p, false);
            }
        }
    }
}