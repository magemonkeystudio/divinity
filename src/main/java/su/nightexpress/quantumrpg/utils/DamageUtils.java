package su.nightexpress.quantumrpg.utils;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.api.DivineItemsAPI;
import su.nightexpress.quantumrpg.api.EntityAPI;
import su.nightexpress.quantumrpg.api.ItemAPI;
import su.nightexpress.quantumrpg.config.Config;
import su.nightexpress.quantumrpg.hooks.HookUtils;
import su.nightexpress.quantumrpg.listeners.DamageMeta;
import su.nightexpress.quantumrpg.modules.arrows.ArrowManager;
import su.nightexpress.quantumrpg.stats.DisarmRateSettings;
import su.nightexpress.quantumrpg.stats.ItemStat;
import su.nightexpress.quantumrpg.types.ArmorType;
import su.nightexpress.quantumrpg.types.DamageType;

import java.util.Map;

public class DamageUtils {
    private static QuantumRPG plugin = QuantumRPG.instance;

    private static FixedMetadataValue metadata = new FixedMetadataValue((Plugin) plugin, "AOE_FIX");

    public static double procDmg(final LivingEntity zertva, final LivingEntity damager, double base, ItemStack item, EntityDamageByEntityEvent e, DamageMeta meta) {
        final ArrowManager.QArrow da = meta.getArrow();
        double dodge = EntityAPI.getItemStat(zertva, ItemStat.DODGE_RATE, da);
        if (dodge > 0.0D) {
            double dodge_rate = Utils.getRandDouble(0.0D, 100.0D);
            double accur = EntityAPI.getItemStat(damager, ItemStat.ACCURACY_RATE, da);
            if (dodge_rate <= dodge && Utils.getRandDouble(0.0D, 100.0D) > accur) {
                e.setDamage(0.0D);
                e.setCancelled(true);
                meta.setDodge(true);
                return 0.0D;
            }
        }
        if (e.isApplicable(EntityDamageEvent.DamageModifier.ARMOR))
            e.setDamage(EntityDamageEvent.DamageModifier.ARMOR, 0.0D);
        double disarm = EntityAPI.getItemStat(damager, ItemStat.DISARM_RATE, da);
        if (disarm > 0.0D && Utils.getRandDouble(0.0D, 100.0D) <= disarm) {
            boolean main = true;
            ItemStack hand = zertva.getEquipment().getItemInMainHand();
            if (hand == null || hand.getType() == Material.AIR) {
                hand = zertva.getEquipment().getItemInOffHand();
                main = false;
            }
            if (hand != null && hand.getType() != Material.AIR) {
                if (main) {
                    zertva.getEquipment().setItemInMainHand(new ItemStack(Material.AIR));
                } else {
                    zertva.getEquipment().setItemInOffHand(new ItemStack(Material.AIR));
                }
                zertva.getWorld().dropItemNaturally(zertva.getLocation(), hand).setPickupDelay(40);
                DisarmRateSettings drs = (DisarmRateSettings) ItemStat.DISARM_RATE.getSettings();
                Utils.playEffect(drs.getEffect(), zertva.getLocation(), 0.2F, 0.4F, 0.2F, 0.1F, 25);
                damager.sendMessage(drs.getMsgToDamager().replace("%s%", Utils.getEntityName((Entity) zertva)));
                zertva.sendMessage(drs.getMsgToEntity().replace("%s%", Utils.getEntityName((Entity) damager)));
            }
        }
        double burn = EntityAPI.getItemStat(damager, ItemStat.BURN_RATE, da);
        if (burn > 0.0D && Utils.getRandDouble(0.0D, 100.0D) <= burn)
            zertva.setFireTicks(100);
        double crit_modifier = 1.0D;
        double crit_rate = EntityAPI.getItemStat(damager, ItemStat.CRITICAL_RATE, da);
        if (crit_rate > 0.0D && Utils.getRandDouble(0.0D, 100.0D) < crit_rate) {
            crit_modifier = EntityAPI.getItemStat(damager, ItemStat.CRITICAL_DAMAGE, da);
            if (crit_modifier == 0.0D) {
                crit_modifier = 1.0D;
            } else {
                meta.setCritical(true);
            }
        }
        if (item != null && item.getType() != Material.AIR) {
            base = Math.max(0.0D, base - ItemAPI.getDefaultDamage(item));
        } else {
            base--;
        }
        ItemStat pvptype = ItemStat.PVE_DEFENSE;
        ItemStat pvptype2 = ItemStat.PVE_DAMAGE;
        if (zertva instanceof Player && damager instanceof Player) {
            pvptype = ItemStat.PVP_DEFENSE;
            pvptype2 = ItemStat.PVP_DAMAGE;
        }
        double direct_procent = EntityAPI.getItemStat(damager, ItemStat.DIRECT_DAMAGE, da) / 100.0D;
        double direct_dmg = 0.0D;
        double penet = EntityAPI.getItemStat(damager, ItemStat.PENETRATION, da);
        double pvpe_def = EntityAPI.getItemStat(zertva, pvptype, da);
        double pvpe_dmg = EntityAPI.getItemStat(damager, pvptype2, da);
        meta.setPvPEDamage(pvpe_dmg);
        meta.setPvPEDefense(pvpe_def);
        Map<DamageType, Double> damages = EntityAPI.getDamageTypes(damager, item, da);
        Map<ArmorType, Double> armors = EntityAPI.getDefenseTypes(zertva, da);
        String total_all = "";
        double dmg_by_perc = MetaUtils.getDamagePercent(zertva);
        double dmg_additional = MetaUtils.getDamageAdditional(zertva);
        base /= Math.max(1, damages.size());
        for (DamageType dt : damages.keySet()) {
            String formula_dmg_type = "";
            double d = ((Double) damages.get(dt)).doubleValue();
            d += base;
            d += dmg_additional;
            d *= dmg_by_perc;
            if (damager instanceof Player)
                d = fineDamageByCooldown((Player) damager, d);
            double direct2 = d * direct_procent;
            d = Math.max(0.0D, d - direct2);
            direct_dmg += direct2;
            for (ArmorType at : armors.keySet()) {
                double def = 0.0D;
                if (at.getBlockDamageTypes().contains(dt.getId()))
                    def = ((Double) armors.get(at)).doubleValue();
                double def_ench = Math.min(20.0D, EntityAPI.getEnchantedDefense((Entity) damager, zertva));
                d *= 1.0D - def_ench / 25.0D;
                def = Math.max(0.0D, def * (1.0D - penet / 100.0D));
                String f1 = at.getFormula()
                        .replace("%crit%", String.valueOf(crit_modifier))
                        .replace("%def%", String.valueOf(def))
                        .replace("%dmg%", String.valueOf(d))
                        .replace("%penetrate%", "0");
                formula_dmg_type = String.valueOf(formula_dmg_type) + "(" + d + " - " + f1 + ") + ";
            }
            if (formula_dmg_type.isEmpty())
                formula_dmg_type = "0";
            if (formula_dmg_type.length() > 3)
                formula_dmg_type = formula_dmg_type.substring(0, formula_dmg_type.length() - 2).trim();
            formula_dmg_type = String.valueOf(d) + " - (" + formula_dmg_type + ")";
            double d_type = eval(formula_dmg_type) * crit_modifier;
            total_all = String.valueOf(total_all) + "(" + d_type + ") + ";
            meta.setDamageType(dt, d_type + direct2);
            for (String act : dt.getActions()) {
                if (act.contains("[DAMAGE]"))
                    dt.getActions().remove(act);
            }
            DivineItemsAPI.executeActions((Entity) damager, dt.getActions(), item);
        }
        if (total_all.length() > 3)
            total_all = total_all.substring(0, total_all.length() - 2).trim();
        double block_dmg = EntityAPI.getItemStat(zertva, ItemStat.BLOCK_DAMAGE, da);
        double block_rate = EntityAPI.getItemStat(zertva, ItemStat.BLOCK_RATE, da);
        double b_mod = 1.0D;
        if (block_rate > 0.0D && Utils.getRandDouble(0.0D, 100.0D) <= block_rate) {
            b_mod = 0.0D;
            meta.setBlocked(true);
            meta.setDamageBlocked(block_dmg);
            if (zertva instanceof Player) {
                Player p = (Player) zertva;
                p.setCooldown(Material.SHIELD, 20 * Config.g_combat_shield_cd);
            }
        }
        if (e.isApplicable(EntityDamageEvent.DamageModifier.BLOCKING))
            e.setDamage(EntityDamageEvent.DamageModifier.BLOCKING, b_mod);
        String formula_total = Config.getDamageFormula()
                .replace("%dmg%", total_all)
                .replace("%pvpe_dmg%", String.valueOf(pvpe_dmg))
                .replace("%pvpe_def%", String.valueOf(pvpe_def))
                .replace("%crit%", String.valueOf(crit_modifier))
                .replace("%block%", String.valueOf(block_dmg));
        final double d_total = Math.max(0.0D, direct_dmg + eval(formula_total));
        if (d_total == 0.0D)
            return d_total;
        final double aoe = EntityAPI.getItemStat(damager, ItemStat.AOE_DAMAGE, da);
        if (aoe > 0.0D)
            if (zertva.hasMetadata("AOE_FIX")) {
                zertva.removeMetadata("AOE_FIX", (Plugin) plugin);
            } else {
                (new BukkitRunnable() {
                    public void run() {
                        double range = EntityAPI.getItemStat(damager, ItemStat.RANGE, da);
                        if (range <= 0.0D)
                            range = 3.0D;
                        for (Entity ee : zertva.getNearbyEntities(range, range, range)) {
                            if (!(ee instanceof LivingEntity))
                                continue;
                            LivingEntity li3 = (LivingEntity) ee;
                            if (!HookUtils.canFights((Entity) damager, (Entity) li3))
                                continue;
                            li3.setMetadata("AOE_FIX", (MetadataValue) DamageUtils.metadata);
                            EntityAPI.damageWithPercent(li3, damager, d_total, aoe);
                        }
                    }
                }).runTask((Plugin) plugin);
            }
        double bleed = EntityAPI.getItemStat(damager, ItemStat.BLEED_RATE, da);
        if (bleed > 0.0D && Utils.getRandDouble(0.0D, 100.0D) <= bleed)
            plugin.getTM().addBleedEffect(zertva, d_total);
        double thorn = EntityAPI.getItemStat(zertva, ItemStat.THORNMAIL, da) / 100.0D;
        if (thorn > 0.0D)
            damager.damage(d_total * thorn);
        return d_total;
    }

    private static double fineDamageByCooldown(Player p, double dmg) {
        double power = getAttackCooldown(p);
        if (power < 1.0D) {
            double re = 1.0D - Config.getDamageCDReduce();
            if (re != 1.0D) {
                dmg *= re;
            } else {
                dmg *= power;
            }
        }
        return dmg;
    }

    private static double getAttackCooldown(Player p) {
        return MetaUtils.getAtkPower(p);
    }

    public static double eval(final String str) {
        return (new Object() {
            int pos = -1;

            int ch;

            void nextChar() {
                this.ch = (++this.pos < str.length()) ? str.charAt(this.pos) : -1;
            }

            boolean eat(int charToEat) {
                for (; this.ch == 32; nextChar()) ;
                if (this.ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (this.pos < str.length())
                    throw new RuntimeException("Unexpected: " + (char) this.ch);
                return x;
            }

            double parseExpression() {
                double x = parseTerm();
                while (true) {
                    for (; eat(43); x += parseTerm()) ;
                    if (eat(45)) {
                        x -= parseTerm();
                        continue;
                    }
                    return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                while (true) {
                    for (; eat(42); x *= parseFactor()) ;
                    if (eat(47)) {
                        x /= parseFactor();
                        continue;
                    }
                    return x;
                }
            }

            double parseFactor() {
                double x;
                if (eat(43))
                    return parseFactor();
                if (eat(45))
                    return -parseFactor();
                int startPos = this.pos;
                if (eat(40)) {
                    x = parseExpression();
                    eat(41);
                } else {
                    if ((this.ch >= 48 && this.ch <= 57) || this.ch == 46)
                        while (true) {
                            if ((this.ch < 48 || this.ch > 57) && this.ch != 46) {
                                x = Double.parseDouble(str.substring(startPos, this.pos));
                            } else {
                                nextChar();
                                continue;
                            }
                            if (eat(94))
                                x = Math.pow(x, parseFactor());
                            return x;
                        }
                    if (this.ch >= 97 && this.ch <= 122) {
                        for (; this.ch >= 97 && this.ch <= 122; nextChar()) ;
                        String func = str.substring(startPos, this.pos);
                        x = parseFactor();
                        if (func.equals("sqrt")) {
                            x = Math.sqrt(x);
                        } else if (func.equals("sin")) {
                            x = Math.sin(Math.toRadians(x));
                        } else if (func.equals("cos")) {
                            x = Math.cos(Math.toRadians(x));
                        } else if (func.equals("tan")) {
                            x = Math.tan(Math.toRadians(x));
                        } else {
                            throw new RuntimeException("Unknown function: " + func);
                        }
                    } else {
                        throw new RuntimeException("Unexpected: " + (char) this.ch);
                    }
                }
                if (eat(94))
                    x = Math.pow(x, parseFactor());
                return x;
            }
        }).parse();
    }
}
