package com.promcteam.divinity.manager.effects.main;

import com.promcteam.codex.util.ItemUT;
import com.promcteam.divinity.manager.effects.IEffect;
import com.promcteam.divinity.manager.effects.IEffectType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DisarmEffect extends IEffect {

    public DisarmEffect() {
        this(null);
    }

    public DisarmEffect(@Nullable LivingEntity caster) {
        this(caster, 1);
    }

    public DisarmEffect(@Nullable LivingEntity caster, int charges) {
        super(caster, charges);
    }

    @Override
    @NotNull
    public IEffectType getType() {
        return IEffectType.DISARM;
    }

    @Override
    protected boolean onTrigger(boolean force) {
        EntityEquipment equipment = this.target.getEquipment();
        if (equipment == null) return true;

        ItemStack hand = equipment.getItemInMainHand();
        if (ItemUT.isAir(hand)) {
            return true;
        }

        equipment.setItemInMainHand(null);
        this.target.getWorld().dropItemNaturally(target.getLocation(), hand).setPickupDelay(50);

        return true;
    }

    @Override
    protected void onClear() {

    }

    @Override
    public boolean resetOnDeath() {
        return true;
    }
}
