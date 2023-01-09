package su.nightexpress.quantumrpg.modules.list.sell;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;

import net.citizensnpcs.api.trait.TraitInfo;
import mc.promcteam.engine.hooks.external.VaultHK;
import mc.promcteam.engine.hooks.external.citizens.CitizensHK;
import mc.promcteam.engine.utils.NumberUT;
import mc.promcteam.engine.utils.actions.ActionManipulator;
import su.nightexpress.quantumrpg.Perms;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.modules.EModule;
import su.nightexpress.quantumrpg.modules.api.QModule;
import su.nightexpress.quantumrpg.modules.list.sell.command.SellOpenCmd;
import su.nightexpress.quantumrpg.modules.list.sell.event.PlayerSellItemEvent;

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
		double cost = e.getPrice();
		Player player = e.getPlayer();
		plugin.lang().Sell_Sell_Complete.replace("%cost%", NumberUT.format(cost)).send(player);
		this.actionsComplete.process(player);
	}
}
