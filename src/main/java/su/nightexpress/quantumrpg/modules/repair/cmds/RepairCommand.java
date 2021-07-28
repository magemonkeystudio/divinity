package su.nightexpress.quantumrpg.modules.repair.cmds;

import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.modules.cmds.MExecutor;

public class RepairCommand extends MExecutor {
  public RepairCommand(QuantumRPG plugin) {
    super(plugin);
  }
  
  public void setup() {
    register(new RepairOpenCmd(this.m));
  }
}
