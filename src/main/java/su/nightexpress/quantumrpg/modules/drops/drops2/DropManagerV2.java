package su.nightexpress.quantumrpg.modules.drops.drops2;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.api.DivineItemsAPI;
import su.nightexpress.quantumrpg.api.EntityAPI;
import su.nightexpress.quantumrpg.api.ItemAPI;
import su.nightexpress.quantumrpg.config.Config;
import su.nightexpress.quantumrpg.config.JYML;
import su.nightexpress.quantumrpg.hooks.EHook;
import su.nightexpress.quantumrpg.hooks.HookUtils;
import su.nightexpress.quantumrpg.hooks.external.MythicMobsHook;
import su.nightexpress.quantumrpg.hooks.external.WorldGuardHook;
import su.nightexpress.quantumrpg.listeners.DamageMeta;
import su.nightexpress.quantumrpg.modules.EModule;
import su.nightexpress.quantumrpg.modules.QModule;
import su.nightexpress.quantumrpg.modules.arrows.ArrowManager;
import su.nightexpress.quantumrpg.modules.cmds.MExecutor;
import su.nightexpress.quantumrpg.modules.drops.drops2.objects.Drop;
import su.nightexpress.quantumrpg.modules.drops.drops2.objects.DropGroup;
import su.nightexpress.quantumrpg.modules.drops.drops2.objects.DropItem;
import su.nightexpress.quantumrpg.modules.drops.drops2.objects.NpcDrop;
import su.nightexpress.quantumrpg.stats.ItemStat;
import su.nightexpress.quantumrpg.types.QCondition;
import su.nightexpress.quantumrpg.utils.DamageUtils;
import su.nightexpress.quantumrpg.utils.MetaUtils;
import su.nightexpress.quantumrpg.utils.Utils;
import su.nightexpress.quantumrpg.utils.logs.LogType;
import su.nightexpress.quantumrpg.utils.logs.LogUtil;

import java.util.*;

public class DropManagerV2 extends QModule {
    private static final String META_DROP_MOB = "QRPG_NO_MOB_DROP";
    private Map<String, Float> mult;
    private Map<String, NpcDrop> mobs;
    private Map<String, GDropGroup> tables;

    public DropManagerV2(QuantumRPG plugin, boolean enabled, MExecutor exe) {
        super(plugin, enabled, exe);
    }

    public EModule type() {
        return EModule.DROPS;
    }

    public String name() {
        return "Drops v2";
    }

    public String version() {
        return "2.0.0";
    }

    public boolean isResolvable() {
        return false;
    }

    public void setup() {
        this.plugin.getCM().extract("modules/" + getId() + "/mobs");
        this.plugin.getCM().extract("modules/" + getId() + "/tables");
        this.mult = new HashMap<>();
        this.mobs = new HashMap<>();
        this.tables = new HashMap<>();
        JYML cfg_main = this.cfg.getConfig();
        for (String s : cfg_main.getSection("multipliers")) {
            double d = cfg_main.getDouble("multipliers." + s);
            this.mult.put(s.toLowerCase(), Float.valueOf((float) d));
        }
        for (JYML cfg : JYML.getFilesFolder(String.valueOf(getFullPath()) + "/tables/")) {
            GDropGroup dg = loadDropGroup(cfg);
            if (dg == null)
                continue;
            this.tables.put(dg.getId(), dg);
        }
        for (JYML cfg : JYML.getFilesFolder(String.valueOf(getFullPath()) + "/mobs/")) {
            NpcDrop npc = loadFromConfig(cfg);
            if (npc == null)
                continue;
            String id = cfg.getFile().getName().replace(".yml", "").toLowerCase();
            this.mobs.put(id, npc);
        }
        LogUtil.send("Drop Mobs Loaded: &a" + this.mobs.size(), LogType.INFO);
        LogUtil.send("Drop Tables Loaded: &a" + this.tables.size(), LogType.INFO);
    }

    public void updateCfg() {
    }

    public void shutdown() {
        this.mult.clear();
        this.mobs.clear();
        this.tables.clear();
    }

    private GDropGroup loadDropGroup(JYML cfg) {
        String file = cfg.getFile().getName().replace(".yml", "");
        String name = cfg.getString("name", "");
        boolean dr_e = cfg.getBoolean("roll-once");
        boolean t_lvl_penal = cfg.getBoolean("level-penalty.enabled");
        int t_lvl_penalVar = cfg.getInt("level-penalty.variance");
        List<String> dr_worlds = cfg.getStringList("world-whitelist");
        List<String> dr_biomes = cfg.getStringList("biome-whitelist");
        List<String> dr_reg = cfg.getStringList("region-blacklist");
        List<Drop> drop = new ArrayList<>();
        for (String id : cfg.getSection("items")) {
            EModule type;
            String path = "items." + id + ".";
            float we = (float) cfg.getDouble(String.valueOf(path) + "chance");
            int minAmount = cfg.getInt(String.valueOf(path) + "min-amount", 1);
            int maxAmount = cfg.getInt(String.valueOf(path) + "max-amount", 1);
            String item = cfg.getString(String.valueOf(path) + "item-id");
            String t = cfg.getString(String.valueOf(path) + "type").toUpperCase();
            try {
                type = EModule.valueOf(t);
            } catch (IllegalArgumentException ex) {
                LogUtil.send("Invalid drop item type &f" + t + " &7for item &f" + id, LogType.ERROR);
                continue;
            }
            String lvl_min = cfg.getString(String.valueOf(path) + "min-level", "-1");
            String lvl_max = cfg.getString(String.valueOf(path) + "max-level", "-1");
            List<String> conds = cfg.getStringList(String.valueOf(path) + "additional-conditions");
            List<String> acts = cfg.getStringList(String.valueOf(path) + "actions-on-drop");
            GDrop di = new GDrop(
                    item, minAmount, maxAmount, we,

                    type, lvl_min, lvl_max, conds, acts);
            drop.add(di);
        }
        return new GDropGroup(
                file, name,
                dr_e, dr_worlds, dr_biomes, dr_reg,

                drop,

                t_lvl_penal, t_lvl_penalVar);
    }

    private NpcDrop loadFromConfig(JYML cfg) {
        float dr_chance = (float) cfg.getDouble("chance");
        boolean dr_e = cfg.getBoolean("roll-once");
        List<String> dr_en = cfg.getStringList("vanilla-mobs");
        List<String> dr_mm = cfg.getStringList("mythic-mobs");
        List<String> dr_reasons = cfg.getStringList("prevent-from");
        List<DropGroup> dr_tables = new ArrayList<>();
        for (String table : cfg.getStringList("drop-tables")) {
            GDropGroup dt = getTableById(table);
            if (dt == null) {
                LogUtil.send("Invalid drop table &f" + table + " &7in &f" + cfg.getFile().getName(), LogType.ERROR);
                continue;
            }
            dr_tables.add(dt);
        }
        if (dr_tables.isEmpty()) {
            LogUtil.send("Empty drop tables in &f" + cfg.getFile().getName(), LogType.ERROR);
            return null;
        }
        return new NpcDrop(
                dr_chance, dr_e,

                dr_en, dr_mm, dr_reasons,

                dr_tables);
    }

    public GDropGroup getTableById(String id) {
        return this.tables.get(id.toLowerCase());
    }

    private NpcDrop getDropsForEntity(Entity e) {
        String id;
        if (!(e instanceof LivingEntity))
            return null;
        boolean mythic = false;
        if (EHook.MYTHIC_MOBS.isEnabled() && ((MythicMobsHook) HookUtils.getHook(MythicMobsHook.class)).isMythicMob(e)) {
            MythicMobsHook mm = (MythicMobsHook) HookUtils.getHook(MythicMobsHook.class);
            id = mm.getMythicNameByEntity(e);
            mythic = true;
        } else {
            id = e.getType().name();
        }
        for (NpcDrop dm : this.mobs.values()) {
            List<String> list;
            if (mythic) {
                list = dm.getMythic();
            } else {
                list = dm.getEntities();
            }
            if (list.contains("ALL"))
                return dm;
            if (!mythic) {
                if (e instanceof org.bukkit.entity.Animals &&
                        list.contains("ANIMALS"))
                    return dm;
                if (e instanceof org.bukkit.entity.Monster &&
                        list.contains("MONSTERS"))
                    return dm;
            }
            if (list.contains(id))
                return dm;
        }
        return null;
    }

    private boolean checkConditions(Player p, LivingEntity droper, GDrop di) {
        for (String s : di.getConditions()) {
            int m_lvl;
            String reg, type, cond;
            boolean bo;
            int lvl;
            String str1;
            if (EHook.PLACEHOLDER_API.isEnabled())
                s = PlaceholderAPI.setPlaceholders(p, s);
            String b = s.split(" ")[0];
            b = b.replace("[", "").replace("]", "");
            QCondition at = null;
            try {
                at = QCondition.valueOf(b);
            } catch (IllegalArgumentException ex) {
                continue;
            }
            String[] values = s.split(" ");
            switch (at) {
                case MYTHIC_MOB_LEVEL:
                    if (EHook.MYTHIC_MOBS.isEnabled()) {
                        MythicMobsHook mm = (MythicMobsHook) EHook.MYTHIC_MOBS.getHook();
                        if (mm.isMythicMob((Entity) droper)) {
                            int i = mm.getLevel((Entity) droper);
                            String str2 = values[1];
                            int j = 0;
                            try {
                                j = Integer.parseInt(values[2]);
                            } catch (IllegalArgumentException ex) {
                                continue;
                            }
                            String str3;
                            switch ((str3 = str2).hashCode()) {
                                case 60:
                                    if (!str3.equals("<"))
                                        continue;
                                    if (i > j)
                                        return false;
                                case 61:
                                    if (!str3.equals("="))
                                        continue;
                                    if (i != j)
                                        return false;
                                case 62:
                                    if (!str3.equals(">"))
                                        continue;
                                    if (i < j)
                                        return false;
                                case 1921:
                                    if (!str3.equals("<="))
                                        continue;
                                    if (i >= j)
                                        return false;
                                case 1983:
                                    if (!str3.equals(">="))
                                        continue;
                                    if (i <= j)
                                        return false;
                            }
                        }
                    }
                case PLAYER_EXP_LEVEL:
                    m_lvl = p.getLevel();
                    cond = values[1];
                    lvl = 0;
                    try {
                        lvl = Integer.parseInt(values[2]);
                    } catch (IllegalArgumentException ex) {
                        continue;
                    }
                    switch ((str1 = cond).hashCode()) {
                        case 60:
                            if (!str1.equals("<"))
                                continue;
                            if (m_lvl > lvl)
                                return false;
                        case 61:
                            if (!str1.equals("="))
                                continue;
                            if (m_lvl != lvl)
                                return false;
                        case 62:
                            if (!str1.equals(">"))
                                continue;
                            if (m_lvl < lvl)
                                return false;
                        case 1921:
                            if (!str1.equals("<="))
                                continue;
                            if (m_lvl >= lvl)
                                return false;
                        case 1983:
                            if (!str1.equals(">="))
                                continue;
                            if (m_lvl <= lvl)
                                return false;
                    }
                case PLAYER_RPG_LEVEL:
                    m_lvl = Config.getLevelPlugin().getLevel(p);
                    cond = values[1];
                    lvl = 0;
                    try {
                        lvl = Integer.parseInt(values[2]);
                    } catch (IllegalArgumentException ex) {
                        continue;
                    }
                    switch ((str1 = cond).hashCode()) {
                        case 60:
                            if (!str1.equals("<"))
                                continue;
                            if (m_lvl > lvl)
                                return false;
                        case 61:
                            if (!str1.equals("="))
                                continue;
                            if (m_lvl != lvl)
                                return false;
                        case 62:
                            if (!str1.equals(">"))
                                continue;
                            if (m_lvl < lvl)
                                return false;
                        case 1921:
                            if (!str1.equals("<="))
                                continue;
                            if (m_lvl >= lvl)
                                return false;
                        case 1983:
                            if (!str1.equals(">="))
                                continue;
                            if (m_lvl <= lvl)
                                return false;
                    }
                case IN_WG_REGION:
                    if (EHook.WORLD_GUARD.isEnabled()) {
                        WorldGuardHook wg = (WorldGuardHook) EHook.WORLD_GUARD.getHook();
                        String str = values[1];
                        boolean bool = Boolean.valueOf(values[2]).booleanValue();
                        if (bool) {
                            if (!wg.isInRegion((LivingEntity) p, str))
                                return false;
                            continue;
                        }
                        if (wg.isInRegion((LivingEntity) p, str))
                            return false;
                    }
                case IN_WORLD:
                    reg = values[1];
                    bo = Boolean.valueOf(values[2]).booleanValue();
                    if (bo) {
                        if (!p.getWorld().getName().equalsIgnoreCase(reg))
                            return false;
                        continue;
                    }
                    if (p.getWorld().getName().equalsIgnoreCase(reg))
                        return false;
                default:
                case ENTITY_TYPE:
                    type = values[1];
                    bo = Boolean.valueOf(values[2]).booleanValue();
                    if (bo) {
                        if (!droper.getType().name().equalsIgnoreCase(type))
                            return false;
                        continue;
                    }
                    if (droper.getType().name().equalsIgnoreCase(type))
                        return false;
            }
        }
        return true;
    }

    private float getMultiplier(Player p, LivingEntity dead) {
        float mult = 0.0F;
        String group = HookUtils.getGroup(p).toLowerCase();
        if (this.mult.containsKey(group))
            mult += ((Float) this.mult.get(group)).floatValue();
        ArrowManager.QArrow arrow = null;
        DamageMeta meta = MetaUtils.getDamageMeta((Entity) dead);
        if (meta != null)
            arrow = meta.getArrow();
        mult = (float) (mult + EntityAPI.getItemStat((LivingEntity) p, ItemStat.LOOT_RATE, arrow) / 100.0D);
        if (mult >= 0.0F && mult < 1.0F)
            mult++;
        return mult;
    }

    private int getEntityLevel(LivingEntity li) {
        if (EHook.MYTHIC_MOBS.isEnabled()) {
            MythicMobsHook mm = (MythicMobsHook) EHook.MYTHIC_MOBS.getHook();
            if (mm.isMythicMob((Entity) li))
                return mm.getLevel((Entity) li);
        }
        return 1;
    }

    private List<ItemStack> methodRoll(Player killer, LivingEntity dead) {
        List<ItemStack> loot = new ArrayList<>();
        NpcDrop mob = getDropsForEntity((Entity) dead);
        if (mob == null)
            return loot;
        Set<DropItem> drop = new HashSet<>();
        int index = 0;
        float modifier = getMultiplier(killer, dead);
        index = mob.dropCalculator(killer, dead, drop, index, modifier);
        for (DropItem item : drop) {
            GDrop template = (GDrop) item.getDropTemplate();
            if (!checkConditions(killer, dead, template))
                continue;
            String id = template.getItemId();
            int lvl = template.getLevel(killer, dead);
            ItemStack is = ItemAPI.getItemByModule(template.getType(), id, lvl, -1);
            if (is == null || is.getType() == Material.AIR)
                continue;
            template.executeActions(killer);
            loot.add(is);
        }
        return loot;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDropDeath(EntityDeathEvent e) {
        LivingEntity dead = e.getEntity();
        if (dead.hasMetadata("QRPG_NO_MOB_DROP"))
            return;
        if (dead.getKiller() == null)
            return;
        e.getDrops().addAll(methodRoll(dead.getKiller(), dead));
    }

    @EventHandler(ignoreCancelled = true)
    public void onDropSpawn(CreatureSpawnEvent e) {
        LivingEntity li = e.getEntity();
        NpcDrop mob = getDropsForEntity((Entity) li);
        if (mob == null)
            return;
        String reason = e.getSpawnReason().name();
        if (mob.getReasons().contains(reason))
            li.setMetadata("QRPG_NO_MOB_DROP", (MetadataValue) new FixedMetadataValue((Plugin) this.plugin, "yes"));
    }

    public class GDropGroup extends DropGroup {
        private boolean lvl_penal;

        private int lvl_penalVar;

        public GDropGroup(String id, String name, boolean rollOnce, List<String> worlds_white, List<String> biomes_white, List<String> regions_black, List<Drop> drop, boolean lvl_penal, int lvl_penalVar) {
            super(id, name, rollOnce, worlds_white, biomes_white, regions_black, drop);
            this.lvl_penal = lvl_penal;
            this.lvl_penalVar = lvl_penalVar;
        }

        public boolean isLevelPenalty() {
            return this.lvl_penal;
        }

        public int getPenaltyVariance() {
            return this.lvl_penalVar;
        }

        protected boolean checkForLocation(LivingEntity npc) {
            if (!super.checkForLocation(npc))
                return false;
            if (isLevelPenalty() &&
                    npc.getKiller() != null) {
                Player p = npc.getKiller();
                int penal = getPenaltyVariance();
                int mob_lvl = DropManagerV2.this.getEntityLevel(npc);
                int pl_lvl = Config.getLevelPlugin().getLevel(p);
                if (pl_lvl > mob_lvl && pl_lvl - mob_lvl >= penal)
                    return false;
            }
            return true;
        }
    }

    public class GDrop extends Drop {
        protected EModule type;

        private String lvl_min;

        private String lvl_max;

        private List<String> conds;

        private List<String> acts;

        public GDrop(String itemId, int minAmount, int maxAmount, float chance, EModule type, String lvl_min, String lvl_max, List<String> conds, List<String> acts) {
            super(itemId, minAmount, maxAmount, chance);
            this.type = type;
            this.lvl_min = lvl_min;
            this.lvl_max = lvl_max;
            this.conds = conds;
            this.acts = acts;
        }

        public EModule getType() {
            return this.type;
        }

        public int getLvlMin(Player p, LivingEntity src) {
            int p_lvl = Config.g_LevelPlugin.getLevel(p);
            int e_lvl = DropManagerV2.this.getEntityLevel(src);
            String ex = this.lvl_min.replace("%mob_lvl%", String.valueOf(e_lvl)).replace("%player_lvl%", String.valueOf(p_lvl));
            return (int) DamageUtils.eval(ex);
        }

        public int getLvlMax(Player p, LivingEntity src) {
            int p_lvl = Config.g_LevelPlugin.getLevel(p);
            int e_lvl = DropManagerV2.this.getEntityLevel(src);
            String ex = this.lvl_max.replace("%mob_lvl%", String.valueOf(e_lvl)).replace("%player_lvl%", String.valueOf(p_lvl));
            return (int) DamageUtils.eval(ex);
        }

        public int getLevel(Player p, LivingEntity src) {
            int min = getLvlMin(p, src);
            int max = getLvlMax(p, src);
            if (min < -1)
                min = 0;
            if (max < 0)
                return -1;
            return Utils.randInt(min, max);
        }

        public List<String> getConditions() {
            return this.conds;
        }

        public void executeActions(Player p) {
            if (this.acts.isEmpty())
                return;
            DivineItemsAPI.executeActions((Entity) p, this.acts, null);
        }
    }
}
