package su.nightexpress.quantumrpg.stats;

public class DisarmRateSettings extends StatSettings {
  private String effect;
  
  private String msg_dam;
  
  private String msg_zertva;
  
  public DisarmRateSettings(ItemStat att, String effect, String msg_dam, String msg_zertva) {
    super(att);
    this.effect = effect;
    this.msg_dam = msg_dam;
    this.msg_zertva = msg_zertva;
  }
  
  public String getEffect() {
    return this.effect;
  }
  
  public String getMsgToDamager() {
    return this.msg_dam;
  }
  
  public String getMsgToEntity() {
    return this.msg_zertva;
  }
}
