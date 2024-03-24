package com.promcteam.divinity.modules.list.drops.object;

import com.promcteam.codex.CodexEngine;
import com.promcteam.codex.utils.random.Rnd;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class DropMoney extends DropNonItem {

    private double min, max;
    private boolean allowDecimals;

    public DropMoney(ConfigurationSection config) {
        super(config);

        min = config.getDouble("min-amount", 0.0);
        max = config.getDouble("max-amount", 0.0);
        allowDecimals = config.getBoolean("allow-decimals");
    }

    @Override
    public void execute(Player target) {
        CodexEngine.get()
                .getVault()
                .getEconomy()
                .depositPlayer(target, ((allowDecimals) ? Rnd.getDouble(min, max) : Rnd.get((int) min, (int) max)));
    }
}
