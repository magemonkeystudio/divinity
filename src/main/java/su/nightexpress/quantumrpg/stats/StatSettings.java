package su.nightexpress.quantumrpg.stats;

public class StatSettings {
  private ItemStat att;
  
  public StatSettings(ItemStat att) {
    this.att = att;
  }
  
  public ItemStat getAttribute() {
    return this.att;
  }
}
