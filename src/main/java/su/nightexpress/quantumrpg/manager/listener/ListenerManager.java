package su.nightexpress.quantumrpg.manager.listener;

import mc.promcteam.engine.manager.api.Loadable;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.hooks.HookListener;
import su.nightexpress.quantumrpg.manager.listener.object.*;
import su.nightexpress.quantumrpg.stats.items.ItemStats;
import su.nightexpress.quantumrpg.stats.items.attributes.stats.DurabilityStat;
import su.nightexpress.quantumrpg.stats.items.requirements.ItemRequirements;

public class ListenerManager implements Loadable {

    private final QuantumRPG              plugin;
    private       ItemDurabilityListener  lisDurability;
    private       ItemHandListener        lisHand;
    private       ItemRequirementListener lisReq;
    private       ItemUpdaterListener     updater;
    private       VanillaWrapperListener  lisQuantum;
    private       HookListener            hookListener;

    public ListenerManager(@NotNull QuantumRPG plugin) {
        this.plugin = plugin;
    }

    @Override
    public void setup() {
        DurabilityStat duraStat = ItemStats.getStat(DurabilityStat.class);
        if (duraStat != null) {
            this.lisDurability = new ItemDurabilityListener(this.plugin, duraStat);
            this.lisDurability.registerListeners();
        }

        if (!ItemStats.getHands().isEmpty()) {
            this.lisHand = new ItemHandListener(this.plugin);
            this.lisHand.registerListeners();
        }

        if (!ItemRequirements.getUserRequirements().isEmpty()) {
            this.lisReq = new ItemRequirementListener(this.plugin);
            this.lisReq.registerListeners();
        }

        this.lisQuantum = new VanillaWrapperListener(this.plugin);
        this.lisQuantum.registerListeners();

        this.updater = new ItemUpdaterListener(this.plugin);
        this.updater.registerListeners();

        this.hookListener = new HookListener(this.plugin);
        this.hookListener.registerListeners();
    }

    @Override
    public void shutdown() {
        if (this.lisDurability != null) {
            this.lisDurability.unregisterListeners();
            this.lisDurability = null;
        }
        if (this.lisHand != null) {
            this.lisHand.unregisterListeners();
            this.lisHand = null;
        }
        if (this.lisReq != null) {
            this.lisReq.unregisterListeners();
            this.lisReq = null;
        }
        if (this.lisQuantum != null) {
            this.lisQuantum.unregisterListeners();
            this.lisQuantum = null;
        }
    }
}
