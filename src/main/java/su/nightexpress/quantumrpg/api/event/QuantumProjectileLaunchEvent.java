package su.nightexpress.quantumrpg.api.event;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.quantumrpg.config.EngineCfg;
import su.nightexpress.quantumrpg.stats.ProjectileStats;

public class QuantumProjectileLaunchEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private boolean cancelled;

    private Entity pj;

    private Location loc;

    private ItemStack bow;

    private LivingEntity shooter;

    private double power;

    private boolean isBowEvent;

    public QuantumProjectileLaunchEvent(@NotNull Entity pj, @NotNull Location loc, @NotNull LivingEntity shooter, @Nullable ItemStack bow, double power, boolean isBowEvent) {
        setProjectile(pj);
        this.loc = loc;
        setShooter(shooter);
        if (bow != null) {
            setWeapon(bow);
        } else {
            this.bow = bow;
        }
        setPower(power);
        this.isBowEvent = isBowEvent;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @NotNull
    public Entity getProjectile() {
        return this.pj;
    }

    public void setProjectile(@NotNull Entity pj) {
        this.pj = pj;
    }

    @NotNull
    public Location getLocation() {
        return this.loc;
    }

    @NotNull
    public LivingEntity getShooter() {
        return this.shooter;
    }

    public void setShooter(@NotNull LivingEntity shooter) {
        if (this.pj instanceof Projectile)
            ((Projectile) this.pj).setShooter((ProjectileSource) shooter);
        this.shooter = shooter;
    }

    @Nullable
    public ItemStack getWeapon() {
        return this.bow;
    }

    public void setWeapon(@NotNull ItemStack bow) {
        if (this.pj instanceof Projectile && (this.shooter instanceof org.bukkit.entity.Player || EngineCfg.ATTRIBUTES_EFFECTIVE_FOR_MOBS))
            ProjectileStats.setSrcWeapon((Projectile) this.pj, bow);
        this.bow = bow;
    }

    public double getPower() {
        return this.power;
    }

    public void setPower(double power) {
        if (this.pj instanceof Projectile)
            ProjectileStats.setPower((Projectile) this.pj, power);
        this.power = power;
    }

    public boolean isBowEvent() {
        return this.isBowEvent;
    }
}
