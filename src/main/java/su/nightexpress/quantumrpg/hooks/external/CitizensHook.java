package su.nightexpress.quantumrpg.hooks.external;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.TraitInfo;
import org.bukkit.entity.Entity;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.hooks.EHook;
import su.nightexpress.quantumrpg.hooks.Hook;
import su.nightexpress.quantumrpg.hooks.external.citizens.traits.ExtractorTrait;
import su.nightexpress.quantumrpg.hooks.external.citizens.traits.RepairTrait;
import su.nightexpress.quantumrpg.hooks.external.citizens.traits.ResolveTrait;
import su.nightexpress.quantumrpg.modules.EModule;

import java.util.ArrayList;
import java.util.List;

public class CitizensHook extends Hook {
    private List<TraitInfo> traits;

    public CitizensHook(EHook type, QuantumRPG plugin) {
        super(type, plugin);
    }

    public void setup() {
        registerTraits();
    }

    public void shutdown() {
        unregisterTraits();
    }

    public boolean isNPC(Entity e) {
        return CitizensAPI.getNPCRegistry().isNPC(e);
    }

    private void registerTraits() {
        this.traits = new ArrayList<>();
        if (EModule.RESOLVE.isEnabled()) {
            TraitInfo resolve = TraitInfo.create(ResolveTrait.class).withName("resolver");
            this.traits.add(resolve);
        }
        if (EModule.EXTRACTOR.isEnabled()) {
            TraitInfo ext = TraitInfo.create(ExtractorTrait.class).withName("extractor");
            this.traits.add(ext);
        }
        if (EModule.REPAIR.isEnabled()) {
            TraitInfo rep = TraitInfo.create(RepairTrait.class).withName("repair");
            this.traits.add(rep);
        }
        for (TraitInfo ti : this.traits)
            CitizensAPI.getTraitFactory().registerTrait(ti);
    }

    private void unregisterTraits() {
        for (TraitInfo ti : this.traits)
            CitizensAPI.getTraitFactory().deregisterTrait(ti);
        this.traits = null;
    }
}
