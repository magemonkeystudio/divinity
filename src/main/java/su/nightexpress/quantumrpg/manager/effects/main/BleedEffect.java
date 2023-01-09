package su.nightexpress.quantumrpg.manager.effects.main;

import java.util.function.Function;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import mc.promcteam.engine.utils.EffectUT;
import su.nightexpress.quantumrpg.manager.effects.IEffectType;
import su.nightexpress.quantumrpg.manager.effects.IPeriodicEffect;

public class BleedEffect extends IPeriodicEffect {

	protected Function<LivingEntity, Double> damageFunction;
	protected String blood;
	
	private BleedEffect(@NotNull Builder builder) {
		super(builder);
		this.damageFunction = builder.damageFunction;
		
		this.blood = builder.blood.name();
		if (builder.bloodColor != null) {
			this.blood += ":" + builder.bloodColor;
		}
	}

	@Override
	public boolean onTrigger(boolean force) {
		double damage = this.damageFunction.apply(this.target);
		this.target.damage(damage);
		EffectUT.playEffect(this.target.getEyeLocation(), this.blood, 0.2f, 0.2f, 0.2f, 0.1f, 20);
		
		return true;
	}

	@Override
	public void onClear() {
		
	}

	@Override
	public boolean resetOnDeath() {
		return true;
	}

	@Override
	@NotNull
	public IEffectType getType() {
		return IEffectType.HARM_BLEED;
	}
	
	public static class Builder extends IPeriodicEffect.Builder<Builder> {

		private Function<LivingEntity, Double> damageFunction;
		private Particle blood;
		private String bloodColor;
		
		public Builder(double lifeTime, double interval, @NotNull Function<LivingEntity, Double> damageFunction) {
			super(lifeTime, interval);
			this.withFunction(damageFunction);
			this.withBlood(Particle.BLOCK_CRACK);
			this.withColor(Material.REDSTONE_BLOCK.name());
		}
		
		public Builder withFunction(@NotNull Function<LivingEntity, Double> damageFunction) {
			this.damageFunction = damageFunction;
			return this.self();
		}
		
		@NotNull
		public Builder withBlood(@NotNull Particle blood) {
			this.blood = blood;
			return this.self();
		}
		
		@NotNull
		public Builder withColor(@NotNull String color) {
			this.bloodColor = color;
			return this.self();
		}

		@Override
		@NotNull
		public BleedEffect build() {
			return new BleedEffect(this);
		}

		@Override
		@NotNull
		protected Builder self() {
			return this;
		}
	}
}
