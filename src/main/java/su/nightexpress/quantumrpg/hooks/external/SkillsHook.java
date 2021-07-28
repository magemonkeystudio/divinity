//package su.nightexpress.quantumrpg.hooks.external;
//
//import me.leothepro555.skills.events.SkillEnergyChangeEvent;
//import me.leothepro555.skills.main.LanguageSupport;
//import me.leothepro555.skills.main.Skills;
//import org.bukkit.ChatColor;
//import org.bukkit.OfflinePlayer;
//import org.bukkit.entity.LivingEntity;
//import org.bukkit.entity.Player;
//import org.bukkit.event.EventHandler;
//import su.nightexpress.quantumrpg.QuantumRPG;
//import su.nightexpress.quantumrpg.api.EntityAPI;
//import su.nightexpress.quantumrpg.hooks.EHook;
//import su.nightexpress.quantumrpg.hooks.Hook;
//import su.nightexpress.quantumrpg.hooks.HookClass;
//import su.nightexpress.quantumrpg.hooks.HookLevel;
//import su.nightexpress.quantumrpg.stats.ItemStat;
//
//public class SkillsHook extends Hook implements HookLevel, HookClass {
//    private Skills sk;
//
//    public SkillsHook(EHook type, QuantumRPG plugin) {
//        super(type, plugin);
//    }
//
//    public void setup() {
//        this.sk = Skills.get();
//        registerListeners();
//    }
//
//    public void shutdown() {
//        unregisterListeners();
//    }
//
//    public int getLevel(Player p) {
//        return this.sk.getPlayerDataManager().getOrLoadPlayerInfo((OfflinePlayer) p).getLevel();
//    }
//
//    public String getClass(Player p) {
//        LanguageSupport.Languages l = this.sk.getPlayerDataManager().getOrLoadPlayerInfo((OfflinePlayer) p).getSkill().getLanguageName();
//        return ChatColor.stripColor(Skills.getLang().parseFirstString(l));
//    }
//
//    @EventHandler(ignoreCancelled = true)
//    public void onRegen(SkillEnergyChangeEvent e) {
//        Player li = e.getPlayer();
//        double regen = 1.0D + EntityAPI.getItemStat((LivingEntity) li, ItemStat.MANA_REGEN, null) / 100.0D;
//        if (regen > 0.0D)
//            e.setAmount(e.getAmount() * regen);
//    }
//}
