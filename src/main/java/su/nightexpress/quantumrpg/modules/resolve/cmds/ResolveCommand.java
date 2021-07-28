package su.nightexpress.quantumrpg.modules.resolve.cmds;

import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.modules.cmds.MExecutor;

public class ResolveCommand extends MExecutor {
  public ResolveCommand(QuantumRPG plugin) {
    super(plugin);
  }
  
  public void setup() {
    register(new ResolveOpenCmd(this.m));
  }
}
