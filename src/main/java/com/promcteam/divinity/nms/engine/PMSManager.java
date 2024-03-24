package com.promcteam.divinity.nms.engine;

import com.promcteam.divinity.Divinity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.promcteam.divinity.config.EngineCfg;
import com.promcteam.divinity.nms.packets.PacketManager;

public class PMSManager {

    private final Divinity plugin;
    private       PMS      nmsEngine;
    private       PacketManager packetManager;

    public PMSManager(@NotNull Divinity plugin) {
        this.plugin = plugin;
    }

    public void setup() {
        nmsEngine = new PMS() {
        };

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
