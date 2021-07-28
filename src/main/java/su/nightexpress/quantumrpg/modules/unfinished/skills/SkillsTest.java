package su.nightexpress.quantumrpg.modules.unfinished.skills;

import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.hooks.HookUtils;
import su.nightexpress.quantumrpg.utils.ParticleUtils;
import su.nightexpress.quantumrpg.utils.Utils;

public class SkillsTest {
  private QuantumRPG plugin;
  
  public SkillsTest(QuantumRPG plugin) {
    this.plugin = plugin;
  }
  
  public void getMinigun(double force, final int lvl, double value, final LivingEntity e1, Entity e) {
    if (Utils.r.nextInt(100) <= value) {
      final Vector vec = e.getVelocity();
      (new BukkitRunnable() {
          int i = 0;
          
          public void run() {
            if (this.i == lvl + 3)
              cancel(); 
            ((Arrow)e1.launchProjectile(Arrow.class)).setVelocity(vec);
            this.i++;
          }
        }).runTaskTimer((Plugin)this.plugin, 1L, 2L);
    } 
  }
  
  public void getAmbush(int lvl, double value, LivingEntity e, LivingEntity e2) {
    if (Utils.r.nextInt(100) <= value) {
      Utils.playEffect("EXPLOSION_NORMAL", e.getLocation(), 0.0F, 0.0F, 0.0F, 0.15F, 15);
      e.teleport(e2.getLocation().add(e2.getLocation().getDirection().multiply(-2.0D)));
      e2.damage(lvl);
      Utils.playEffect("EXPLOSION_NORMAL", e.getLocation(), 0.0F, 0.0F, 0.0F, 0.15F, 15);
    } 
  }
  
  public void getEternalDenial(int lvl, double value, LivingEntity e) {
    Location l = e.getLocation();
    l.getWorld().strikeLightning(l);
    e.damage(lvl);
  }
  
  public void getPrayVictor(double value, LivingEntity e) {
    for (PotionEffect pe : e.getActivePotionEffects()) {
      if (pe.getType() == PotionEffectType.BLINDNESS || 
        pe.getType() == PotionEffectType.CONFUSION || 
        pe.getType() == PotionEffectType.HUNGER || 
        pe.getType() == PotionEffectType.LEVITATION || 
        pe.getType() == PotionEffectType.POISON || 
        pe.getType() == PotionEffectType.SLOW || 
        pe.getType() == PotionEffectType.SLOW_DIGGING || 
        pe.getType() == PotionEffectType.UNLUCK || 
        pe.getType() == PotionEffectType.WEAKNESS || 
        pe.getType() == PotionEffectType.WITHER)
        e.removePotionEffect(pe.getType()); 
    } 
    ParticleUtils.doParticle((Entity)e, "CRIT_MAGIC", "VILLAGER_HAPPY");
  }
  
  public void getMagicImp(double value, LivingEntity e) {
    for (PotionEffect pe : e.getActivePotionEffects())
      e.removePotionEffect(pe.getType()); 
    ParticleUtils.doParticle((Entity)e, "CRIT_MAGIC", "VILLAGER_ANGRY");
  }
  
  public void getPunishWave(int lvl, double value, LivingEntity e) {
    if (Utils.r.nextInt(100) <= value) {
      for (Entity e2 : e.getLocation().getWorld().getNearbyEntities(e.getLocation(), 5.0D, 5.0D, 5.0D)) {
        if (!(e2 instanceof LivingEntity) || e2.equals(e) || 
          HookUtils.canFights((Entity)e, e2))
          continue; 
        Location localLocation1 = e2.getLocation();
        Location localLocation2 = localLocation1.subtract(e.getLocation());
        Vector localVector = localLocation2.getDirection().normalize().multiply(-1.4D);
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
        double d1 = localVector.getX() * 2.0D;
        double d2 = localVector.getY() * 2.0D;
        double d3 = localVector.getZ() * 2.0D;
        if (d1 >= 3.0D)
          d1 *= 0.5D; 
        if (d2 >= 3.0D)
          d2 *= 0.5D; 
        if (d3 >= 3.0D)
          d3 *= 0.5D; 
        localVector.setX(d1);
        localVector.setY(d2);
        localVector.setZ(d3);
        e2.setVelocity(localVector);
        ((LivingEntity)e2).damage(lvl);
      } 
      ParticleUtils.wave(e.getLocation());
    } 
  }
}
