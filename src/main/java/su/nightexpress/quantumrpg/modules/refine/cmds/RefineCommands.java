package su.nightexpress.quantumrpg.modules.refine.cmds;

import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.modules.cmds.MExecutor;

public class RefineCommands extends MExecutor {
  public RefineCommands(QuantumRPG plugin) {
    super(plugin);
  }
  
  public void setup() {
    register(new RefineCmd(this.m));
    register(new DowngradeCmd(this.m));
  }
}
