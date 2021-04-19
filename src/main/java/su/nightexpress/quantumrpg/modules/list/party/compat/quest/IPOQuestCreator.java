package su.nightexpress.quantumrpg.modules.list.party.compat.quest;

import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import com.guillaumevdn.gcore.data.UserInfo;
import com.guillaumevdn.questcreator.QuestCreator;
import com.guillaumevdn.questcreator.data.QCDataManager;
import com.guillaumevdn.questcreator.module.quest.Quest;

import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.modules.list.party.PartyManager;
import su.nightexpress.quantumrpg.modules.list.party.PartyManager.PartyMember;

@Deprecated
public class IPOQuestCreator extends IPartyObjective {

	public IPOQuestCreator(@NotNull QuantumRPG plugin, @NotNull PartyManager partyManager) {
		super(plugin, partyManager);
	}

	@Override
	public void progressObjective(
			@NotNull Player killer, @NotNull Set<PartyMember> objGeters, @NotNull Event e) {
		
        QCDataManager qcd = QuestCreator.inst().getData();
        UserInfo mainInfo = new UserInfo(killer);
        
        // iterate the party players
        for (PartyMember partyPlayer : objGeters) {
            // iterate the party player's quests
            for (Quest quest : qcd.getQuests().getElements(new UserInfo(partyPlayer.getPlayer()))) {
                // if the main player is in this quest (coop quest or his own quest), then the quest will be updated already, so don't check it here
                if (quest.isUser(mainInfo)) {
                    continue;
                }
                // progress the quest, not checking if the player is a member of the quest and if he has the correct role
                // TODO
                /*CheckResult result = quest.check(e, killer, false);
                if (result.equals(CheckResult.PROGRESSED)) {// progressed the quest
                } else if (result.equals(CheckResult.STOPPED)) {// stopped the quest
                } else if (result.equals(CheckResult.PAUSED)) {// paused the quest
                } else if (result.equals(CheckResult.NONE)) {// no effect
                }*/
            }
        }
	}
}
