package studio.magemonkey.divinity.manager.effects.main;

import studio.magemonkey.divinity.manager.effects.IEffectType;
import studio.magemonkey.divinity.manager.effects.IPeriodicEffect;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class StunEffect extends IPeriodicEffect {

    private Location loc;

    private StunEffect(@NotNull Builder builder) {
        super(builder);
    }

    @Override
    public boolean applyTo(@NotNull LivingEntity entity) {
        if (super.applyTo(entity)) {
            this.loc = entity.getLocation();
            return true;
        }
        return false;
    }

    @Override
    public boolean onTrigger(boolean force) {
        this.applyPotionEffects();
        if (this.getTarget().isOnGround()) {
            this.getTarget().teleport(this.loc);
        }
        return true;
    }

    @Override
    public void onClear() {
        this.removePotionEffects();
    }

    @Override
    public boolean resetOnDeath() {
        return true;
    }

    @Override
    @NotNull
    public IEffectType getType() {
        return IEffectType.CONTROL_STUN;
    }

    public static class Builder extends IPeriodicEffect.Builder<Builder> {

        public Builder(double lifeTime) {
            super(lifeTime, 1D / 20D);

            this.addPotionEffects(new PotionEffect(PotionEffectType.SLOW, (int) (lifeTime * 20), 127));
            this.addPotionEffects(new PotionEffect(PotionEffectType.SLOW_DIGGING, (int) (lifeTime * 20), 127));
            this.addPotionEffects(new PotionEffect(PotionEffectType.BLINDNESS, (int) lifeTime * 20, 127));
        }

        @Override
        @NotNull
        public StunEffect build() {
            return new StunEffect(this);
        }

        @Override
        @NotNull
        protected Builder self() {
            return this;
        }
    }
}