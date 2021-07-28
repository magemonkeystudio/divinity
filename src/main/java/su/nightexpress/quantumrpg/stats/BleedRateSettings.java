package su.nightexpress.quantumrpg.stats;

public class BleedRateSettings extends StatSettings {
  private int time;
  
  private String formula;
  
  private String effect;
  
  public BleedRateSettings(ItemStat att, int time, String formula, String effect) {
    super(att);
    setTime(time);
    setFormula(formula);
    setEffect(effect);
  }
  
  public int getTime() {
    return this.time;
  }
  
  public void setTime(int time) {
    this.time = time;
  }
  
  public String getFormula() {
    return this.formula;
  }
  
  public void setFormula(String formula) {
    this.formula = formula;
  }
  
  public String getEffect() {
    return this.effect;
  }
  
  public void setEffect(String effect) {
    this.effect = effect;
  }
}
