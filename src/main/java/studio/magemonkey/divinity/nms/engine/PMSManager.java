package studio.magemonkey.divinity.nms.engine;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import studio.magemonkey.divinity.Divinity;
import studio.magemonkey.divinity.config.EngineCfg;
import studio.magemonkey.divinity.nms.packets.PacketManager;

public class PMSManager {
    private final Divinity      plugin;
    @Getter
    private       PacketManager packetManager;

    public PMSManager(@NotNull Divinity plugin) {
        this.plugin = plugin;
    }

    public void setup() {
        if (!EngineCfg.PACKETS_ENABLED) return;

        this.plugin.info("Packets are enabled. Setup packet manager...");
        this.packetManager = new PacketManager(this.plugin);
        this.packetManager.setup();
    }

    public void shutdown() {
        if (this.packetManager == null) return;

        this.packetManager.shutdown();
        this.packetManager = null;
    }
}
