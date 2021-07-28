package su.nightexpress.quantumrpg.stats;

public class BleedEffect {
  private int time;
  
  private double dmg;
  
  public BleedEffect(int time, double dmg) {
    setTime(time);
    setDamage(dmg);
  }
  
  public int getTime() {
    return this.time;
  }
  
  public void setTime(int time) {
    this.time = time;
  }
  
  public double getDamage() {
    return this.dmg;
  }
  
  public void setDamage(double dmg) {
    this.dmg = dmg;
  }
}
