package su.nightexpress.quantumrpg.modules.sell.cmds;

import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.modules.cmds.MExecutor;

public class SellCommand extends MExecutor {
  public SellCommand(QuantumRPG plugin) {
    super(plugin);
  }
  
  public void setup() {
    register(new SellOpenCmd(this.m));
  }
}
