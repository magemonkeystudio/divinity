package su.nightexpress.quantumrpg.listeners;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.quantumrpg.config.Config;
import su.nightexpress.quantumrpg.modules.arrows.ArrowManager;
import su.nightexpress.quantumrpg.types.DamageType;
import su.nightexpress.quantumrpg.utils.DamageUtils;
import su.nightexpress.quantumrpg.utils.Utils;

public class DamageMeta {
  private ItemStack weapon;
  
  private Entity damager;
  
  private Entity zertva;
  
  private boolean crit = false;
  
  private boolean dodge = false;
  
  private boolean block = false;
  
  private double block_procent = 0.0D;
  
  private double default_damage = 0.0D;
  
  private ArrowManager.QArrow arrow = null;
  
  private double pvpe_dmg = 0.0D;
  
  private double pvpe_def = 0.0D;
  
  private Map<DamageType, Double> dmg_types;
  
  private Map<EntityDamageEvent.DamageCause, Double> dmg_cause;
  
  private String entity_name;
  
  public DamageMeta(ItemStack item, Entity damager, Entity zertva) {
    this.weapon = item;
    this.damager = damager;
    this.zertva = zertva;
    this.dmg_types = new HashMap<>();
    this.dmg_cause = new HashMap<>();
    setEntityName(Utils.getEntityName(zertva));
  }
  
  public ArrowManager.QArrow getArrow() {
    return this.arrow;
  }
  
  public ItemStack getWeapon() {
    return this.weapon;
  }
  
  public Entity getEntity() {
    return this.zertva;
  }
  
  public double getDefaultDamage() {
    return this.default_damage;
  }
  
  public Entity getDamager() {
    return this.damager;
  }
  
  public void setDamageCause(EntityDamageEvent.DamageCause src, double amount) {
    this.dmg_cause.put(src, Double.valueOf(amount));
  }
  
  public double getDamage(String type) {
    DamageType dt = Config.getDamageTypeById(type);
    return getDamage(dt);
  }
  
  public double getDamage(DamageType dt) {
    if (dt == null)
      return 0.0D; 
    if (this.dmg_types.containsKey(dt)) {
      double dmg = ((Double)this.dmg_types.get(dt)).doubleValue();
      return recalcByFormula(dmg);
    } 
    return 0.0D;
  }
  
  public double getDamageBlocked() {
    return this.block_procent;
  }
  
  public double getDamageCause(EntityDamageEvent.DamageCause c) {
    if (this.dmg_cause.containsKey(c))
      return ((Double)this.dmg_cause.get(c)).doubleValue(); 
    return 0.0D;
  }
  
  public Map<EntityDamageEvent.DamageCause, Double> getDamageCause() {
    return this.dmg_cause;
  }
  
  public double getDamageCause(String type) {
    type = type.toLowerCase().replace("default", "custom");
    for (Map.Entry<EntityDamageEvent.DamageCause, Double> ee : this.dmg_cause.entrySet()) {
      if (((EntityDamageEvent.DamageCause)ee.getKey()).name().equalsIgnoreCase(type))
        return ((Double)ee.getValue()).doubleValue(); 
    } 
    return 0.0D;
  }
  
  public double getTotalDamage() {
    double d = 0.0D;
    Iterator<Double> iterator;
    for (iterator = this.dmg_types.values().iterator(); iterator.hasNext(); ) {
      double d1 = ((Double)iterator.next()).doubleValue();
      d += d1;
    } 
    for (iterator = this.dmg_cause.values().iterator(); iterator.hasNext(); ) {
      double d1 = ((Double)iterator.next()).doubleValue();
      d += d1;
    } 
    if (d == 0.0D)
      d = this.default_damage; 
    return Math.max(0.0D, recalcByFormula(d));
  }
  
  public double getPvPEDamage() {
    return this.pvpe_dmg;
  }
  
  public double getPvPEDefense() {
    return this.pvpe_def;
  }
  
  public String getEntityName() {
    if (this.entity_name == null)
      setEntityName(Utils.getEntityName(this.zertva)); 
    return this.entity_name;
  }
  
  public void setEntityName(String name) {
    this.entity_name = name;
  }
  
  public void setArrow(ArrowManager.QArrow arrow) {
    this.arrow = arrow;
  }
  
  public void setEntity(Entity e) {
    this.zertva = e;
  }
  
  public void setDamager(Entity e) {
    this.damager = e;
  }
  
  public void setDefaultDamage(double d) {
    this.default_damage = d;
  }
  
  public void setDamageType(DamageType dt, double amount) {
    this.dmg_types.put(dt, Double.valueOf(Math.max(0.0D, amount)));
  }
  
  public void setDamageBlocked(double d) {
    if (isBlocked()) {
      this.block_procent = d;
    } else {
      this.block_procent = 0.0D;
    } 
  }
  
  public void setBlocked(boolean block) {
    this.block = block;
  }
  
  public void setDodge(boolean dodge) {
    this.dodge = dodge;
  }
  
  public void setCritical(boolean crit) {
    this.crit = crit;
  }
  
  public void setPvPEDamage(double val) {
    this.pvpe_dmg = val;
  }
  
  public void setPvPEDefense(double val) {
    this.pvpe_def = val;
  }
  
  public void setWeapon(ItemStack item) {
    this.weapon = item;
  }
  
  public boolean isCritical() {
    return this.crit;
  }
  
  public boolean isDodged() {
    return this.dodge;
  }
  
  public boolean isBlocked() {
    return this.block;
  }
  
  public void multiply(double x) {
    Map<DamageType, Double> map1 = new HashMap<>();
    for (Map.Entry<DamageType, Double> ee : this.dmg_types.entrySet()) {
      double d = ((Double)ee.getValue()).doubleValue() * x;
      map1.put(ee.getKey(), Double.valueOf(d));
    } 
    this.dmg_types = map1;
    Map<EntityDamageEvent.DamageCause, Double> map2 = new HashMap<>();
    for (Map.Entry<EntityDamageEvent.DamageCause, Double> ee : this.dmg_cause.entrySet()) {
      double d = ((Double)ee.getValue()).doubleValue() * x;
      map2.put(ee.getKey(), Double.valueOf(d));
    } 
    this.dmg_cause = map2;
    this.default_damage *= x;
  }
  
  private double recalcByFormula(double damage) {
    String formula_total = Config.getDamageFormula()
      .replace("%dmg%", String.valueOf(damage))
      .replace("%pvpe_dmg%", String.valueOf(this.pvpe_dmg))
      .replace("%pvpe_def%", String.valueOf(this.pvpe_def))
      .replace("%block%", String.valueOf(this.block_procent));
    return DamageUtils.eval(formula_total);
  }
  
  public void fixDefaultDamage() {
    if (this.dmg_types.isEmpty() && this.dmg_cause.isEmpty()) {
      setDamageType(Config.getDamageTypeByDefault(), this.default_damage);
      return;
    } 
  }
  
  public void addMissingDmg(double damage) {
    double me = getTotalDamage();
    if (me == damage)
      return; 
    double part = 0.0D;
    if (me < damage) {
      part = damage - me;
    } else {
      part = -(me - damage);
    } 
    if (!this.dmg_cause.isEmpty()) {
      part /= this.dmg_cause.size();
      for (Map.Entry<EntityDamageEvent.DamageCause, Double> e : this.dmg_cause.entrySet())
        this.dmg_cause.put(e.getKey(), Double.valueOf(((Double)e.getValue()).doubleValue() + part)); 
      return;
    } 
    if (!this.dmg_types.isEmpty()) {
      part /= this.dmg_types.size();
      for (Map.Entry<DamageType, Double> e : this.dmg_types.entrySet())
        this.dmg_types.put(e.getKey(), Double.valueOf(((Double)e.getValue()).doubleValue() + part)); 
      return;
    } 
    this.default_damage += part;
  }
}
