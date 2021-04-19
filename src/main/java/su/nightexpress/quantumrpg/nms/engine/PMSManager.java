package su.nightexpress.quantumrpg.nms.engine;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nexmedia.engine.core.Version;
import su.nexmedia.engine.utils.Reflex;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.config.EngineCfg;
import su.nightexpress.quantumrpg.nms.packets.PacketManager;

public class PMSManager {

	private QuantumRPG plugin;
	private PMS nmsEngine;
	private PacketManager packetManager;
	
	public PMSManager(@NotNull QuantumRPG plugin) {
		this.plugin = plugin;
	}
	
	public void setup() {
    	String cur = Version.CURRENT.name().toUpperCase();
    	try {
    		String pack = this.getClass().getPackage().getName() + ".versions";
    		Class<?> clazz = Reflex.getClass(pack, cur);
    		if (clazz == null) return;
    		
			this.nmsEngine = (PMS) clazz.getConstructor().newInstance();
		} 
    	catch (Exception e) {
    		plugin.error("Could not register Internal NMS! Plugin will be disabled.");
			e.printStackTrace();
			return;
		}
    	
    	if (EngineCfg.PACKETS_ENABLED) {
    		this.plugin.info("Packets are enabled. Setup packet manager...");
	    	this.packetManager = new PacketManager(this.plugin);
	    	this.packetManager.setup();
    	}
	}
	
	public void shutdown() {
		this.nmsEngine = null;
		if (this.packetManager != null) {
			this.packetManager.shutdown();
			this.packetManager = null;
		}
	}
	
	public PMS get() {
		return this.nmsEngine;
	}
	
	@Nullable
	public PacketManager getPacketManager() {
		return this.packetManager;
	}
}
