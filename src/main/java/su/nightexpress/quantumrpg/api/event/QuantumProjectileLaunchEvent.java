package su.nightexpress.quantumrpg.api.event;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nexmedia.engine.manager.api.event.ICancellableEvent;
import su.nightexpress.quantumrpg.config.EngineCfg;
import su.nightexpress.quantumrpg.stats.ProjectileStats;

public class QuantumProjectileLaunchEvent extends ICancellableEvent {

	private Entity pj;
	private Location loc;
	private ItemStack bow;
	private LivingEntity shooter;
	private double power;
	private boolean isBowEvent;

	public QuantumProjectileLaunchEvent(
			@NotNull Entity pj,
			@NotNull Location loc,
			@NotNull LivingEntity shooter,
			@Nullable ItemStack bow,
			double power,
			boolean isBowEvent
			) {
	    this.setProjectile(pj);
	    this.loc = loc;
	    this.setShooter(shooter);
	    if (bow != null) {
	    	this.setWeapon(bow);
	    }
	    else {
	    	this.bow = bow;
	    }
	    this.setPower(power);
	    this.isBowEvent = isBowEvent;
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
		if (this.pj instanceof Projectile) {
			((Projectile) this.pj).setShooter(shooter);
		}
		this.shooter = shooter;
	}
	
	@Nullable
	public ItemStack getWeapon() {
		return this.bow;
	}
	
	public void setWeapon(@NotNull ItemStack bow) {
		if (this.pj instanceof Projectile && (this.shooter instanceof Player || EngineCfg.ATTRIBUTES_EFFECTIVE_FOR_MOBS)) {
			ProjectileStats.setSrcWeapon((Projectile) this.pj, bow);
		}
		this.bow = bow;
	}
	
	public double getPower() {
		return this.power;
	}
	
	public void setPower(double power) {
		if (this.pj instanceof Projectile) {
			ProjectileStats.setPower((Projectile) this.pj, power);
		}
		this.power = power;
	}
	
	public boolean isBowEvent() {
		return this.isBowEvent;
	}
}
