package com.promcteam.divinity.modules.list.sell;

import com.promcteam.codex.hooks.external.VaultHK;
import com.promcteam.codex.hooks.external.citizens.CitizensHK;
import com.promcteam.codex.utils.NumberUT;
import com.promcteam.codex.utils.actions.ActionManipulator;
import net.citizensnpcs.api.trait.TraitInfo;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;
import com.promcteam.divinity.Perms;
import com.promcteam.divinity.QuantumRPG;
import com.promcteam.divinity.modules.EModule;
import com.promcteam.divinity.modules.api.QModule;
import com.promcteam.divinity.modules.list.sell.command.SellOpenCmd;
import com.promcteam.divinity.modules.list.sell.event.PlayerSellItemEvent;

public class SellManager extends QModule {

    private ActionManipulator actionsComplete;
    ActionManipulator actionsError;

    private SellGUI gui;

    public SellManager(@NotNull QuantumRPG plugin) {
        super(plugin);
    }

    @Override
    @NotNull
    public String getId() {
        return EModule.SELL;
    }

    @Override
    @NotNull
    public String version() {
        return "1.6.0";
    }

    @Override
    public void setup() {
        VaultHK vault = plugin.getVault();
        if (vault == null || vault.getEconomy() == null) {
            this.error("Economy not found! Module will be disabled.");
            this.interruptLoad();
            return;
        }

        this.actionsComplete = new ActionManipulator(plugin, cfg, "general.actions-complete");
        this.actionsError = new ActionManipulator(plugin, cfg, "general.actions-error");

        this.gui = new SellGUI(this);
        this.moduleCommand.addSubCommand(new SellOpenCmd(this));

        CitizensHK citi = plugin.getCitizens();
        if (citi != null) {
            TraitInfo trait = TraitInfo.create(SellTrait.class).withName("sell");
            citi.registerTrait(plugin, trait);
        }

        this.cfg.saveChanges();
    }

    @Override
    public void shutdown() {
        if (this.gui != null) {
            this.gui.shutdown();
            this.gui = null;
        }
        this.actionsComplete = null;
        this.actionsError = null;
    }

    public void openSellGUI(@NotNull Player player, boolean isForce) {
        if (!isForce && !player.hasPermission(Perms.SELL_GUI)) {
            plugin.lang().Error_NoPerm.send(player);
            return;
        }
        this.gui.open(player, 1);
    }

    // ------------------------------------------------------

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSellMain(PlayerSellItemEvent e) {
        double cost   = e.getPrice();
        Player player = e.getPlayer();
        plugin.lang().Sell_Sell_Complete.replace("%cost%", NumberUT.format(cost)).send(player);
        this.actionsComplete.process(player);
    }
}
