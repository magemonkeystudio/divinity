package su.nightexpress.quantumrpg.api;

import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.hooks.EHook;
import su.nightexpress.quantumrpg.hooks.external.MythicMobsHook;
import su.nightexpress.quantumrpg.modules.buffs.BuffManager;
import su.nightexpress.quantumrpg.types.ActionType;
import su.nightexpress.quantumrpg.types.SpellType;
import su.nightexpress.quantumrpg.types.TargetType;
import su.nightexpress.quantumrpg.utils.ParticleUtils;
import su.nightexpress.quantumrpg.utils.Spells;
import su.nightexpress.quantumrpg.utils.Utils;
import su.nightexpress.quantumrpg.utils.msg.MsgUT;

import java.util.ArrayList;
import java.util.List;

public class DivineItemsAPI {
    private static final QuantumRPG plugin = QuantumRPG.instance;

    private static ItemAPI ia;

    private static EntityAPI ea;

    public static ItemAPI getItemAPI() {
        return ia;
    }

    public static EntityAPI getEntityAPI() {
        return ea;
    }

    public static void executeActions(final Entity executor, List<String> list, final ItemStack item) {
        final List<String> act = new ArrayList<>(list);
        for (String s : list) {
            LivingEntity livingEntity = null;
            int pos = act.indexOf(s);
            List<Entity> tlist = new ArrayList<>();
            if (EHook.PLACEHOLDER_API.isEnabled() && executor instanceof Player)
                s = PlaceholderAPI.setPlaceholders((Player) executor, s);
            String b = s.split(" ")[0];
            b = b.replace("[", "").replace("]", "");
            ActionType at = null;
            try {
                at = ActionType.valueOf(b);
            } catch (IllegalArgumentException ex) {
                continue;
            }
            Entity target = executor;
            TargetType tt = TargetType.SELF;
            double range = 0.0D;
            double chance = 100.0D;
            String[] values_0 = s.split(" ");
            List<String> values_1 = new ArrayList<>();
            String rep = "";
            byte b1;
            int i;
            String[] arrayOfString1;
            for (i = (arrayOfString1 = values_0).length, b1 = 0; b1 < i; ) {
                String s5 = arrayOfString1[b1];
                if (s5.startsWith("@")) {
                    if (s5.contains("-")) {
                        range = Double.parseDouble(s5.split("-")[1]);
                        s5 = s5.split("-")[0];
                    }
                    try {
                        tt = TargetType.valueOf(s5.replace("@", "").toUpperCase());
                        rep = s5;
                    } catch (IllegalArgumentException illegalArgumentException) {
                    }
                } else if (s5.startsWith("%")) {
                    try {
                        chance = Double.parseDouble(s5.replace("%", ""));
                    } catch (NumberFormatException numberFormatException) {
                    }
                } else {
                    values_1.add(s5);
                }
                b1++;
            }
            if (Utils.getRandDouble(0.0D, 100.0D) > chance)
                continue;
            String[] values = values_1.toArray(new String[values_1.size()]);
            if (tt == null)
                tlist = executor.getNearbyEntities(range, range, range);
            else
                switch (tt) {
                    case SELF:
                        target = executor;
                        break;
                    case TARGET:
                        if (executor.hasMetadata("DI_TARGET")) {
                            target = (Entity) executor.getMetadata("DI_TARGET").get(0).value();
                            break;
                        }
                        livingEntity = EntityAPI.getTarget(executor);
                        break;
                }
            if (livingEntity == null)
                return;
            if (tlist.isEmpty())
                tlist.add(livingEntity);
            for (Entity target2 : tlist) {
                Player player3;
                String str3;
                Player player2;
                String skill;
                PotionEffectType pe;
                String str2;
                Player p;
                String world;
                Player player1;
                String str1;
                Player p2;
                String cmd;
                LivingEntity livingEntity1;
                double d5;
                Location localLocation1;
                LivingEntity li;
                double d4;
                BuffManager.BuffType type;
                double x;
                SpellType spellType;
                MythicMobsHook mm;
                int lvl;
                String str6;
                World w;
                String str5, tit;
                Sound s1;
                double speed;
                Location localLocation2;
                String str4;
                double amount;
                String name;
                int time;
                double d7;
                String sub;
                Vector localVector;
                double d6;
                String title;
                double mod, y;
                PotionEffect pp;
                int show;
                Arrow arrow;
                Fireball fireball;
                WitherSkull fb;
                double d1;
                String ch;
                double d8;
                int stay;
                Vector vv;
                int j;
                String c1;
                double sec, z;
                int end;
                double d2;
                String c2;
                double d9, d10, d3;
                Location l;
                int k;
                Location loc;
                Entity entity1 = target2;
                if (at == null) {
                    if (!(entity1 instanceof Player))
                        continue;
                    p2 = (Player) entity1;
                    str5 = ChatColor.translateAlternateColorCodes('&', s.replace(rep, "").replaceFirst("\\[", "").replaceFirst("\\]", "").replace(at.name() + " ", "").replace("%p", executor.getName()).replace("%t", entity1.getName()));
                    QuantumRPG.instance.getNMS().sendActionBar(p2, str5);
                } else {
                    switch (at) {
                        case BUFF:
                            if (!(entity1 instanceof Player))
                                continue;
                            player3 = (Player) entity1;
                            type = null;
                            try {
                                type = BuffManager.BuffType.valueOf(values[1].toUpperCase());
                            } catch (IllegalArgumentException ex) {
                                continue;
                            }
                            name = values[2];
                            mod = 0.0D;
                            sec = 0.0D;
                            try {
                                String v1 = values[3];
                                String v2 = values[4];
                                mod = Double.parseDouble(v1);
                                sec = Double.parseDouble(v2);
                            } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
                                continue;
                            }
                            plugin.getMM().getModule(BuffManager.class).addBuff(player3, type, name, mod, (int) sec, true);
                            break;
                        case EFFECT:
                            str3 = values[1].toUpperCase();
                            x = 0.0D;
                            y = 0.0D;
                            z = 0.0D;
                            d10 = 0.0D;
                            k = 0;
                            try {
                                x = Double.parseDouble(values[2]);
                                y = Double.parseDouble(values[3]);
                                z = Double.parseDouble(values[4]);
                                d10 = Double.parseDouble(values[5]);
                                k = (int) Double.parseDouble(values[6]);
                            } catch (NumberFormatException ex) {
                                continue;
                            }
                            if (entity1 instanceof LivingEntity) {
                                loc = ((LivingEntity) entity1).getEyeLocation();
                            } else {
                                loc = entity1.getLocation();
                            }
                            Utils.playEffect(str3, loc, (float) x, (float) y, (float) z, (float) d10, k);
                            break;
                        case SPELL:
                            if (!(entity1 instanceof Player))
                                continue;
                            player2 = (Player) entity1;
                            spellType = null;
                            try {
                                spellType = SpellType.valueOf(values[1].toUpperCase());
                            } catch (IllegalArgumentException ex) {
                                continue;
                            }
                            if (spellType == null)
                                Spells.skillIceFireStorm(player2, 1);
                            else
                                switch (spellType) {
                                    case ICE_SNAKE:
                                        Spells.skillIceSnake(player2);
                                        break;
                                    case METEOR:
                                        Spells.skillMeteor(player2);
                                        break;
                                    case ICE_STORM:
                                        Spells.skillIceFireStorm(player2, 0);
                                        break;
                                }
                            break;
                        case MYTHIC_SKILL:
                            if (!EHook.MYTHIC_MOBS.isEnabled())
                                continue;
                            skill = values[1];
                            mm = (MythicMobsHook) EHook.MYTHIC_MOBS.getHook();
                            mm.castSkill(executor, skill);
                            break;
                        case POTION:
                            if (!(entity1 instanceof LivingEntity))
                                continue;
                            pe = PotionEffectType.getByName(values[1].toUpperCase());
                            if (pe == null)
                                continue;
                            lvl = 0;
                            time = 0;
                            try {
                                lvl = (int) Double.parseDouble(values[2]);
                                time = (int) Double.parseDouble(values[3]);
                            } catch (NumberFormatException ex) {
                                continue;
                            }
                            if (lvl < 0)
                                continue;
                            pp = new PotionEffect(pe, time * 20, lvl - 1);
                            ((LivingEntity) entity1).removePotionEffect(pe);
                            ((LivingEntity) entity1).addPotionEffect(pp);
                            break;
                        case COMMAND:
                            if (!(entity1 instanceof Player))
                                continue;
                            str2 = s.replace(rep, "").replace("[", "").replace("]", "").replace(at.name() + " ", "").replace("%p", executor.getName()).replace("%t", entity1.getName());
                            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), str2);
                            break;
                        case OP_COMMAND:
                            if (!(entity1 instanceof Player))
                                continue;
                            p = (Player) entity1;
                            str6 = s.replace(rep, "").replace("[", "").replace("]", "").replace(at.name() + " ", "").replace("%p", executor.getName()).replace("%t", entity1.getName());
                            p.setOp(true);
                            plugin.getServer().dispatchCommand(p, str6);
                            p.setOp(false);
                            break;
                        case PLAYER_COMMAND:
                            if (!(entity1 instanceof Player))
                                continue;
                            p = (Player) entity1;
                            str6 = s.replace(rep, "").replace("[", "").replace("]", "").replace(at.name() + " ", "").replace("%p", executor.getName()).replace("%t", entity1.getName());
                            p.performCommand(str6);
                            break;
                        case TELEPORT:
                            world = values[1];
                            w = plugin.getServer().getWorld(world);
                            if (w == null)
                                continue;
                            d7 = 0.0D;
                            d8 = 0.0D;
                            d9 = 0.0D;
                            try {
                                d7 = Double.parseDouble(values[2]);
                                d8 = Double.parseDouble(values[3]);
                                d9 = Double.parseDouble(values[4]);
                            } catch (NumberFormatException ex) {
                                continue;
                            }
                            l = new Location(w, d7, d8, d9);
                            entity1.teleport(l);
                            break;
                        case MESSAGE:
                            if (!(entity1 instanceof Player))
                                continue;
                            player1 = (Player) entity1;
                            str5 = ChatColor.translateAlternateColorCodes('&', s.replace(rep, "").replaceFirst("\\[", "").replaceFirst("\\]", "").replace(at.name() + " ", "").replace("%p", executor.getName()).replace("%t", entity1.getName()));
                            player1.sendMessage(str5);
                            break;
                        case BROADCAST:
                            str1 = ChatColor.translateAlternateColorCodes('&', s.replace(rep, "").replaceFirst("\\[", "").replaceFirst("\\]", "").replace(at.name() + " ", "").replace("%p", executor.getName()).replace("%t", entity1.getName()));
                            plugin.getServer().broadcastMessage(str1);
                            break;
                        case TITLES:
                            if (!(entity1 instanceof Player))
                                continue;
                            p2 = (Player) entity1;
                            tit = ChatColor.translateAlternateColorCodes('&', values[1].replace("_", " "));
                            sub = ChatColor.translateAlternateColorCodes('&', values[2].replace("_", " "));
                            show = 0;
                            stay = 0;
                            end = 0;
                            try {
                                show = (int) Double.parseDouble(values[3]);
                                stay = (int) Double.parseDouble(values[4]);
                                end = (int) Double.parseDouble(values[5]);
                            } catch (NumberFormatException ex) {
                                continue;
                            }
                            plugin.getNMS().sendTitles(p2, tit, sub, show, stay, end);
                            break;
                        case SOUND:
                            cmd = values[1];
                            s1 = null;
                            try {
                                s1 = Sound.valueOf(cmd);
                            } catch (NullPointerException ex) {
                                continue;
                            }
                            executor.getWorld().playSound(entity1.getLocation(), s1, 0.6F, 0.6F);
                            break;
                        case ARROW:
                            if (!(entity1 instanceof LivingEntity))
                                continue;
                            livingEntity1 = (LivingEntity) entity1;
                            speed = Double.parseDouble(values[1]);
                            arrow = livingEntity1.launchProjectile(Arrow.class);
                            vv = livingEntity1.getEyeLocation().getDirection();
                            arrow.setShooter(livingEntity1);
                            arrow.setVelocity(vv.multiply(speed));
                            break;
                        case FIREBALL:
                            if (!(entity1 instanceof LivingEntity))
                                continue;
                            livingEntity1 = (LivingEntity) entity1;
                            speed = Double.parseDouble(values[1]);
                            fireball = livingEntity1.launchProjectile(Fireball.class);
                            vv = livingEntity1.getEyeLocation().getDirection();
                            fireball.setShooter(livingEntity1);
                            fireball.setVelocity(vv.multiply(speed));
                            break;
                        case WITHER_SKULL:
                            if (!(entity1 instanceof LivingEntity))
                                continue;
                            livingEntity1 = (LivingEntity) entity1;
                            speed = Double.parseDouble(values[1]);
                            fb = livingEntity1.launchProjectile(WitherSkull.class);
                            vv = livingEntity1.getEyeLocation().getDirection();
                            fb.setShooter(livingEntity1);
                            fb.setVelocity(vv.multiply(speed));
                            break;
                        case IGNITE:
                            d5 = 0.0D;
                            try {
                                d5 = Double.parseDouble(values[1]);
                            } catch (NumberFormatException ex) {
                                continue;
                            }
                            entity1.setFireTicks((int) d5 * 20);
                            break;
                        case LIGHTNING:
                            if (entity1.equals(executor))
                                continue;
                            entity1.getWorld().strikeLightning(entity1.getLocation());
                            break;
                        case THROW:
                            if (entity1.equals(executor))
                                continue;
                            localLocation1 = entity1.getLocation();
                            localLocation2 = localLocation1.subtract(executor.getLocation());
                            localVector = localLocation2.getDirection().normalize().multiply(-1.4D);
                            if (localVector.getY() >= 1.15D) {
                                localVector.setY(localVector.getY() * 0.45D);
                            } else if (localVector.getY() >= 1.0D) {
                                localVector.setY(localVector.getY() * 0.6D);
                            } else if (localVector.getY() >= 0.8D) {
                                localVector.setY(localVector.getY() * 0.85D);
                            }
                            if (localVector.getY() <= 0.0D)
                                localVector.setY(-localVector.getY() + 0.3D);
                            if (Math.abs(localLocation2.getX()) <= 1.0D)
                                localVector.setX(localVector.getX() * 1.2D);
                            if (Math.abs(localLocation2.getZ()) <= 1.0D)
                                localVector.setZ(localVector.getZ() * 1.2D);
                            d1 = localVector.getX() * 2.0D;
                            d2 = localVector.getY() * 2.0D;
                            d3 = localVector.getZ() * 2.0D;
                            if (d1 >= 3.0D)
                                d1 *= 0.5D;
                            if (d2 >= 3.0D)
                                d2 *= 0.5D;
                            if (d3 >= 3.0D)
                                d3 *= 0.5D;
                            localVector.setX(d1);
                            localVector.setY(d2);
                            localVector.setZ(d3);
                            entity1.setVelocity(localVector);
                            break;
                        case HOOK:
                            if (entity1.equals(executor))
                                continue;
                            localLocation1 = entity1.getLocation();
                            localLocation2 = localLocation1.subtract(executor.getLocation());
                            localVector = localLocation2.getDirection().normalize().multiply(-1.4D);
                            if (localVector.getY() >= 1.15D) {
                                localVector.setY(localVector.getY() * 0.45D);
                            } else if (localVector.getY() >= 1.0D) {
                                localVector.setY(localVector.getY() * 0.6D);
                            } else if (localVector.getY() >= 0.8D) {
                                localVector.setY(localVector.getY() * 0.85D);
                            }
                            if (localVector.getY() <= 0.0D)
                                localVector.setY(-localVector.getY() + 0.3D);
                            if (Math.abs(localLocation2.getX()) <= 1.0D)
                                localVector.setX(localVector.getX() * 1.2D);
                            if (Math.abs(localLocation2.getZ()) <= 1.0D)
                                localVector.setZ(localVector.getZ() * 1.2D);
                            d1 = localVector.getX() * -2.0D;
                            d2 = localVector.getY() * -2.0D;
                            d3 = localVector.getZ() * -2.0D;
                            if (d1 >= -3.0D)
                                d1 *= -0.5D;
                            if (d2 >= -3.0D)
                                d2 *= -0.5D;
                            if (d3 >= -3.0D)
                                d3 *= -0.5D;
                            localVector.setX(d1);
                            localVector.setY(d2);
                            localVector.setZ(d3);
                            entity1.setVelocity(localVector);
                            break;
                        case FIREWORK:
                            Utils.spawnRandomFirework(entity1.getLocation());
                            break;
                        case PARTICLE_LINE:
                            if (!(entity1 instanceof LivingEntity))
                                continue;
                            li = (LivingEntity) entity1;
                            if (entity1.equals(executor))
                                continue;
                            str4 = values[1].toUpperCase();
                            d6 = 0.0D;
                            j = 0;
                            try {
                                d6 = Double.parseDouble(values[2]);
                                j = (int) Double.parseDouble(values[3]);
                            } catch (NumberFormatException ex) {
                                continue;
                            }
                            ParticleUtils.drawParticleLine((LivingEntity) executor, li, str4, (float) d6, j);
                            break;
                        case PARTICLE_PULSE:
                            ParticleUtils.wave(entity1.getLocation());
                            break;
                        case DAMAGE:
                            if (!(entity1 instanceof LivingEntity))
                                continue;
                            li = (LivingEntity) entity1;
                            amount = 0.0D;
                            try {
                                amount = Double.parseDouble(values[1]);
                            } catch (NumberFormatException ex) {
                                continue;
                            }
                            if (executor instanceof Projectile) {
                                Projectile pj = (Projectile) executor;
                                if (pj.getShooter() != null && pj.getShooter() instanceof LivingEntity) {
                                    LivingEntity ex = (LivingEntity) pj.getShooter();
                                    li.damage(amount, ex);
                                    continue;
                                }
                            }
                            li.damage(amount, executor);
                            break;
                        case DELAY:
                            d4 = 0.0D;
                            try {
                                d4 = Double.parseDouble(values[1]);
                            } catch (NumberFormatException ex) {
                                continue;
                            }
                            act.remove(s);
                            for (String ss : act)
                                System.out.println(ss);
                            new BukkitRunnable() {
                                public void run() {
                                    DivineItemsAPI.executeActions(executor, act, item);
                                }
                            }.runTaskLater(plugin, (int) d4);
                            break;
                        case ANIM_DELAY:
                            if (!(executor instanceof Player))
                                return;
                            d4 = 0.0D;
                            try {
                                d4 = Double.parseDouble(values[1]);
                            } catch (NumberFormatException ex) {
                                continue;
                            }
                            title = values[2];
                            ch = values[3];
                            c1 = values[4];
                            c2 = values[5];
                            act.remove(pos);
                            playAnimDelay((Player) executor, (int) d4, title, ch, c1, c2, act, item);
                            break;
                    }
                }
            }
            act.remove(s);
        }
    }

    public static void playAnimDelay(Player p, int time, String title, String ch, String c1, String c2, List<String> acts, ItemStack item) {
        double pers = (20 / time);
        double step = 20.0D / pers;
        AnimDelay a = new AnimDelay(p, (int) step, title, ch, c1, c2, acts, item);
        a.runTaskTimer(plugin, 0L, (int) step);
    }

    public static class AnimDelay extends BukkitRunnable {
        private final ItemStack item;

        private final List<String> acts;

        private final Player p;
        private final int step;
        private final String title;
        private final String c1;
        private final String c2;
        private final String bar;
        private int i;

        public AnimDelay(Player p, int step, String title, String ch, String c1, String c2, List<String> acts, ItemStack item) {
            this.i = 0;
            this.p = p;
            this.step = step;
            this.title = ChatColor.translateAlternateColorCodes('&', title).replace("_", " ");
            this.c1 = ChatColor.translateAlternateColorCodes('&', c1);
            this.c2 = ChatColor.translateAlternateColorCodes('&', c2);
            this.acts = acts;
            this.item = item;
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < 20; i++)
                sb.insert(i, ch);
            this.bar = sb.toString();
        }

        public void run() {
            if (this.i >= 20) {
                DivineItemsAPI.executeActions(this.p, this.acts, this.item);
                cancel();
                return;
            }
            paintBar();
            this.i++;
        }

        private void paintBar() {
            String kist = this.bar;
            StringBuffer sb = new StringBuffer(kist);
            sb.setLength(sb.length());
            sb.insert(0, this.c1);
            if (this.i < 20)
                sb.insert(this.i + this.c2.length() + 1, this.c2);
            kist = sb.toString();
            MsgUT.sendTitles(this.p, this.title, kist, 0, this.step, 20);
        }
    }
}
