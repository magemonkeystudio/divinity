package su.nightexpress.quantumrpg.hooks;

import mc.promcteam.engine.hooks.Hooks;
import mc.promcteam.engine.manager.IListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.PluginEnableEvent;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.hooks.external.*;
import su.nightexpress.quantumrpg.hooks.external.mythicmobs.MythicMobsHK;
import su.nightexpress.quantumrpg.hooks.external.mythicmobs.MythicMobsHKv5;
import su.nightexpress.quantumrpg.modules.list.party.PartyManager;

public class HookListener extends IListener<QuantumRPG> {

    public HookListener(@NotNull QuantumRPG plugin) {
        super(plugin);
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        switch (event.getPlugin().getName()) {
            case "LorinthsRpgMobs": {
                this.plugin.registerHook(EHook.LORINTHS_RPG_MOBS, LorinthsRpgMobsHK.class);
                break;
            }
            case "Magic": {
                this.plugin.registerHook(EHook.MAGIC, MagicHK.class);
                break;
            }
            case "mcMMO": {
                this.plugin.registerHook(EHook.MCMMO, McmmoHK.class);
                break;
            }
            case "MythicMobs": {
                boolean mythic4 = true;
                try {
                    Class.forName("io.lumine.xikage.mythicmobs.MythicMobs");
                } catch (ClassNotFoundException classNotFoundException) {
                    mythic4 = false;
                }
                if (mythic4) {this.plugin.registerHook(Hooks.MYTHIC_MOBS, MythicMobsHK.class);}
                else {this.plugin.registerHook(Hooks.MYTHIC_MOBS, MythicMobsHKv5.class);}
                break;
            }
            case "PlaceholderAPI": {
                this.plugin.registerHook(Hooks.PLACEHOLDER_API, PlaceholderAPIHK.class);
                break;
            }
            case "PwingRaces": {
                this.plugin.registerHook(EHook.PWING_RACES, PwingRacesHK.class);
                break;
            }
            case "ProSkillAPI": {
                this.plugin.registerHook(EHook.SKILL_API, SkillAPIHK.class);
                this.plugin.setConfig();
                PartyManager partyManager = this.plugin.getModuleManager().getModule(PartyManager.class);
                if (partyManager != null) {partyManager.reload();}
                SkillAPIHK skillAPIHK = (SkillAPIHK) this.plugin.getHook(EHook.SKILL_API);
                if (skillAPIHK != null) {
                    for (Player player : Bukkit.getServer().getOnlinePlayers()) {skillAPIHK.updateSkills(player);}
                }
                break;
            }
        }
    }
}
