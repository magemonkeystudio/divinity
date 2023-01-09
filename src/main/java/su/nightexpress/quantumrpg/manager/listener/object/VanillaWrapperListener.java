package su.nightexpress.quantumrpg.manager.listener.object;

import mc.promcteam.engine.hooks.Hooks;
import mc.promcteam.engine.manager.IListener;
import mc.promcteam.engine.utils.ItemUT;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.api.event.QuantumProjectileLaunchEvent;
import su.nightexpress.quantumrpg.api.event.RPGDamageEvent;
import su.nightexpress.quantumrpg.config.EngineCfg;
import su.nightexpress.quantumrpg.hooks.EHook;
import su.nightexpress.quantumrpg.hooks.external.SkillAPIHK;
import su.nightexpress.quantumrpg.manager.damage.DamageMeta;
import su.nightexpress.quantumrpg.modules.list.arrows.ArrowManager;
import su.nightexpress.quantumrpg.modules.list.arrows.ArrowManager.QArrow;
import su.nightexpress.quantumrpg.stats.EntityStats;
import su.nightexpress.quantumrpg.stats.ProjectileStats;
import su.nightexpress.quantumrpg.stats.items.ItemStats;
import su.nightexpress.quantumrpg.stats.items.attributes.AmmoAttribute;
import su.nightexpress.quantumrpg.stats.items.attributes.DamageAttribute;
import su.nightexpress.quantumrpg.stats.items.attributes.DefenseAttribute;
import su.nightexpress.quantumrpg.stats.items.attributes.api.AbstractStat;
import su.nightexpress.quantumrpg.utils.ItemUtils;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("deprecation")
public class VanillaWrapperListener extends IListener<QuantumRPG> {

    private static final String META_PROJECTILE_EVENT_FIXER = "QRPG_EVENT_FIX";

    public VanillaWrapperListener(@NotNull QuantumRPG plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVanillaShootBow(EntityShootBowEvent e) {
        ItemStack bow = e.getBow();

        LivingEntity shooter = e.getEntity();
        Projectile   pj      = (Projectile) e.getProjectile();
        Vector       orig    = pj.getVelocity();
        double       power   = e.getForce();

        // Quick fix for "high" skeleton damage
        if (shooter instanceof Skeleton) {
            power = 0.35D;
        }

        if (bow != null) {
            AmmoAttribute ammo = ItemStats.getAmmo(bow);
            if (ammo != null && ammo.getType() != AmmoAttribute.Type.ARROW) {
                boolean bounce = pj.doesBounce(),
                        glow = pj.isGlowing(),
                        gravity = pj.hasGravity();
                pj = ammo.getProjectile(shooter);
                pj.setVelocity(orig);
                pj.setBounce(bounce);
                pj.setGlowing(glow);
                pj.setGravity(gravity);
                e.setProjectile(pj);
            }
        }
        // Prevent duplicated event call
        pj.setMetadata(META_PROJECTILE_EVENT_FIXER, new FixedMetadataValue(plugin, "true"));

        QuantumProjectileLaunchEvent eve = new QuantumProjectileLaunchEvent(pj, pj.getLocation(), shooter, bow, power, true);
        plugin.getPluginManager().callEvent(eve);
        if (eve.isCancelled()) {
            e.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVanillaProjectLaunch(ProjectileLaunchEvent e) {
        Entity e1 = e.getEntity();
        if (e1.hasMetadata(META_PROJECTILE_EVENT_FIXER)) {
            e1.removeMetadata(META_PROJECTILE_EVENT_FIXER, plugin);
            return;
        }

        LivingEntity shooter;
        ItemStack    bow   = null;
        double       power = 1D;

        if (!(e1 instanceof Projectile)) return;

        Projectile       pp = (Projectile) e1;
        ProjectileSource ps = pp.getShooter();
        if (!(ps instanceof LivingEntity)) return;

        shooter = (LivingEntity) ps;

        String          pjType = pp.getType().name();
        EntityEquipment eq     = shooter.getEquipment();
        if (eq != null) {
            // Fix main hand damage when launched from off hand
            ItemStack off = eq.getItemInOffHand();
            if (ItemUT.isAir(off) || !off.getType().name().equalsIgnoreCase(pjType)) {
                bow = eq.getItemInMainHand();
            }
        }

        QuantumProjectileLaunchEvent eve = new QuantumProjectileLaunchEvent(e1, e1.getLocation(), shooter, bow, power, false);
        plugin.getPluginManager().callEvent(eve);
        if (eve.isCancelled()) {
            e.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVanillaDamage(EntityDamageEvent e) {
//        long l1 = System.currentTimeMillis();

        Entity eVictim = e.getEntity();
        if (!(eVictim instanceof LivingEntity)) return;

        // Disable vanilla armor effect.
        if (e.isApplicable(DamageModifier.ARMOR)) {
            e.setDamage(DamageModifier.ARMOR, 0D);
        }

        double damageStart = e.getDamage();

        ItemStack  weapon     = null;
        Projectile projectile = null;

        LivingEntity damager = null;
        LivingEntity victim  = (LivingEntity) eVictim;
        DamageCause  cause   = e.getCause();

        EntityStats statsDamager = null;
        EntityStats statsVictim  = EntityStats.get(victim);

        DamageMeta meta = new DamageMeta(victim, damager, weapon, cause);
        statsVictim.setLastDamageMeta(meta);

        boolean isEde        = e instanceof EntityDamageByEntityEvent;
        boolean isFullDamage = false;

        SkillAPIHK skillApi = (SkillAPIHK) QuantumRPG.getInstance().getHook(EHook.SKILL_API);
        // Here only check for damager
        // to be sure to use his stats or not.
        labelFullDamage:
        if (isEde) {
            EntityDamageByEntityEvent ede = (EntityDamageByEntityEvent) e;

            // Check if damager can fight victim
            Entity edeDamager = ede.getDamager();
            if (!Hooks.canFights(edeDamager, victim)) {
                ede.setCancelled(true);
                return;
            }

            if (edeDamager instanceof LivingEntity) {
                if (cause == DamageCause.ENTITY_SWEEP_ATTACK && EngineCfg.COMBAT_DISABLE_VANILLA_SWEEP) {
                    ede.setCancelled(true);
                    return;
                }

                damager = (LivingEntity) edeDamager;

                meta.setDamager(damager);
                statsDamager = EntityStats.get(damager);
                statsDamager.setLastDamageMeta(meta);
                weapon = statsDamager.getItemInMainHand();

                if (ItemUtils.isBow(weapon) && !EngineCfg.COMBAT_BOWS_DO_FULL_MELEE_DAMAGE) {
                    break labelFullDamage;
                }
            } else if (edeDamager instanceof Projectile) {
                projectile = (Projectile) edeDamager;
                ProjectileSource shooter = projectile.getShooter();

                if (!(shooter instanceof LivingEntity)) {
                    break labelFullDamage;
                }

                // Fix extra damage from ender pearls
                if (projectile instanceof EnderPearl && shooter.equals(victim)) {
                    break labelFullDamage;
                }

                damager = (LivingEntity) shooter;
                meta.setDamager(damager);
                statsDamager = EntityStats.get(damager);
                statsDamager.setLastDamageMeta(meta);
                weapon = ProjectileStats.getSrcWeapon(projectile);

                // Applying custom arrow object to damage meta,
                // if it's present, to use additional stats from it.
                ArrowManager arr = plugin.getModuleCache().getArrowManager();
                if (arr != null) {
                    QArrow arrow = arr.getArrow(projectile);
                    int    level = arr.getArrowLevel(projectile);
                    meta.setArrow(arrow, level);
                    if (arrow != null) {
                        statsDamager.setArrowBonus(arrow, level);
                        statsVictim.setArrowBonus(arrow, level);
                    }
                }

                // Anti-weapon damage bug, when shot was from a bow,
                // but user swap his weapon to replace bow stats/damage.
                if (weapon != null && !weapon.isSimilar(statsDamager.getItemInMainHand())) {
                    damageStart = 1D;
                    break labelFullDamage;
                }

                // Applying attack power multiplier depends on
                // projectile launch power for proper damage values.
                double power = ProjectileStats.getPower(projectile);
                statsDamager.setAttackPower(power);


            }
            // If label not broken, then we have to use all damager damage attributes
            isFullDamage = statsDamager != null;
        }
        meta.setWeapon(weapon);

        boolean exempt = skillApi != null && damager != null && skillApi.isExempt(damager);

        // +----------------------------------------------------+
        // | Get all DamageAttribute values of the damager.     |
        // | For projectiles we're use the meta with saved data.|
        // | For melee damage we're use damager's stats for full|
        // | damage, or a default attribute with event damage.  |
        // +----------------------------------------------------+
        final Map<DamageAttribute, Double>   damages  = new HashMap<>();
        final Map<AbstractStat.Type, Double> stats    = new HashMap<>();
        final Map<DefenseAttribute, Double>  defenses = new HashMap<>();

        // Pre-cache damager damage types.
        if (isFullDamage && statsDamager != null && !exempt) {
            damages.putAll(statsDamager.getDamageTypes(false));
        } else {
            DamageAttribute dmgCause = ItemStats.getDamageByCause(cause);
            if (dmgCause == null) dmgCause = ItemStats.getDamageByDefault();
            damages.put(dmgCause, damageStart);
        }

        if (statsDamager != null) {
            stats.putAll(statsDamager.getItemStats(false));
        }

        defenses.putAll(statsVictim.getDefenseTypes(false));

        // +----------------------------------------------------+
        // | Make 'damageStart' to be only additional damage,   |
        // | that is added to the default hand/weapon damage by |
        // | AttributeModifiers and such other things.          |
        // +----------------------------------------------------+
        if (statsDamager != null && !exempt) {
            // Deduct vanilla weapon or hand damage value.
            if (weapon != null && !ItemUT.isAir(weapon)) {
                double defaultDamage = DamageAttribute.getVanillaDamage(weapon);
                long countCustomDamage = damages.keySet().stream()
                        .filter(att -> {
                            DamageAttribute def = ItemStats.getDamageByDefault();
                            // Heh... well. If we have a damage type that's doing more than 1 damage and is not the
                            // default damage type and is doing more than 1 damage (since 1 is sort of our hard-coded
                            // default), then we can assume that this damage is intended to override the vanilla damage.
                            return !att.equals(def) || (att.equals(def) && damages.get(att) != 1);
                        }).count();
                if (projectile != null && countCustomDamage > 0) {
                    // If it's a projectile, the NMS for default damage doesn't work. so we'll just assume that the
                    // event damage is the default.
                    defaultDamage = e.getDamage();
                }

//                QuantumRPG.getInstance().getLogger().info("Default damage is " + defaultDamage);
                damageStart = Math.max(0D, damageStart - defaultDamage);
            } else {
                damageStart = damageStart - 1D; // Reduce the damage by 1 for non-weapon item/hand.
            }
            // Probably can't be lower than 1, but anyway xD
            damageStart /= Math.max(1D, damages.size());
        } else {
            damageStart = 0D;
        }

        // +----------------------------------------------------+
        // | Add additional damage to all damager's attributes. |
        // +----------------------------------------------------+
        final double damageStart2 = damageStart;
        damages.keySet().forEach((dmgAtt) -> damages.compute(dmgAtt, (dmgKey, dmgVal) -> dmgVal + damageStart2));

        RPGDamageEvent.Start eventStart = new RPGDamageEvent.Start(victim, damager, projectile, damages, defenses,
                stats, e, meta, exempt);
        plugin.getPluginManager().callEvent(eventStart);
        if (eventStart.isCancelled() || e.isCancelled()) {
//            QuantumRPG.getInstance().info("Damage event was cancelled.");
            return;
        }

        // Remove arrow bonus added above after we got all the stats.
        // We're doing it here to allow arrow bonus affect victim's stats.
        if (statsDamager != null) statsDamager.setArrowBonus(null, 0);
        statsVictim.setArrowBonus(null, 0);

        // +----------------------------------------------------+
        // | Fix final damage value of the vanilla damage event.|
        // +----------------------------------------------------+
//        QuantumRPG.getInstance().info("Damage Final Check: " + e.getFinalDamage() + "/" + e.getDamage());
        if (e.getFinalDamage() != e.getDamage()) {
            double absorption = Math.min(e.getDamage(), victim.getAbsorptionAmount());
            for (DamageModifier dmgModifier : DamageModifier.values()) {
                if (dmgModifier == DamageModifier.ABSORPTION) continue;
                if (e.isApplicable(dmgModifier)) {
                    if (dmgModifier == DamageModifier.BASE) {
//                        QuantumRPG.getInstance().info("FINAL - " + dmgModifier.name() + ": " + e.getDamage());
                        e.setDamage(dmgModifier, e.getDamage() - absorption);
                    } else if (dmgModifier == DamageModifier.ABSORPTION) {
                        e.setDamage(dmgModifier, absorption);
                    } else e.setDamage(dmgModifier, 0); // Fix
                }
            }
        }

//        QuantumRPG.getInstance().info("event took: " + (System.currentTimeMillis() - l1) + " millis");
    }
}
