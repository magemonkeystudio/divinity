package su.nightexpress.quantumrpg.manager.damage;

import mc.promcteam.engine.hooks.Hooks;
import mc.promcteam.engine.manager.IListener;
import mc.promcteam.engine.utils.LocUT;
import mc.promcteam.engine.utils.random.Rnd;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.api.PartyAPI;
import su.nightexpress.quantumrpg.api.event.RPGDamageEvent;
import su.nightexpress.quantumrpg.config.EngineCfg;
import su.nightexpress.quantumrpg.hooks.external.MythicMobsHK;
import su.nightexpress.quantumrpg.manager.effects.main.AdjustStatEffect;
import su.nightexpress.quantumrpg.manager.effects.main.DisarmEffect;
import su.nightexpress.quantumrpg.modules.list.party.PartyManager.Party;
import su.nightexpress.quantumrpg.stats.EntityStats;
import su.nightexpress.quantumrpg.stats.items.ItemStats;
import su.nightexpress.quantumrpg.stats.items.attributes.DamageAttribute;
import su.nightexpress.quantumrpg.stats.items.attributes.DefenseAttribute;
import su.nightexpress.quantumrpg.stats.items.attributes.api.AbstractStat;
import su.nightexpress.quantumrpg.stats.items.attributes.stats.BleedStat;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.DoubleUnaryOperator;

@SuppressWarnings("deprecation")
public class DamageManager extends IListener<QuantumRPG> {

	private static final QuantumRPG plugin;

	static {
		plugin = QuantumRPG.getInstance();
	}
	//private CrackShotHK csHook;

	private MythicMobsHK mmHook;

	public DamageManager() {
		super(plugin);
	}

	@Nullable
	public static LivingEntity getTargetByDirection(@NotNull Entity damager) {
		return DamageManager.getTargetByDirection(damager, EngineCfg.COMBAT_MAX_GET_TARGET_DISTANCE);
	}

	@Nullable
	public static LivingEntity getTargetByDirection(@NotNull Entity damager, double range) {
		Location start = damager.getLocation();
		if (damager instanceof LivingEntity) {
			start = ((LivingEntity) damager).getEyeLocation();
		}
		Vector increase = start.getDirection();

		return DamageManager.getTargetByDirection(damager, start, increase, range);
	}

	@Nullable
	public static LivingEntity getTargetByDirection(@NotNull Entity damager, @NotNull Location from, @NotNull Location to) {
		return DamageManager.getTargetByDirection(damager, from, to, EngineCfg.COMBAT_MAX_GET_TARGET_DISTANCE);
	}

	@Nullable
	public static LivingEntity getTargetByDirection(@NotNull Entity damager, @NotNull Location from, @NotNull Location to, double range) {
		Vector increase = LocUT.getDirectionTo(from, to);
		return DamageManager.getTargetByDirection(damager, from, increase, range);
	}

	@Nullable
	public static LivingEntity getTargetByDirection(@NotNull Entity damager, @NotNull Location from, @NotNull Vector dir, double range) {
		LivingEntity target = null;
		Vector increase = dir;

		Party party = null;
		if (damager.getType() == EntityType.PLAYER) {
			party = PartyAPI.getPlayerParty((Player) damager);
		}

		for (int counter = 0; counter < range; counter++) {
			Location point = from.add(increase);

			Material wall = point.getBlock().getType();
			if (wall != Material.AIR || wall.isSolid()) {
				break;
			}

			for (Entity entity : point.getChunk().getEntities()) {
				if (!Hooks.canFights(damager, entity)) continue;
				LivingEntity entity2 = (LivingEntity) entity;
				if (entity2.isDead() || !entity2.isValid()) continue;
				if (entity2.getEyeLocation().distance(point) > 1.5) continue;
				if (party != null && entity2 instanceof Player && party.isMember((Player) entity2)) {
					continue;
				}

				return entity2;
			}
		}
		return target;
	}

	@NotNull
	public static Set<LivingEntity> getTargetsByRange(@NotNull Entity damager, double range) {
		return getTargetsByRange(damager, damager.getLocation(), range);
	}

	@NotNull
	public static Set<LivingEntity> getTargetsByRange(@NotNull Entity damager, @NotNull Location from, double range) {
		Set<LivingEntity> set = new HashSet<>();

		Party party = null;
		if (damager.getType() == EntityType.PLAYER) {
			party = PartyAPI.getPlayerParty((Player) damager);
		}

		ProjectileSource shooter = null;
		if (damager instanceof Projectile) {
			shooter = ((Projectile) damager).getShooter();
		}

		for (Entity entity : damager.getWorld().getNearbyEntities(from, range, range, range)) {
			if (!Hooks.canFights(damager, entity)) continue;
			LivingEntity target = (LivingEntity) entity;
			if (target.isDead() || !target.isValid()) continue;

			if (party != null && target instanceof Player && party.isMember((Player) target)) {
				continue;
			}
			if (shooter != null && target.equals(shooter)) continue;

			set.add(target);
		}
		return set;
	}

	public void setup() {
		this.mmHook = plugin.getHook(MythicMobsHK.class);
		//this.csHook = plugin.getHook(CrackShotHK.class);

		this.registerListeners();
	}

	public void shutdown() {
		this.unregisterListeners();
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onDamageFishHook(PlayerFishEvent e) {
		if (!EngineCfg.COMBAT_FISHING_HOOK_DO_DAMAGE) return;

		Entity caught = e.getCaught();
		if (!(caught instanceof LivingEntity)) return;

		Player player = e.getPlayer();
		LivingEntity target = (LivingEntity) caught;

		target.damage(1D, player);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onDamageRPGStart(@NotNull RPGDamageEvent.Start e) {
		LivingEntity victim = e.getVictim();
		LivingEntity damager = e.getDamager();
		Projectile projectile = e.getProjectile();
		DamageMeta meta = e.getDamageMeta();
		EntityDamageEvent orig = e.getOriginalEvent();

		EntityStats statsDamager = e.getDamagerStats();
		EntityStats statsVictim = e.getVictimStats();

		// Handle damager stats
		// If false then event is cancelled, then stop calculations
		if (damager != null && statsDamager != null) {
			if (!this.handleDamageModifiers(e, victim, damager, statsVictim, statsDamager, projectile, orig, meta)) {
				return;
			}
		}

		RPGDamageEvent.Pre eventPre = new RPGDamageEvent.Pre(victim, damager, projectile, orig, meta);
		plugin.getPluginManager().callEvent(eventPre);
		if (eventPre.isCancelled()) return;

		String mythicFaction = "";
		if (this.mmHook != null && this.mmHook.isMythicMob(victim)) {
			mythicFaction = this.mmHook.getMythicInstance(victim).getFaction();
		}

		double powerMod = statsDamager != null ? statsDamager.getAttackPowerModifier() : 1D;
		double directMod = meta.getDirectModifier();
		double critMod = meta.getCriticalModifier();
		double blockMod = meta.getBlockModifier();
		double penetrateMod = meta.getPenetrateModifier();
		double pveDamageMod = meta.getPvEDamageModifier();
		double pveDefenseMod = meta.getPvEDefenseModifier();
		double enchantFactorMod = this.getEnchantModifier(victim, e.getCause());
		meta.setEnchantProtectionModifier(enchantFactorMod);

		Map<DefenseAttribute, Double> defenses = e.getDefenseMap();

		for (Map.Entry<DamageAttribute, Double> en : e.getDamageMap().entrySet()) {
			DamageAttribute dmgAtt = en.getKey();
			double dmgType = en.getValue();

			dmgType *= pveDamageMod;
			dmgType *= critMod;
			dmgType *= dmgAtt.getDamageModifierByEntityType(victim);
			dmgType *= dmgAtt.getDamageModifierByMythicFaction(mythicFaction);
			dmgType *= enchantFactorMod;
			dmgType *= powerMod;
			dmgType *= blockMod;
			double directType = dmgType * directMod; // Get direct value for this Damage Attribute
			dmgType = Math.max(0, dmgType - directType); // Deduct this value from damage

			if (dmgType > 0) {
				DefenseAttribute defAtt = dmgAtt.getAttachedDefense();
				if (defAtt != null && defenses.containsKey(defAtt)) {
					double def = Math.max(0, defenses.get(defAtt) * pveDefenseMod * penetrateMod);

					double defCalced = Math.max(0, dmgType - (def * defAtt.getProtectionFactor()));
					meta.setDefendedDamage(defAtt, dmgType - defCalced);
					dmgType = defCalced;
				}
			}
			//Should we reactivate direct damage, remove directType here and deal the damage straight.
			meta.setDamage(dmgAtt, dmgType + directType);

			// Actions Executor
			if (damager != null) {
				dmgAtt.getHitActions().process(damager);
			}
		}

		double dmgTotal = meta.getTotalDamage();
		orig.setDamage(dmgTotal);

		if (damager != null && statsDamager != null && dmgTotal > 0) {
			if (!this.handleDamagePostEffects(e, victim, damager, statsVictim, statsDamager, projectile, orig, meta)) {
				return;
			}
		}

		RPGDamageEvent.Exit eventExit = new RPGDamageEvent.Exit(victim, damager, projectile, orig, meta);
		plugin.getPluginManager().callEvent(eventExit);
		if (eventExit.isCancelled()) return;

		if (damager != null) {
			if (this.mmHook != null) {
				this.mmHook.setSkillDamage(damager, dmgTotal);
			}
		}
	}

	private boolean handleDamageModifiers(
			@NotNull RPGDamageEvent event,
			@NotNull LivingEntity victim,
			@NotNull LivingEntity damager,
			@NotNull EntityStats statsVictim,
			@NotNull EntityStats statsDamager,
			@Nullable Projectile projectile,
			@NotNull EntityDamageEvent e,
			@NotNull DamageMeta meta
	) {

		double dodgeRate = statsVictim.getItemStat(AbstractStat.Type.DODGE_RATE, false);
		if (dodgeRate > 0D) {
			double accurRate = event.getDamagerItemStat(AbstractStat.Type.ACCURACY_RATE);
			if (Rnd.get(true) < dodgeRate && Rnd.get(true) >= accurRate) {

				RPGDamageEvent.Dodge qDodge = new RPGDamageEvent.Dodge(victim, damager, projectile, e, meta);
				plugin.getPluginManager().callEvent(qDodge);
				if (!qDodge.isCancelled()) {
					e.setDamage(0D);
					e.setCancelled(true);
					meta.setDodge(true);
					return false;
				}
			}
		}

		double critModifier = 1D;
		double critRate = event.getDamagerItemStat(AbstractStat.Type.CRITICAL_RATE);
		if (critRate > 0 && Rnd.get(true) < critRate) {
			critModifier = event.getDamagerItemStat(AbstractStat.Type.CRITICAL_DAMAGE);
			if (critModifier == 0D) {
				critModifier = 1D;
			}
		}
		meta.setCriticalModifier(critModifier);

		AbstractStat.Type pvpDefenseType = AbstractStat.Type.PVE_DEFENSE;
		AbstractStat.Type pvpDamageType = AbstractStat.Type.PVE_DAMAGE;

		if (statsVictim.isPlayer() && statsDamager.isPlayer()) {
			pvpDefenseType = AbstractStat.Type.PVP_DEFENSE;
			pvpDamageType = AbstractStat.Type.PVP_DAMAGE;
		}

		double directPercent = /*event.getDamagerItemStat(AbstractStat.Type.DIRECT_DAMAGE)*/100D / 100D;
		double penetration = 1D - event.getDamagerItemStat(AbstractStat.Type.PENETRATION) / 100D;

		double pvpeDefense = 1D + statsVictim.getItemStat(pvpDefenseType, false) / 100D;
		double pvpeDamage = 1D + event.getDamagerItemStat(pvpDamageType) / 100D;

		meta.setPvEDamageModifier(pvpeDamage);
		meta.setPvEDefenseModifier(pvpeDefense);
		meta.setDirectModifier(directPercent);
		meta.setPenetrateModifier(penetration);

		double blockModifier = statsVictim.getItemStat(AbstractStat.Type.BLOCK_DAMAGE, false);
		double blockRate = statsVictim.getItemStat(AbstractStat.Type.BLOCK_RATE, false);

		Player player = statsVictim.getPlayer();
		boolean isVanillaBlocked = false;
		if (player != null && player.isBlocking() && player.getCooldown(Material.SHIELD) <= 0) {
			// Damage in face = Block
			if (e.getDamage(DamageModifier.BLOCKING) < 0) {
				isVanillaBlocked = true;
				blockRate += EngineCfg.COMBAT_SHIELD_BLOCK_BONUS_RATE;
				blockModifier += EngineCfg.COMBAT_SHIELD_BLOCK_BONUS_DAMAGE_MOD;
			}
		}

		if (blockRate > 0D) {
			double vanillaBlockModifier = 1D;
			if (Rnd.get(true) < blockRate) {
				vanillaBlockModifier = 0D;
				meta.setBlockModifier(1D - blockModifier / 100D);

				if (isVanillaBlocked && player != null) {
					player.setCooldown(Material.SHIELD, 20 * EngineCfg.COMBAT_SHIELD_BLOCK_COOLDOWN);
				}
			}
			// Fix/Disable vanilla shield block
			if (e.isApplicable(DamageModifier.BLOCKING)) {
				e.setDamage(DamageModifier.BLOCKING, vanillaBlockModifier);
			}
		}

		return true;
	}

	private boolean handleDamagePostEffects(
			@NotNull RPGDamageEvent event,
			@NotNull LivingEntity victim,
			@NotNull LivingEntity damager,
			@NotNull EntityStats statsVictim,
			@NotNull EntityStats statsDamager,
			@Nullable Projectile projectile,
			@NotNull EntityDamageEvent e,
			@NotNull DamageMeta meta
	) {

		final double aoe = event.getDamagerItemStat(AbstractStat.Type.AOE_DAMAGE);
		if (aoe > 0D && e.getCause() != DamageCause.ENTITY_SWEEP_ATTACK) {
			if (statsVictim.isIgnoreAOE()) {
				statsVictim.setIgnoreAOE(false);
			} else {
				plugin.getServer().getScheduler().runTask(plugin, () -> {
					Set<LivingEntity> targets = DamageManager.getTargetsByRange(damager, 2D);
					targets.remove(victim);
					targets.remove(damager);
					DoubleUnaryOperator operator = (dmg) -> dmg * (aoe / 100D);

					for (LivingEntity targetAoe : targets) {
						AdjustStatEffect aoeReducer = new AdjustStatEffect.Builder(-1)
								.withCharges(1).withAdjust(ItemStats.getDamages(), operator).build();
						aoeReducer.applyTo(damager);

						EntityStats.get(targetAoe).setIgnoreAOE(true);
						targetAoe.damage(1D, damager);
					}
				});
			}
		}

		double dmgTotal = meta.getTotalDamage();

		double disarm = event.getDamagerItemStat(AbstractStat.Type.DISARM_RATE);
		if (disarm > 0D && Rnd.get(true) < disarm) {
			DisarmEffect disarmEffect = new DisarmEffect();
			disarmEffect.applyTo(victim);
		}

		double burn = event.getDamagerItemStat(AbstractStat.Type.BURN_RATE);
		if (burn > 0D && Rnd.get(true) < burn) {
			victim.setFireTicks(100);
		}

		double bleed = event.getDamagerItemStat(AbstractStat.Type.BLEED_RATE);
		if (bleed > 0D && Rnd.get(true) < bleed) {
			BleedStat bleedStat = ItemStats.getStat(BleedStat.class);
			if (bleedStat != null) {
				bleedStat.bleed(victim, dmgTotal);
			}
		}

		double vamp = Math.max(0, dmgTotal * (event.getDamagerItemStat(AbstractStat.Type.VAMPIRISM) / 100D));
		if (vamp > 0D) {
			EntityRegainHealthEvent eventRegain = new EntityRegainHealthEvent(damager, vamp, RegainReason.CUSTOM);
			plugin.getPluginManager().callEvent(eventRegain);
			if (!eventRegain.isCancelled()) {
				double max = EntityStats.getEntityMaxHealth(damager);
				damager.setHealth(Math.min(max, damager.getHealth() + vamp));
			}
		}

		double thorn = statsVictim.getItemStat(AbstractStat.Type.THORNMAIL, false) / 100D;
		if (thorn > 0D) {
			damager.damage(dmgTotal * thorn);
		}

		return true;
	}

	private double getEnchantModifier(@NotNull LivingEntity zertva, @NotNull DamageCause cause) {
		EntityStats stats = EntityStats.get(zertva);

		double epfAll = stats.getEnchantProtectFactor(Enchantment.PROTECTION_ENVIRONMENTAL);
		double epfSpec = 0D;
		double epfMod = 1D;

		if (cause == DamageCause.FIRE || cause == DamageCause.FIRE_TICK
				|| cause == DamageCause.LAVA) {
			epfSpec = stats.getEnchantProtectFactor(Enchantment.PROTECTION_FIRE);
		} else if (cause == DamageCause.FALL) {
			epfSpec = stats.getEnchantProtectFactor(Enchantment.PROTECTION_FALL);
		} else if (cause == DamageCause.PROJECTILE) {
			epfSpec = stats.getEnchantProtectFactor(Enchantment.PROTECTION_PROJECTILE);
		} else if (cause == DamageCause.BLOCK_EXPLOSION || cause == DamageCause.ENTITY_EXPLOSION) {
			epfSpec = stats.getEnchantProtectFactor(Enchantment.PROTECTION_EXPLOSIONS);
		}
		epfMod = Math.min(20D, (epfSpec + epfAll));

		return (1D - epfMod / 25D);
	}
}
