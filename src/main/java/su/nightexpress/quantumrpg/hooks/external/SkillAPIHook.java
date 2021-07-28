package su.nightexpress.quantumrpg.hooks.external;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.enums.ExpSource;
import com.sucy.skill.api.event.PlayerCastSkillEvent;
import com.sucy.skill.api.event.PlayerClassChangeEvent;
import com.sucy.skill.api.event.PlayerExperienceGainEvent;
import com.sucy.skill.api.event.PlayerManaGainEvent;
import com.sucy.skill.api.player.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.api.EntityAPI;
import su.nightexpress.quantumrpg.api.ItemAPI;
import su.nightexpress.quantumrpg.config.Config;
import su.nightexpress.quantumrpg.hooks.EHook;
import su.nightexpress.quantumrpg.hooks.Hook;
import su.nightexpress.quantumrpg.hooks.HookClass;
import su.nightexpress.quantumrpg.hooks.HookLevel;
import su.nightexpress.quantumrpg.modules.EModule;
import su.nightexpress.quantumrpg.modules.party.PartyManager;
import su.nightexpress.quantumrpg.stats.ItemStat;

public class SkillAPIHook extends Hook implements HookLevel, HookClass {
    public SkillAPIHook(EHook type, QuantumRPG plugin) {
        super(type, plugin);
    }

    public void setup() {
        registerListeners();
    }

    public void shutdown() {
        unregisterListeners();
    }

    public int getLevel(Player p) {
        PlayerData playerData = SkillAPI.getPlayerData(p);
        return playerData.hasClass() ? playerData.getMainClass().getLevel() : 0;
    }

    public String getClass(Player p) {
        PlayerData data = SkillAPI.getPlayerData(p);
        if (data.hasClass())
            return ChatColor.stripColor(data.getMainClass().getData().getName());
        return "";
    }

    @EventHandler
    public void onSkillCast(PlayerCastSkillEvent e) {
        if (!Config.skillAPIReduceDurability())
            return;
        Player p = e.getPlayer();
        ItemStack item = p.getInventory().getItemInMainHand();
        ItemAPI.reduceDurability(p, item, 1);
    }

    @EventHandler
    public void onClass(PlayerClassChangeEvent e) {
        Player p = e.getPlayerData().getPlayer();
        EntityAPI.checkForLegitItems(p);
    }

    @EventHandler(ignoreCancelled = true)
    public void onRegen(PlayerManaGainEvent e) {
        if (e.getPlayerData() == null)
            return;
        Player li = e.getPlayerData().getPlayer();
        if (li == null)
            return;
        try {
            double regen = 1.0D + EntityAPI.getItemStat(li, ItemStat.MANA_REGEN, null) / 100.0D;
            if (regen > 0.0D)
                e.setAmount(e.getAmount() * regen);
        } catch (Exception exception) {
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onExp(PlayerExperienceGainEvent e) {
        if (!EModule.PARTY.isEnabled())
            return;
        if (e.getSource() != ExpSource.MOB)
            return;
        Player p = e.getPlayerData().getPlayer();
        PartyManager pm = this.plugin.getModule(PartyManager.class);
        if (pm.isInParty(p)) {
            PartyManager.Party g = pm.getPlayerParty(p);
            double exp = e.getExp();
            double size = g.getMembers().size();
            double each = 1.0D;
            if (exp >= size)
                each = exp / size;
            e.setExp((int) each);
            for (PartyManager.PartyMember m : g.getMembers()) {
                if (exp <= 0.0D)
                    break;
                Player p2 = m.getPlayer();
                if (p2.equals(p))
                    continue;
                SkillAPI.getPlayerAccountData(p2).getActiveData().giveExp(each, ExpSource.MOB);
                exp -= each;
            }
        }
    }
}
