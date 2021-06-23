package su.nightexpress.quantumrpg.modules.api.socketing.merchant;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import mc.promcteam.engine.config.api.JYML;
import mc.promcteam.engine.manager.api.Loadable;
import mc.promcteam.engine.modules.IModuleExecutor;
import su.nightexpress.quantumrpg.Perms;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.modules.api.socketing.ModuleSocket;

public class MerchantSocket implements Loadable {

	private QuantumRPG plugin;
	private ModuleSocket<?> moduleSocket;
	private JYML cfg;
	
	private int socketChanceBonusAmount;
	private int socketChanceBonusMax;
	private boolean socketSilentRateEnabled;
	
	private double socketWorthModifier;
	private double itemWorthModifier;
	
	private MerchantGUI merchantGUI;
	
	public MerchantSocket(@NotNull ModuleSocket<?> moduleSocket) {
		this.plugin = moduleSocket.plugin;
		this.moduleSocket = moduleSocket;
	}

	@Override
	public void setup() {
		this.cfg = JYML.loadOrExtract(plugin, this.moduleSocket.getPath() + "merchant.yml");
		
		String path = "socketing.";
		this.socketChanceBonusAmount = cfg.getInt(path + "chance.merchant-bonus.amount", 15);
		this.socketChanceBonusMax = cfg.getInt(path + "chance.merchant-bonus.maximal", 80);
		this.socketSilentRateEnabled = cfg.getBoolean(path + "silent-rate-bonus.enabled", false);
		
		path = "price.";
		this.socketWorthModifier = cfg.getDouble(path + "socket-worth-modifier", 1.0);
		this.itemWorthModifier = cfg.getDouble(path + "item-worth-modifier", 0.6);
		
		this.merchantGUI = new MerchantGUI(this.moduleSocket, this);
		
		IModuleExecutor<QuantumRPG> exec = this.moduleSocket.getExecutor();
		if (exec != null) {
			exec.addSubCommand(new MerchantCmd(this.moduleSocket, this));
		}
	}

	@Override
	public void shutdown() {
		if (this.merchantGUI != null) {
			this.merchantGUI.shutdown();
			this.merchantGUI = null;
		}
		this.cfg = null;
	}
	
	public void openMerchantGUI(@NotNull Player player, boolean force) {
		if (!force && !player.hasPermission(Perms.getSocketGuiMerchant(this.moduleSocket))) {
			plugin.lang().Error_NoPerm.send(player);
			return;
		}
		this.getMerchantGUI().open(player, 1);
	}
	
	@NotNull
	public JYML getConfig() {
		return cfg;
	}
	
	@NotNull
	public MerchantGUI getMerchantGUI() {
		return merchantGUI;
	}
	
	public int getSocketChanceBonusAmount() {
		return socketChanceBonusAmount;
	}
	
	public int getSocketChanceBonusMax() {
		return socketChanceBonusMax;
	}
	
	public boolean isSocketSilentRateEnabled() {
		return socketSilentRateEnabled;
	}
	
	public double getSocketWorthModifier() {
		return socketWorthModifier;
	}
	
	public double getItemWorthModifier() {
		return itemWorthModifier;
	}
}
