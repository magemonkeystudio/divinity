package su.nightexpress.quantumrpg.nms.packets;

import mc.promcteam.engine.core.Version;
import mc.promcteam.engine.nms.packets.IPacketHandler;
import mc.promcteam.engine.utils.Reflex;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.QuantumRPG;

import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class PacketManager {

    public static final Map<Player, Set<ChatColor>> COLOR_CACHE = new WeakHashMap<>();
    private             QuantumRPG                  plugin;
    private             IPacketHandler              packetHandler;

    public PacketManager(@NotNull QuantumRPG plugin) {
        this.plugin = plugin;
    }

    public void setup() {
        if (Version.TEST.isCurrent()) return;
        String cur = Version.CURRENT.name().toUpperCase();
        try {
            String   pack  = this.getClass().getPackage().getName() + ".versions";
            Class<?> clazz = Reflex.getClass(pack, cur);
            if (clazz == null) return;

            packetHandler = (IPacketHandler) clazz.getConstructor(QuantumRPG.class).newInstance(plugin);
            this.plugin.getPacketManager().registerHandler(this.packetHandler);
        } catch (Exception e) {
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
