package su.nightexpress.quantumrpg.utils.msg;

public class DelayedMsg {
  private String msg;
  
  private long delay;
  
  public DelayedMsg(String msg, int sec) {
    this.msg = msg;
    this.delay = System.currentTimeMillis() + 1000L * sec;
  }
  
  public String getMsg() {
    return this.msg;
  }
  
  public boolean isExpired() {
    return (System.currentTimeMillis() > this.delay);
  }
}
