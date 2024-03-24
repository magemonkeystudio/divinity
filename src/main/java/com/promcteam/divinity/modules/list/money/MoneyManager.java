package com.promcteam.divinity.modules.list.money;

import com.promcteam.codex.commands.list.HelpCommand;
import com.promcteam.codex.config.api.JYML;
import com.promcteam.codex.hooks.external.VaultHK;
import com.promcteam.codex.utils.random.Rnd;
import com.promcteam.divinity.Divinity;
import org.jetbrains.annotations.NotNull;
import com.promcteam.divinity.modules.EModule;
import com.promcteam.divinity.modules.ModuleItem;
import com.promcteam.divinity.modules.api.QModuleDrop;
import com.promcteam.divinity.modules.command.MReloadCmd;

public class MoneyManager extends QModuleDrop<MoneyManager.QMoney> {

    public MoneyManager(@NotNull Divinity plugin) {
        super(plugin, QMoney.class);
    }

    @Override
    public String getId() {
        return EModule.MONEY;
    }

    @Override
    public String version() {
        return "1.0.0";
    }

    @Override
    public void setup() {

        VaultHK vault = plugin.getVault();

        if (vault == null || vault.getEconomy() == null) {
            this.error("Economy not found! Module will be disabled.");
            this.interruptLoad();
            return;
        }
    }

    @Override
    public void shutdown() {

    }

    @Override
    public void onPostSetup() {

        this.loadSettings();
        this.loadItems();

        if (this.moduleCommand != null) {
            this.moduleCommand.addDefaultCommand(new HelpCommand<>(this.plugin));
            this.moduleCommand.addSubCommand(new MReloadCmd(this));
        }
    }


    public class QMoney extends ModuleItem {

        private static final String MIN_MONEY      = "money.min";
        private static final String MAX_MONEY      = "money.max";
        private static final String ALLOW_DECIMALS = "money.allow-decimals";
        double min, max;
        boolean allowDecimals;

        public QMoney(@NotNull Divinity plugin, @NotNull JYML cfg) {
            super(plugin, cfg, MoneyManager.this);
            min = cfg.getDouble(MIN_MONEY, 0.0);
            max = cfg.getDouble(MAX_MONEY, 0.0);
            allowDecimals = cfg.getBoolean(ALLOW_DECIMALS);
        }

        public double getAmount() {

            return (allowDecimals) ? Rnd.getDouble(min, max) : Rnd.get((int) min, (int) max);
        }

    }

}
