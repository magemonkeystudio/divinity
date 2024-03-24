package com.promcteam.divinity.utils.actions.executors;

import com.promcteam.codex.utils.actions.actions.IActionExecutor;
import com.promcteam.codex.utils.actions.params.IParamResult;
import com.promcteam.codex.utils.actions.params.IParamType;
import com.promcteam.codex.utils.actions.params.IParamValue;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import com.promcteam.divinity.QuantumRPG;
import com.promcteam.divinity.config.EngineCfg;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class ActionTakeMana extends IActionExecutor {

    public ActionTakeMana(@NotNull QuantumRPG plugin) {
        super(plugin, "TAKE_MANA");
    }

    @Override
    public void registerParams() {
        this.registerParam(IParamType.TARGET);
        this.registerParam(IParamType.DELAY);
        this.registerParam(IParamType.AMOUNT);
    }

    @Override
    protected void execute(Entity exe, Set<Entity> targets, IParamResult result) {
        IParamValue v1 = result.getParamValue(IParamType.AMOUNT);

        double amount = v1.getDouble(0);
        if (amount == 0) return;

        boolean ofMax = v1.getBoolean();

        for (Entity e : targets) {
            if (e.getType() == EntityType.PLAYER) {
                EngineCfg.HOOK_PLAYER_CLASS_PLUGIN.takeMana((Player) e, amount, ofMax);
            }
        }
    }

    @Override
    public boolean mustHaveTarget() {
        return true;
    }

    @Override
    @NotNull
    public List<String> getDescription() {
        // TODO Auto-generated method stub
        return Arrays.asList("Consumes mana");
    }

}
