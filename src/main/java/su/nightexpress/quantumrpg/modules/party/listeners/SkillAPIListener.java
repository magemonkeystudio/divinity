package su.nightexpress.quantumrpg.modules.party.listeners;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.enums.ExpSource;
import com.sucy.skill.api.event.PlayerExperienceGainEvent;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import su.nightexpress.quantumrpg.modules.party.PartyManager;
import su.nightexpress.quantumrpg.utils.Utils;

public class SkillAPIListener implements Listener {
  private PartyManager pm;
  
  public SkillAPIListener(PartyManager pm) {
    this.pm = pm;
  }
  
  @EventHandler
  public void onExpSkillapi(PlayerExperienceGainEvent e) {
    Player p = e.getPlayerData().getPlayer();
    if (!this.pm.isInParty(p))
      return; 
    if (e.getSource() != ExpSource.MOB)
      return; 
    double exp = e.getExp();
    PartyManager.Party party = this.pm.getPlayerParty(p);
    int size = party.getMembers().size();
    if (exp > size) {
      exp /= size;
      e.setExp((int)exp);
      for (PartyManager.PartyMember pm : party.getMembers()) {
        Player p1 = pm.getPlayer();
        if (p1 == null)
          continue; 
        SkillAPI.getPlayerAccountData((OfflinePlayer)p1).getActiveData().giveExp(Utils.round3(exp), ExpSource.MOB);
      } 
    } else {
      List<PartyManager.PartyMember> list = new ArrayList<>(party.getMembers());
      for (int i = 0; i < exp; i++) {
        Player p1 = ((PartyManager.PartyMember)list.get(i)).getPlayer();
        if (p1 != null)
          SkillAPI.getPlayerAccountData((OfflinePlayer)p1).getActiveData().giveExp(1.0D, ExpSource.MOB); 
      } 
    } 
  }
}
