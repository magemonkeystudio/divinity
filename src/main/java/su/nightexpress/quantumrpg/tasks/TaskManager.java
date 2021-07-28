package su.nightexpress.quantumrpg.tasks;

import java.util.HashMap;
import org.bukkit.entity.LivingEntity;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.stats.BleedEffect;
import su.nightexpress.quantumrpg.stats.BleedRateSettings;
import su.nightexpress.quantumrpg.stats.ItemStat;
import su.nightexpress.quantumrpg.utils.DamageUtils;
import su.nightexpress.quantumrpg.utils.Utils;

public class TaskManager {
  private QuantumRPG plugin;
  
  private BleedTask global;
  
  private AtkTask atk;
  
  private HashMap<LivingEntity, BleedEffect> bleed;
  
  public TaskManager(QuantumRPG plugin) {
    this.plugin = plugin;
    this.global = new BleedTask(this.plugin);
    this.atk = new AtkTask(plugin);
    this.bleed = new HashMap<>();
  }
  
  public void start() {
    this.global.start();
    this.atk.start();
  }
  
  public void stop() {
    this.global.stop();
    this.atk.stop();
    this.bleed.clear();
  }
  
  public void addBleedEffect(LivingEntity li, double dmg) {
    BleedRateSettings bs = (BleedRateSettings)ItemStat.BLEED_RATE.getSettings();
    if (bs == null || bs.getFormula() == null)
      return; 
    double d = DamageUtils.eval(bs.getFormula().replace("%dmg%", String.valueOf(dmg)));
    BleedEffect be = new BleedEffect(bs.getTime(), d);
    this.bleed.put(li, be);
  }
  
  public void processBleed() {
    BleedRateSettings bs = (BleedRateSettings)ItemStat.BLEED_RATE.getSettings();
    HashMap<LivingEntity, BleedEffect> map = new HashMap<>(this.bleed);
    for (LivingEntity li : map.keySet()) {
      BleedEffect be = map.get(li);
      if (be == null)
        continue; 
      if (!li.isValid() || li.isDead() || be.getTime() <= 0) {
        this.bleed.remove(li);
        continue;
      } 
      li.damage(be.getDamage());
      Utils.playEffect(bs.getEffect(), li.getEyeLocation(), 0.2F, 0.25F, 0.2F, 0.1F, 50);
      be.setTime(be.getTime() - 1);
      this.bleed.put(li, be);
    } 
  }
}
