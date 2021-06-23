package su.nightexpress.quantumrpg.nms.packets;

import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import mc.promcteam.engine.core.Version;
import mc.promcteam.engine.nms.packets.IPacketHandler;
import mc.promcteam.engine.utils.Reflex;
import su.nightexpress.quantumrpg.QuantumRPG;

public class PacketManager {

	private QuantumRPG plugin;
	private IPacketHandler packetHandler;
	
	public static final Map<Player, Set<ChatColor>> COLOR_CACHE = new WeakHashMap<>();
	
	public PacketManager(@NotNull QuantumRPG plugin) {
		this.plugin = plugin;
	}
	
	public void setup() {
    	String cur = Version.CURRENT.name().toUpperCase();
    	try {
    		String pack = this.getClass().getPackage().getName() + ".versions";
    		Class<?> clazz = Reflex.getClass(pack, cur);
    		if (clazz == null) return;
    		
			packetHandler = (IPacketHandler) clazz.getConstructor(QuantumRPG.class).newInstance(plugin);
			this.plugin.getPacketManager().registerHandler(this.packetHandler);
    	} 
    	catch (Exception e) {
    		this.plugin.error("Could not register PacketHandler!");
    		e.printStackTrace();
		}
	}
	
	public void shutdown() {
		if (this.packetHandler != null) {
			this.plugin.getPacketManager().unregisterHandler(this.packetHandler);
			this.packetHandler = null;
		}
	}
}
