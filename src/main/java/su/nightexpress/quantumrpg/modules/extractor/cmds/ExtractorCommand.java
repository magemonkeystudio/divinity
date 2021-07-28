package su.nightexpress.quantumrpg.modules.extractor.cmds;

import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.modules.cmds.MExecutor;

public class ExtractorCommand extends MExecutor {
  public ExtractorCommand(QuantumRPG plugin) {
    super(plugin);
  }
  
  public void setup() {
    register(new ExtractorOpenCmd(this.m));
  }
}
