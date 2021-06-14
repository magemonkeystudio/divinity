package su.nightexpress.quantumrpg.hooks.external;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.TraitInfo;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.hooks.HookState;
import su.nexmedia.engine.hooks.NHook;
import su.nightexpress.quantumrpg.QuantumRPG;

import java.util.HashSet;
import java.util.Set;

public class CitizensHK extends NHook<QuantumRPG> {
    private Set<TraitInfo> traits;

    public CitizensHK(@NotNull QuantumRPG plugin) {
        super(plugin);
    }

    @NotNull
    public HookState setup() {
        this.traits = new HashSet<>();
        return HookState.SUCCESS;
    }

    public void shutdown() {
        unregisterTraits();
    }

    public void registerTrait(@NotNull TraitInfo trait) {
        unregisterTrait(trait);
        if (this.traits.add(trait)) {
            this.plugin.info("[Citizens Hook] Registered trait: " + trait.getTraitName());
            CitizensAPI.getTraitFactory().registerTrait(trait);
        }
    }

    public void unregisterTrait(@NotNull TraitInfo trait) {
        if (this.traits.remove(trait))
            this.plugin.info("[Citizens Hook] Unregistered trait: " + trait.getTraitName());
        CitizensAPI.getTraitFactory().deregisterTrait(trait);
    }

    private void unregisterTraits() {
        for (TraitInfo ti : this.traits)
            unregisterTrait(ti);
        this.traits = null;
    }
}
