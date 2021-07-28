package su.nightexpress.quantumrpg.hooks;

import su.nightexpress.quantumrpg.QListener;
import su.nightexpress.quantumrpg.QuantumRPG;

public abstract class Hook extends QListener<QuantumRPG> {
    protected EHook type;

    public Hook(EHook type, QuantumRPG plugin) {
        super(plugin);
        this.type = type;
    }

    public abstract void setup();

    public abstract void shutdown();
}
