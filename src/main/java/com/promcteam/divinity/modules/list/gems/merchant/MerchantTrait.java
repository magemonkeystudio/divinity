package com.promcteam.divinity.modules.list.gems.merchant;

import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import com.promcteam.divinity.api.QuantumAPI;
import com.promcteam.divinity.modules.api.socketing.merchant.MerchantSocket;
import com.promcteam.divinity.modules.list.gems.GemManager;

@TraitName("gems-merchant")
public class MerchantTrait extends Trait {

    public MerchantTrait() {
        super("gems-merchant");
    }

    @EventHandler
    public void click(NPCRightClickEvent e) {
        if (e.getNPC() == this.getNPC()) {
            GemManager gemManager = QuantumAPI.getModuleManager().getGemManager();
            if (gemManager == null) return;

            MerchantSocket merchant = gemManager.getMerchant();
            if (merchant != null) {
                Player player = e.getClicker();
                merchant.openMerchantGUI(player, false);
            }
        }
    }
}
