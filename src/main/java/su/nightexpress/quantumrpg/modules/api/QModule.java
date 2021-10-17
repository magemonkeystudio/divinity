package su.nightexpress.quantumrpg.modules.api;

import mc.promcteam.engine.commands.list.HelpCommand;
import mc.promcteam.engine.modules.IModule;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.modules.api.socketing.ModuleSocket;
import su.nightexpress.quantumrpg.modules.command.*;

public abstract class QModule extends IModule<QuantumRPG> {

    public QModule(@NotNull QuantumRPG plugin) {
        super(plugin);
    }

    @Override
    protected void onPostSetup() {
        if (this instanceof QModuleDrop) {
            QModuleDrop<?> md = (QModuleDrop<?>) this;
            md.loadSettings();
            md.loadItems();
        }

        if (this.moduleCommand != null) {
            this.moduleCommand.addDefaultCommand(new HelpCommand<>(this.plugin));
            if (this.isDropable()) {
                QModuleDrop<?> md = (QModuleDrop<?>) this;
                this.moduleCommand.addSubCommand(new MGetCmd(md));
                this.moduleCommand.addSubCommand(new MGiveCmd(md));
                this.moduleCommand.addSubCommand(new MDropCmd(md));
                this.moduleCommand.addSubCommand(new MListCmd(md));
            }
            this.moduleCommand.addSubCommand(new MReloadCmd(this));
        }
    }

    public final boolean isDropable() {
        return this instanceof QModuleDrop;
    }

    public final boolean isSocketable() {
        return this instanceof ModuleSocket;
    }
}
