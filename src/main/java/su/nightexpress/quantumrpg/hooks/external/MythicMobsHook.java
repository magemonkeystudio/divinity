package su.nightexpress.quantumrpg.hooks.external;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import org.bukkit.entity.Entity;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.hooks.EHook;
import su.nightexpress.quantumrpg.hooks.Hook;

public class MythicMobsHook extends Hook {
    private MythicMobs mm;

    public MythicMobsHook(EHook type, QuantumRPG plugin) {
        super(type, plugin);
    }

    public void setup() {
        this.mm = MythicMobs.inst();
    }

    public void shutdown() {
    }

    public boolean isMythicMob(Entity e) {
        return this.mm.getAPIHelper().isMythicMob(e);
    }

    public String getMythicNameByEntity(Entity e) {
        return this.mm.getAPIHelper().getMythicMobInstance(e).getType().getInternalName();
    }

    public MythicMob getMythicInstance(Entity e) {
        return this.mm.getAPIHelper().getMythicMobInstance(e).getType();
    }

    public boolean isDropTable(String table) {
        return (this.mm.getDropManager().getDropTable(table) != null && MythicMobs.inst().getDropManager().getDropTable(table).isPresent());
    }

    public int getLevel(Entity e) {
        return (int) this.mm.getAPIHelper().getMythicMobInstance(e).getLevel();
    }

    public void castSkill(Entity e, String skill) {
        this.mm.getAPIHelper().castSkill(e, skill);
    }

    public void setSkillDamage(Entity e, double d) {
        if (!isMythicMob(e))
            return;
        ActiveMob am1 = this.mm.getMobManager().getMythicMobInstance(e);
        am1.setLastDamageSkillAmount(d);
    }
}
