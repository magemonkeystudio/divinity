package com.promcteam.divinity.nms.packets;

import com.promcteam.codex.core.Version;
import com.promcteam.codex.nms.packets.IPacketHandler;
import com.promcteam.codex.util.Reflex;
import com.promcteam.divinity.Divinity;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class PacketManager {

    public static final Map<Player, Set<ChatColor>> COLOR_CACHE = new WeakHashMap<>();
    private final       Divinity                    plugin;
    private             IPacketHandler              packetHandler;

    public PacketManager(@NotNull Divinity plugin) {
        this.plugin = plugin;
    }

    public void setup() {
        if (Version.TEST.isCurrent()) return;
        String cur = Version.CURRENT.name().toUpperCase();
        try {
            String   pack  = this.getClass().getPackage().getName() + ".versions";
            Class<?> clazz = Reflex.getClass(pack, cur);
            if (clazz == null) return;

            packetHandler = (IPacketHandler) clazz.getConstructor(Divinity.class).newInstance(plugin);
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
