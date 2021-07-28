//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package su.nightexpress.quantumrpg.modules.buffs;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.config.Config;
import su.nightexpress.quantumrpg.config.Lang;
import su.nightexpress.quantumrpg.modules.EModule;
import su.nightexpress.quantumrpg.modules.QModule;
import su.nightexpress.quantumrpg.modules.cmds.MExecutor;
import su.nightexpress.quantumrpg.stats.ItemStat;
import su.nightexpress.quantumrpg.types.ArmorType;
import su.nightexpress.quantumrpg.types.DamageType;
import su.nightexpress.quantumrpg.utils.Utils;

import java.util.*;

public class BuffManager extends QModule {
    private int taskId;
    private boolean resetDeath;
    private boolean buffMsg;
    private Map<Entity, List<BuffManager.Buff>> buffs;

    public BuffManager(QuantumRPG plugin, boolean enabled, MExecutor exe) {
        super(plugin, enabled, exe);
    }

    public EModule type() {
        return EModule.BUFFS;
    }

    public String name() {
        return "Buffs";
    }

    public String version() {
        return "1.0";
    }

    public boolean isDropable() {
        return false;
    }

    public boolean isResolvable() {
        return false;
    }

    public void updateCfg() {
    }

    public void setup() {
        this.buffs = new HashMap();
        this.setupCfg();
        this.startTask();
    }

    public void shutdown() {
        this.stopTask();
        this.buffs = null;
    }

    private void setupCfg() {
        FileConfiguration cfg = this.getCfg().getConfig();
        this.resetDeath = cfg.getBoolean("general.reset-on-death", true);
        this.buffMsg = cfg.getBoolean("general.messages-enabled", true);
    }

    private void startTask() {
        this.taskId = this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, new Runnable() {
            public void run() {
                Map<Entity, List<BuffManager.Buff>> map = BuffManager.this.getBuffs();
                Iterator var3 = map.keySet().iterator();

                while (true) {
                    while (var3.hasNext()) {
                        Entity e = (Entity) var3.next();
                        if (e != null && e.isValid()) {
                            Iterator var5 = (new ArrayList((Collection) map.get(e))).iterator();

                            while (var5.hasNext()) {
                                BuffManager.Buff b = (BuffManager.Buff) var5.next();
                                if (b.getTimeSec() - 1 < 0) {
                                    BuffManager.this.resetBuff(e, b.getType(), b.getValue());
                                } else {
                                    b.setTimeSec(b.getTimeSec() - 1);
                                }
                            }
                        } else {
                            BuffManager.this.resetBuff(e);
                        }
                    }

                    return;
                }
            }
        }, 10L, 20L);
    }

    private void stopTask() {
        this.plugin.getServer().getScheduler().cancelTask(this.taskId);
    }

    public boolean addBuff(Entity p, BuffManager.BuffType type, String value, double mod, int time, boolean plus) {
        if (!this.isValidBuff(type, value)) {
            return false;
        } else if (plus) {
            double has = this.getBuffValue(p, type, value);
            double sum = mod + has;
            return this.addBuff(p, type, value, sum, time, false);
        } else {
            this.resetBuff(p, type, value);
            List<BuffManager.Buff> list = this.getBuffs(p);
            BuffManager.Buff b = new BuffManager.Buff(type, value, mod, time);
            list.add(b);
            this.buffs.put(p, list);
            if (this.buffMsg) {
                String name = this.getBuffName(type, value);
                this.out(p, Lang.Buffs_Get.toMsg().replace("%type%", name).replace("%time%", String.valueOf(time)).replace("%value%", String.valueOf(Utils.round3(mod))));
            }

            return true;
        }
    }

    public void resetBuff(Entity e, BuffManager.BuffType type, String value) {
        List<BuffManager.Buff> list = this.getBuffs(e, type, value);
        this.resetBuff(e, list);
    }

    public void resetBuff(Entity e, BuffManager.BuffType type) {
        List<BuffManager.Buff> list = this.getBuffs(e, type);
        this.resetBuff(e, list);
    }

    public void resetBuff(Entity e) {
        List<BuffManager.Buff> list = this.getBuffs(e);
        this.resetBuff(e, list);
    }

    private void resetBuff(Entity e, List<BuffManager.Buff> reset) {
        List<BuffManager.Buff> list = this.getBuffs(e);
        Iterator var5 = (new ArrayList(reset)).iterator();

        while (var5.hasNext()) {
            BuffManager.Buff a = (BuffManager.Buff) var5.next();
            list.remove(a);
            if (this.buffMsg) {
                String name = this.getBuffName(a.getType(), a.getValue());
                this.out(e, Lang.Buffs_End.toMsg().replace("%type%", name));
            }
        }

        if (list.isEmpty()) {
            this.buffs.remove(e);
        } else {
            this.buffs.put(e, list);
        }

    }

    public Map<Entity, List<BuffManager.Buff>> getBuffs() {
        return this.buffs;
    }

    public List<BuffManager.Buff> getBuffs(Entity e) {
        return (List) (this.buffs.containsKey(e) ? (List) this.buffs.get(e) : new ArrayList());
    }

    public List<BuffManager.Buff> getBuffs(Entity e, BuffManager.BuffType type) {
        List<BuffManager.Buff> valid = new ArrayList();
        List<BuffManager.Buff> list = this.getBuffs(e);
        Iterator var6 = list.iterator();

        while (var6.hasNext()) {
            BuffManager.Buff b = (BuffManager.Buff) var6.next();
            if (b.getType() == type) {
                valid.add(b);
            }
        }

        return valid;
    }

    public List<BuffManager.Buff> getBuffs(Entity e, BuffManager.BuffType type, String value) {
        List<BuffManager.Buff> valid = new ArrayList();
        List<BuffManager.Buff> list = this.getBuffs(e, type);
        Iterator var7 = list.iterator();

        while (var7.hasNext()) {
            BuffManager.Buff b = (BuffManager.Buff) var7.next();
            if (b.getValue().equalsIgnoreCase(value)) {
                valid.add(b);
            }
        }

        return valid;
    }

    public double getBuffValue(Entity e, BuffManager.BuffType type, String value) {
        double d = 0.0D;

        BuffManager.Buff b;
        for (Iterator var7 = this.getBuffs(e, type, value).iterator(); var7.hasNext(); d = b.getModifier()) {
            b = (BuffManager.Buff) var7.next();
        }

        return d;
    }

    private String getBuffName(BuffManager.BuffType type, String value) {
        String name = value;
        switch (type.ordinal()) {
            case 1:
                try {
                    ItemStat is = ItemStat.valueOf(value.toUpperCase());
                    name = is.getName();
                } catch (IllegalArgumentException var5) {
                }
                break;
            case 2:
                DamageType dt = Config.getDamageTypeById(value);
                if (dt != null) {
                    name = dt.getName();
                }
                break;
            case 3:
                ArmorType at = Config.getArmorTypeById(value);
                if (at != null) {
                    name = at.getName();
                }
        }

        return name;
    }

    private boolean isValidBuff(BuffManager.BuffType type, String value) {
        switch (type.ordinal()) {
            case 1:
                try {
                    ItemStat.valueOf(value.toUpperCase());
                    return true;
                } catch (IllegalArgumentException var4) {
                    return false;
                }
            case 2:
                DamageType dt = Config.getDamageTypeById(value);
                if (dt != null) {
                    return true;
                }

                return false;
            case 3:
                ArmorType at = Config.getArmorTypeById(value);
                if (at != null) {
                    return true;
                }

                return false;
            default:
                return false;
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        if (this.resetDeath) {
            Player p = e.getEntity();
            Iterator var4 = this.getBuffs(p).iterator();

            while (var4.hasNext()) {
                BuffManager.Buff b = (BuffManager.Buff) var4.next();
                this.resetBuff(p, b.getType(), b.getValue());
            }
        }

    }

    public static enum BuffType {
        ITEM_STAT,
        DAMAGE,
        DEFENSE;

        private BuffType() {
        }
    }

    public class Buff {
        private BuffManager.BuffType type;
        private String value;
        private double modifier;
        private int time;

        public Buff(BuffManager.BuffType type, String value, double modifier, int time) {
            this.setType(type);
            this.setValue(value);
            this.setModifier(modifier);
            this.setTimeSec(time);
        }

        public BuffManager.BuffType getType() {
            return this.type;
        }

        public void setType(BuffManager.BuffType type) {
            this.type = type;
        }

        public String getValue() {
            return this.value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public double getModifier() {
            return this.modifier;
        }

        public void setModifier(double modifier) {
            this.modifier = modifier;
        }

        public int getTimeSec() {
            return this.time;
        }

        public void setTimeSec(int time) {
            this.time = time;
        }
    }
}
