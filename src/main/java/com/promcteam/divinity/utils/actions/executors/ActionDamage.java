package com.promcteam.divinity.utils.actions.executors;

import com.promcteam.codex.utils.actions.actions.IActionExecutor;
import com.promcteam.codex.utils.actions.params.IParamResult;
import com.promcteam.codex.utils.actions.params.IParamType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;
import com.promcteam.divinity.QuantumRPG;
import com.promcteam.divinity.manager.effects.main.AdjustStatEffect;
import com.promcteam.divinity.stats.items.ItemStats;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.DoubleUnaryOperator;

public class ActionDamage extends IActionExecutor {

    public ActionDamage(@NotNull QuantumRPG plugin) {
        super(plugin, "DAMAGE");
    }

    @Override
    public void registerParams() {
        this.registerParam(IParamType.DELAY);
        this.registerParam(IParamType.TARGET);
        this.registerParam(IParamType.AMOUNT);
    }

    @Override
    protected void execute(Entity exe, Set<Entity> targets, IParamResult result) {
        double dmgAdd = result.getParamValue(IParamType.AMOUNT).getDouble(0);
        if (dmgAdd == 0) return;

        boolean perc = result.getParamValue(IParamType.AMOUNT).getBoolean();

        Entity eDamager = exe;

        Projectile pj = null;

        if (eDamager instanceof Projectile) {
            pj = (Projectile) eDamager;

            ProjectileSource src = pj.getShooter();
            if (src instanceof LivingEntity) {
                eDamager = (Entity) src;
            }
        }

        LivingEntity        damager  = eDamager instanceof LivingEntity ? (LivingEntity) eDamager : null;
        DoubleUnaryOperator operator = (d) -> d == 0D ? d : ((perc) ? d * (dmgAdd / 100D) : d + dmgAdd);

        for (Entity eTarget : targets) {
            if (!(eTarget instanceof LivingEntity)) continue;
            LivingEntity victim = (LivingEntity) eTarget;

            if (damager != null) {
                AdjustStatEffect adjust = new AdjustStatEffect.Builder(-1)
                        .withCharges(1).withAdjust(ItemStats.getDamages(), operator).build();
                adjust.applyTo(damager);
            }

            victim.damage(1D, eDamager);
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
        return Arrays.asList("Inflicts damage");
    }

}
