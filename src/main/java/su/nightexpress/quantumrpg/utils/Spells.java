package su.nightexpress.quantumrpg.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.api.ItemAPI;

import java.util.*;

public class Spells {
    private static final Random r = new Random();

    private static final QuantumRPG plugin = QuantumRPG.instance;

    private static final HashMap<Player, HashSet<LivingEntity>> ice = new HashMap<>();

    private static final HashMap<Projectile, String> pjef = new HashMap<>();

    public static void skillIceSnake(final Player p) {
        int range = 20;
        final double dmg = ItemAPI.getAllDamage(p.getInventory().getItemInMainHand());
        final List<ArmorStand> snakes = new ArrayList<>();
        final Location start = p.getEyeLocation().add(0.0D, -2.2D, 0.0D);
        final Vector increase = start.getDirection();
        (new BukkitRunnable() {
            int i = 0;

            public void run() {
                if (this.i == 20)
                    cancel();
                Location point = start.add(increase);
                if (this.i % 5 == 0)
                    if (Utils.getDirection(Float.valueOf(p.getLocation().getYaw())) == "EAST" || Utils.getDirection(Float.valueOf(p.getLocation().getYaw())) == "WEST") {
                        point = point.add(0.0D, 0.0D, 0.33D);
                    } else {
                        point = point.add(0.33D, 0.0D, 0.0D);
                    }
                ArmorStand snake = p.getWorld().spawn(point, ArmorStand.class);
                snake.setVisible(false);
                snake.setHelmet(new ItemStack(Material.ICE));
                snake.setGravity(false);
                snake.setInvulnerable(true);
                snake.setSmall(false);
                Spells.setHeadPos(snake);
                snakes.add(snake);
                EntityUtils.add(snake);
                Utils.playEffect("BLOCK_CRACK:ICE", point.clone().add(0.0D, 2.0D, 0.0D), 0.3F, 0.0F, 0.3F, 0.3F, 15);
                Location sn2 = point.clone().add(Utils.getRandDoubleNega(-0.25D, 0.25D), Utils.getRandDouble(0.2D, 0.5D), Utils.getRandDoubleNega(-0.25D, 0.25D));
                ArmorStand snake2 = p.getWorld().spawn(sn2, ArmorStand.class);
                snake2.setVisible(false);
                snake2.setHelmet(new ItemStack(Material.SNOW_BLOCK));
                snake2.setGravity(false);
                snake2.setInvulnerable(true);
                snake2.setSmall(true);
                Spells.setHeadPos(snake2);
                snakes.add(snake2);
                EntityUtils.add(snake2);
                Utils.playEffect("BLOCK_CRACK:SNOW", sn2.clone().add(0.0D, 0.75D, 0.0D), 0.3F, 0.0F, 0.3F, 0.3F, 15);
                for (LivingEntity li : EntityUtils.getEnemies(snake, 1.15D, p)) {
                    if (Spells.ice.get(p) != null && Spells.ice.get(p).contains(li))
                        continue;
                    li.damage(dmg, p);
                    if (li.hasPotionEffect(PotionEffectType.SLOW))
                        li.removePotionEffect(PotionEffectType.SLOW);
                    li.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 0));
                    if (Spells.ice.get(p) == null) {
                        HashSet<LivingEntity> hs = new HashSet<>();
                        hs.add(li);
                        Spells.ice.put(p, hs);
                        continue;
                    }
                    Spells.ice.get(p).add(li);
                }
                if ((new Location(point.getWorld(), point.getX(), point.getY() + 1.5D, point.getZ())).getBlock().getType() != Material.AIR)
                    cancel();
                this.i++;
            }
        }).runTaskTimer(plugin, 0L, 1L);
        (new BukkitRunnable() {
            public void run() {
                for (ArmorStand a : snakes) {
                    Utils.playEffect("FIREWORKS_SPARK", a.getLocation().add(0.0D, 2.0D, 0.0D), 0.0F, 0.0F, 0.0F, 0.2F, 5);
                    a.remove();
                    EntityUtils.remove(a);
                }
            }
        }).runTaskLater(plugin, 50L);
        if (ice.get(p) != null) {
            ice.get(p).clear();
            ice.remove(p);
        }
        p.playSound(p.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.7F, 0.7F);
    }

    private static void setHeadPos(ArmorStand as) {
        double x = Utils.getRandDouble(0.0D, 15.0D);
        double z = Utils.getRandDouble(0.0D, 15.0D);
        EulerAngle a = new EulerAngle(x, 0.0D, z);
        as.setHeadPose(a);
    }

    public static void skillMeteor(final Player p) {
        int range = 50;
        Block b = p.getTargetBlock(null, range);
        final Location ground = b.getLocation();
        final Location sky = new Location(ground.getWorld(), ground.getX(), ground.getY() + 20.0D, ground.getZ());
        Location origin = b.getLocation();
        Vector target = (new Location(ground.getWorld(), ground.getX(), ground.getY() + 20.0D, ground.getZ())).toVector();
        origin.setDirection(target.subtract(origin.toVector()));
        Vector increase = origin.getDirection();
        for (int counter = 0; counter < sky.distance(ground); counter++) {
            Location loc = origin.add(increase);
            Utils.playEffect("FLAME", loc, 0.0F, 0.0F, 0.0F, 0.0F, 25);
        }
        (new BukkitRunnable() {
            public void run() {
                Location strike = new Location(sky.getWorld(), sky.getX() + Spells.r.nextInt(10), sky.getY(), sky.getZ() + Spells.r.nextInt(10));
                Vector target2 = (new Location(ground.getWorld(), ground.getX(), ground.getY(), ground.getZ())).toVector();
                strike.setDirection(target2.subtract(strike.toVector()));
                Vector increase = strike.getDirection();
                Fireball fb = strike.getWorld().spawn(strike, Fireball.class);
                fb.setShooter(p);
                fb.setDirection(increase);
                fb.setVelocity(increase.normalize());
                Spells.pjef.put(fb, "SMOKE_LARGE");
            }
        }).runTaskLater(plugin, 30L);
        p.playSound(p.getLocation(), Sound.ENTITY_BLAZE_AMBIENT, 0.7F, 0.7F);
    }

    public static void startPjEfTask() {
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            HashMap<Projectile, String> map = new HashMap<>(Spells.pjef);
            for (Projectile pj : map.keySet()) {
                if (pj.isOnGround() || !pj.isValid()) {
                    Spells.pjef.remove(pj);
                    continue;
                }
                String pe = map.get(pj);
                Utils.playEffect(pe, pj.getLocation(), 0.0F, 0.0F, 0.0F, 0.0F, 25);
            }
        }, 0L, 1L);
    }

    public static void skillIceFireStorm(final Player p, final int j) {
        final ItemStack block;
        final String p1, p2;
        int range = 50;
        Block b = p.getTargetBlock(null, range);
        final Location ground = b.getLocation();
        final Location sky = ground.clone().add(0.0D, 20.0D, 0.0D);
        if (j == 0) {
            block = new ItemStack(Material.ICE);
            p1 = "CLOUD";
            p2 = "FIREWORKS_SPARK";
        } else {
            block = new ItemStack(Material.NETHERRACK);
            p1 = "LAVA";
            p2 = "FLAME";
        }
        (new BukkitRunnable() {
            public void run() {
                (new BukkitRunnable() {
                    int i = 0;

                    public void run() {
                        if (this.i == 8) {
                            cancel();
                            return;
                        }
                        final double dmg = ItemAPI.getAllDamage(p.getInventory().getItemInMainHand());
                        final Location strike = sky.clone().add(Spells.r.nextInt(10), 0.0D, Spells.r.nextInt(10));
                        Vector target2 = ground.clone().add(Spells.r.nextInt(5), 0.0D, Spells.r.nextInt(5)).toVector();
                        strike.setDirection(target2.subtract(strike.toVector()));
                        final Vector increase = strike.getDirection();
                        final ArmorStand snake = p.getWorld().spawn(strike, ArmorStand.class);
                        snake.setVisible(false);
                        snake.setHelmet(block);
                        snake.setGravity(false);
                        snake.setInvulnerable(true);
                        snake.setSmall(true);
                        Spells.setHeadPos(snake);
                        (new BukkitRunnable() {
                            public void run() {
                                Location point = strike.add(increase);
                                snake.teleport(point);
                                Location eye = snake.getEyeLocation().clone().add(0.0D, 1.3D, 0.0D);
                                if (eye.getBlock() != null && eye.getBlock().getType() != Material.AIR) {
                                    Utils.playEffect(p1, eye, 1.25F, 0.2F, 1.25F, 0.1F, 100);
                                    for (LivingEntity li : EntityUtils.getEnemies(snake, 2.0D, p)) {
                                        li.damage(dmg, p);
                                        if (j == 0) {
                                            if (li.hasPotionEffect(PotionEffectType.SLOW))
                                                li.removePotionEffect(PotionEffectType.SLOW);
                                            li.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 0));
                                            continue;
                                        }
                                        li.setFireTicks(100);
                                    }
                                    eye.getWorld().playSound(eye, Sound.ENTITY_GENERIC_EXPLODE, 1.0F, 1.0F);
                                    snake.remove();
                                    cancel();
                                    return;
                                }
                                eye.getWorld().playSound(eye, Sound.ENTITY_GENERIC_EXPLODE, 1.0F, 1.0F);
                                Utils.playEffect(p2, snake.getEyeLocation().clone().add(0.2D, 1.0D, 0.2D), 0.0F, 0.0F, 0.0F, 0.25F, 15);
                                Utils.playEffect("BLOCK_CRACK:" + block.getType().name(), snake.getEyeLocation().clone().add(0.2D, 1.0D, 0.2D), 0.0F, 0.0F, 0.0F, 0.3F, 15);
                            }
                        }).runTaskTimer(Spells.plugin, 0L, 1L);
                        this.i++;
                    }
                }).runTaskTimer(Spells.plugin, 0L, 15L);
            }
        }).runTaskLater(plugin, 30L);
    }
}
