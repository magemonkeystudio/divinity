package com.promcteam.divinity.modules.command;

import com.promcteam.codex.commands.api.ISubCommand;
import org.jetbrains.annotations.NotNull;
import com.promcteam.divinity.Divinity;
import com.promcteam.divinity.modules.api.QModule;

public abstract class MCmd<M extends QModule> extends ISubCommand<Divinity> {

    protected M module;

    public MCmd(@NotNull M module, @NotNull String[] aliases, String permission) {
        super(module.plugin, aliases, permission);
        this.module = module;
    }
}
