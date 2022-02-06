package su.nightexpress.quantumrpg.modules.list.money;

import mc.promcteam.engine.commands.list.HelpCommand;
import org.jetbrains.annotations.NotNull;

import mc.promcteam.engine.config.api.JYML;
import mc.promcteam.engine.hooks.external.VaultHK;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.modules.EModule;
import su.nightexpress.quantumrpg.modules.ModuleItem;
import su.nightexpress.quantumrpg.modules.api.QModuleDrop;
import su.nightexpress.quantumrpg.modules.command.MReloadCmd;

public class MoneyManager extends QModuleDrop<MoneyManager.QMoney> {
	
	public MoneyManager(@NotNull QuantumRPG plugin) {
		super(plugin, QMoney.class);
	}
	
	@Override
	public String getId() {
		return EModule.MONEY;
	}

	@Override
	public String version() {
		return "1.0.0";
	}
	
	@Override
	public void setup() {
		
		VaultHK vault = plugin.getVault();
		
		if (vault == null || vault.getEconomy() == null) {
			this.error("Economy not found! Module will be disabled.");
			this.interruptLoad();
			return;
		}
	}

	@Override
	public void shutdown(){

	}

	@Override
	public void onPostSetup(){

		this.loadSettings();
		this.loadItems();

		if (this.moduleCommand != null) {
			this.moduleCommand.addDefaultCommand(new HelpCommand<>(this.plugin));
			this.moduleCommand.addSubCommand(new MReloadCmd(this));
		}
	}

	
	public class QMoney extends ModuleItem{

		private static final String MONEY = "money-amount";
		double amount;
		
		public QMoney(@NotNull QuantumRPG plugin, @NotNull JYML cfg) {
			super(plugin, cfg, MoneyManager.this);
			amount = cfg.getDouble(MONEY, 0.0);
		}

		public double getAmount() {
			return amount;
		}
	
	}
	
}
