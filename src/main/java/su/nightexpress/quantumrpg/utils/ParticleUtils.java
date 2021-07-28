package su.nightexpress.quantumrpg.utils;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import su.nightexpress.quantumrpg.QuantumRPG;

public class ParticleUtils {
    private static QuantumRPG plugin;

    static {
        plugin = QuantumRPG.instance;
    }

    public ParticleUtils() {
    }

    public static void wave(final Location loc) {
        (new BukkitRunnable() {
            int i = 0;

            public void run() {
                if (++this.i == 20) {
                    this.cancel();
                }

                int n = this.i;
                double n2 = (0.5D + (double) n * 0.15D) % 3.0D;

                for (int n3 = 0; (double) n3 < n2 * 10.0D; ++n3) {
                    double n4 = 6.283185307179586D / (n2 * 10.0D) * (double) n3;
                    Utils.playEffect("REDSTONE", Utils.getPointOnCircle(loc, false, n4, n2, 1.0D), 0.1F, 0.1F, 0.1F, 0.0F, 2);
                    if (n < 15) {
                        Utils.playEffect("CRIT_MAGIC", Utils.getPointOnCircle(loc, false, n4, n2, 1.0D), 0.1F, 0.1F, 0.1F, 0.0F, 1);
                    }
                }

            }
        }).runTaskTimerAsynchronously(plugin, 0L, 1L);
    }

    public static void doParticle(Entity e, final String eff1, final String eff2) {
        new BukkitRunnable() {
            int i = 1;
            int k = 0;
            World localWorld;
            double d3;
            double d5;
            double d7;

            {
                this.localWorld = e.getWorld();
                this.d3 = e.getLocation().getX();
                this.d5 = e.getLocation().getY();
                this.d7 = e.getLocation().getZ();
            }

            public void run() {
                if (this.k == 3) {
                    this.cancel();
                }

                Utils.playEffect(eff1, new Location(this.localWorld, this.d3, this.d5 + 1.8D, this.d7), 0.0F, 0.0F, 0.0F, 0.0F, this.i);
                Utils.playEffect(eff1, new Location(this.localWorld, this.d3, this.d5 + 1.5D, this.d7), 0.0F, 0.0F, 0.0F, 0.0F, this.i);
                Utils.playEffect(eff1, new Location(this.localWorld, this.d3 + 0.6D, this.d5 + 1.8D, this.d7), 0.0F, 0.0F, 0.0F, 0.0F, this.i);
                Utils.playEffect(eff1, new Location(this.localWorld, this.d3 + 0.6D, this.d5 + 1.5D, this.d7), 0.0F, 0.0F, 0.0F, 0.0F, this.i);
                Utils.playEffect(eff1, new Location(this.localWorld, this.d3 + 0.6D, this.d5 + 1.8D, this.d7 + 0.6D), 0.0F, 0.0F, 0.0F, 0.0F, this.i);
                Utils.playEffect(eff1, new Location(this.localWorld, this.d3 + 0.6D, this.d5 + 1.5D, this.d7 + 0.6D), 0.0F, 0.0F, 0.0F, 0.0F, this.i);
                Utils.playEffect(eff1, new Location(this.localWorld, this.d3 - 0.6D, this.d5 + 1.8D, this.d7), 0.0F, 0.0F, 0.0F, 0.0F, this.i);
                Utils.playEffect(eff1, new Location(this.localWorld, this.d3 - 0.6D, this.d5 + 1.5D, this.d7), 0.0F, 0.0F, 0.0F, 0.0F, this.i);
                Utils.playEffect(eff1, new Location(this.localWorld, this.d3 - 0.6D, this.d5 + 1.8D, this.d7 + 0.6D), 0.0F, 0.0F, 0.0F, 0.0F, this.i);
                Utils.playEffect(eff1, new Location(this.localWorld, this.d3 - 0.6D, this.d5 + 1.5D, this.d7 + 0.6D), 0.0F, 0.0F, 0.0F, 0.0F, this.i);
                Utils.playEffect(eff1, new Location(this.localWorld, this.d3, this.d5 + 1.8D, this.d7 + 0.6D), 0.0F, 0.0F, 0.0F, 0.0F, this.i);
                Utils.playEffect(eff1, new Location(this.localWorld, this.d3 - 0.6D, this.d5 + 1.5D, this.d7 + 0.6D), 0.0F, 0.0F, 0.0F, 0.0F, this.i);
                Utils.playEffect(eff1, new Location(this.localWorld, this.d3 + 0.6D, this.d5 + 1.8D, this.d7 - 0.6D), 0.0F, 0.0F, 0.0F, 0.0F, this.i);
                Utils.playEffect(eff1, new Location(this.localWorld, this.d3 + 0.6D, this.d5 + 1.5D, this.d7 - 0.6D), 0.0F, 0.0F, 0.0F, 0.0F, this.i);
                Utils.playEffect(eff1, new Location(this.localWorld, this.d3, this.d5 + 1.8D, this.d7 - 0.6D), 0.0F, 0.0F, 0.0F, 0.0F, this.i);
                Utils.playEffect(eff1, new Location(this.localWorld, this.d3, this.d5 + 1.5D, this.d7 - 0.6D), 0.0F, 0.0F, 0.0F, 0.0F, this.i);
                Utils.playEffect(eff1, new Location(this.localWorld, this.d3 - 0.6D, this.d5 + 1.8D, this.d7 - 0.6D), 0.0F, 0.0F, 0.0F, 0.0F, this.i);
                Utils.playEffect(eff1, new Location(this.localWorld, this.d3 - 0.6D, this.d5 + 1.5D, this.d7 - 0.6D), 0.0F, 0.0F, 0.0F, 0.0F, this.i);
                Utils.playEffect(eff2, new Location(this.localWorld, this.d3, this.d5 + 1.0D, this.d7), 0.0F, 0.0F, 0.0F, 0.0F, 2);
                ++this.k;
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }

    public static void helix(final String eff, final LivingEntity e1, final int lvl) {
        (new BukkitRunnable() {
            double phi = 0.0D;

            public void run() {
                this.phi += 0.39269908169872414D;
                Location location1 = e1.getLocation();

                for (double t = 0.0D; t <= (double) lvl * 3.141592653589793D; t += 0.19634954084936207D) {
                    for (double i = 0.0D; i <= 1.0D; ++i) {
                        double x = 0.4D * (6.283185307179586D - t) * 0.5D * Math.cos(t + this.phi + i * 3.141592653589793D);
                        double y = 0.5D * t;
                        double z = 0.4D * (6.283185307179586D - t) * 0.5D * Math.sin(t + this.phi + i * 3.141592653589793D);
                        location1.add(x, y, z);
                        Utils.playEffect(eff, location1, 0.0F, 0.0F, 0.0F, 0.0F, 1);
                        location1.subtract(x, y, z);
                    }
                }

                if (this.phi > 6.283185307179586D) {
                    this.cancel();
                }

            }
        }).runTaskTimer(plugin, 0L, 3L);
    }

    public static void aura(final String eff, final LivingEntity p, final int lvl) {
        (new BukkitRunnable() {
            int i = 0;

            public void run() {
                if (this.i >= 20) {
                    this.cancel();
                }

                Location l = p.getLocation().add(0.0D, (double) lvl, 0.0D);
                Utils.playEffect(eff, l, 0.5F, 0.5F, 0.5F, 0.1F, 25);
                ++this.i;
            }
        }).runTaskTimer(plugin, 0L, 5L);
    }

    public static void foot(final String eff, final LivingEntity p, final int lvl) {
        (new BukkitRunnable() {
            int i = 0;

            public void run() {
                if (this.i >= 50) {
                    this.cancel();
                }

                Location l = p.getLocation();
                Utils.playEffect(eff, l, 0.0F, 0.0F, 0.0F, 0.1F, 10 * lvl);
                ++this.i;
            }
        }).runTaskTimer(plugin, 0L, 2L);
    }

    public static void repairEffect(final Location loc, final String eff) {
        (new BukkitRunnable() {
            int i = 0;

            public void run() {
                if (++this.i == 72) {
                    this.cancel();
                }

                int n = this.i;
                double n2 = 0.3141592653589793D * (double) n;
                double n3 = (double) n * 0.1D % 2.5D;
                double n4 = 0.45D;
                Location pointOnCircle = Utils.getPointOnCircle(Utils.getCenter(loc), true, n2, 0.45D, n3);
                Location pointOnCircle2 = Utils.getPointOnCircle(Utils.getCenter(loc), true, n2 - 3.141592653589793D, 0.45D, n3);
                Utils.playEffect("FLAME", pointOnCircle, 0.0F, 0.0F, 0.0F, 0.0F, 1);
                Utils.playEffect("FLAME", pointOnCircle2, 0.0F, 0.0F, 0.0F, 0.0F, 1);
                Utils.playEffect(eff, Utils.getCenter(loc).add(0.0D, 0.5D, 0.0D), 0.2F, 0.0F, 0.2F, 0.0F, 5);
            }
        }).runTaskTimerAsynchronously(plugin, 0L, 1L);
    }

    public static void drawParticleLine(LivingEntity blaze, LivingEntity li, String pe, float speed, int amount) {
        Location to = li.getLocation();
        Location origin = blaze.getEyeLocation();
        Vector target = (new Location(to.getWorld(), to.getX(), to.getY(), to.getZ())).toVector();
        origin.setDirection(target.subtract(origin.toVector()));
        Vector increase = origin.getDirection();

        for (int counter = 0; (double) counter < blaze.getLocation().distance(to); ++counter) {
            Location loc = origin.add(increase);
            Utils.playEffect(pe, loc, 0.0F, 0.0F, 0.0F, 0.0F, 5);
        }

    }
}
