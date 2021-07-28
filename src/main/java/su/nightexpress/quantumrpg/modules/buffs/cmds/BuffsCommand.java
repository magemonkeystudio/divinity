package su.nightexpress.quantumrpg.modules.buffs.cmds;

import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.modules.cmds.MExecutor;

public class BuffsCommand extends MExecutor {
  public BuffsCommand(QuantumRPG plugin) {
    super(plugin);
  }
  
  public void setup() {
    register(new BuffsResetCmd(this.plugin, this.m));
    register(new BuffsAddCmd(this.plugin, this.m));
  }
}
