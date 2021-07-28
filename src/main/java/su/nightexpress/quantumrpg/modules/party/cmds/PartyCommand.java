package su.nightexpress.quantumrpg.modules.party.cmds;

import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.modules.cmds.MExecutor;

public class PartyCommand extends MExecutor {
  public PartyCommand(QuantumRPG plugin) {
    super(plugin);
  }
  
  public void setup() {
    register(new PartyChatCmd(this.m));
    register(new PartyCreateCmd(this.m));
    register(new PartyDisbandCmd(this.m));
    register(new PartyDropCmd(this.m));
    register(new PartyInfoCmd(this.m));
    register(new PartyInviteCmd(this.m));
    register(new PartyJoinCmd(this.m));
    register(new PartyKickCmd(this.m));
    register(new PartyLeaveCmd(this.m));
    register(new PartyTpCmd(this.m));
  }
}
