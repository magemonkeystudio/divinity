package su.nightexpress.quantumrpg.modules.command;

import mc.promcteam.engine.commands.api.ISubCommand;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.modules.api.QModule;

public abstract class MCmd<M extends QModule> extends ISubCommand<QuantumRPG> {

    protected M module;

    public MCmd(@NotNull M module, @NotNull String[] aliases, String permission) {
        super(module.plugin, aliases, permission);
        this.module = module;
    }
}
