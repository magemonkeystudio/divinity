package com.promcteam.divinity.utils.actions.executors;

import com.promcteam.codex.utils.actions.actions.IActionExecutor;
import com.promcteam.codex.utils.actions.params.IParamResult;
import com.promcteam.codex.utils.actions.params.IParamType;
import com.promcteam.divinity.Divinity;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import com.promcteam.divinity.utils.ParticleUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class ActionParticlePulse extends IActionExecutor {

    public ActionParticlePulse(@NotNull Divinity plugin) {
        super(plugin, "PARTICLE_PULSE");
    }

    @Override
    public void registerParams() {
        this.registerParam(IParamType.DELAY);
        this.registerParam(IParamType.TARGET);
        this.registerParam(IParamType.NAME);
    }

    @Override
    protected void execute(Entity exe, Set<Entity> targets, IParamResult result) {
        String   name  = result.getParamValue(IParamType.NAME).getString("REDSTONE-CRIT_MAGIC");
        String[] split = name.split("-");

        String eff1 = split[0];
        String eff2 = "";
        if (split.length > 1) {
            eff2 = split[1];
        }

        if (!targets.isEmpty()) {
            for (Entity e : targets) {
                ParticleUtils.spiral(e.getLocation(), eff1, eff2);
            }
        } else {
            ParticleUtils.spiral(exe.getLocation(), eff1, eff2);
        }
    }

    @Override
    public boolean mustHaveTarget() {
        return false;
    }

    @Override
    @NotNull
    public List<String> getDescription() {
        // TODO Auto-generated method stub
        return Arrays.asList("Draws a particle wave");
    }

}
