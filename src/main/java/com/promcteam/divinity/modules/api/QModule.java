package com.promcteam.divinity.modules.api;

import com.promcteam.codex.commands.list.HelpCommand;
import com.promcteam.codex.modules.IModule;
import com.promcteam.divinity.Divinity;
import com.promcteam.divinity.modules.api.socketing.ModuleSocket;
import com.promcteam.divinity.modules.command.*;
import org.jetbrains.annotations.NotNull;

public abstract class QModule extends IModule<Divinity> {

    public QModule(@NotNull Divinity plugin) {
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
