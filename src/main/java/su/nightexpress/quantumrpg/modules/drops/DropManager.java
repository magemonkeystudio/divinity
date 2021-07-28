package su.nightexpress.quantumrpg.modules.drops;

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
import su.nightexpress.quantumrpg.modules.EModule;
import su.nightexpress.quantumrpg.modules.QModule;
import su.nightexpress.quantumrpg.modules.cmds.MExecutor;
import su.nightexpress.quantumrpg.stats.ItemStat;
import su.nightexpress.quantumrpg.types.QCondition;
import su.nightexpress.quantumrpg.utils.DamageUtils;
import su.nightexpress.quantumrpg.utils.Utils;
import su.nightexpress.quantumrpg.utils.logs.LogType;

import java.util.*;

public class DropManager extends QModule {
    private static final String TABLE_BLACK = "NO_DROP:";
    private Map<String, Double> mult;
    private boolean m_roll_once;
    private Map<EModule, Double> module_rate;
    private Map<EModule, List<DropTable>> drops;
    private double global_chance;

    public DropManager(QuantumRPG plugin, boolean enabled, MExecutor exe) {
        super(plugin, enabled, exe);
    }

    public EModule type() {
        return EModule.DROPS;
    }

    public String name() {
        return "Drops";
    }

    public String version() {
        return "1.1.0";
    }

    public boolean isResolvable() {
        return false;
    }

    public void setup() {
        setupMain();
    }

    public void updateCfg() {
        JYML cfg = this.cfg.getConfig();
        byte b;
        int i;
        EModule[] arrayOfEModule;
        for (i = (arrayOfEModule = EModule.values()).length, b = 0; b < i; ) {
            EModule e = arrayOfEModule[b];
            QModule q = this.plugin.getMM().getModule(e);
            if (q != null && q.isDropable())
                if (!cfg.contains("global-rates." + e.name()))
                    cfg.set("global-rates." + e.name(), Double.valueOf(5.0D));
            b++;
        }
        cfg.addMissing("global-drop-chance", Double.valueOf(20.0D));
        this.cfg.save();
    }

    public void shutdown() {
        this.drops = null;
        this.mult = null;
    }

    private void setupMain() {
        this.plugin.getCM().extract("modules/" + getId() + "/tables");
        this.mult = new HashMap<>();
        JYML cfg = this.cfg.getConfig();
        this.global_chance = cfg.getDouble("global-drop-chance", 20.0D);
        for (String s : cfg.getConfigurationSection("multipliers").getKeys(false)) {
            double d = cfg.getDouble("multipliers." + s);
            this.mult.put(s.toLowerCase(), Double.valueOf(d));
        }
        this.m_roll_once = cfg.getBoolean("roll-once");
        this.module_rate = new HashMap<>();
        if (cfg.isConfigurationSection("global-rates"))
            for (String s : cfg.getConfigurationSection("global-rates").getKeys(false)) {
                EModule e;
                try {
                    e = EModule.valueOf(s.toUpperCase());
                } catch (IllegalArgumentException ex) {
                    continue;
                }
                if (!e.isEnabled())
                    continue;
                QModule q = this.plugin.getMM().getModule(e);
                if (q == null || !q.isDropable())
                    continue;
                this.module_rate.put(e, Double.valueOf(cfg.getDouble("global-rates." + s)));
            }
        setupTables();
    }

    private void setupTables() {
        this.drops = new HashMap<>();
        for (JYML cfg : JYML.getFilesFolder(String.valueOf(getFullPath()) + "/tables/")) {
            EModule e;
            List<DropTable> list;
            if (!cfg.isConfigurationSection("items")) {
                log("Empty drop table: &f" + cfg.getFileName(), LogType.WARN);
                continue;
            }
            try {
                e = EModule.valueOf(cfg.getString("module").toUpperCase());
            } catch (IllegalArgumentException ex) {
                log("Invalid module type for table: &f" + cfg.getFileName(), LogType.ERROR);
                continue;
            }
            double t_rate = cfg.getDouble("chance");
            boolean t_once = cfg.getBoolean("roll-once");
            Set<String> t_worlds_wl = new HashSet<>(cfg.getStringList("world-whitelist"));
            Set<String> t_reg_bl = new HashSet<>(cfg.getStringList("region-whitelist"));
            Set<String> t_biome_wl = new HashSet<>(cfg.getStringList("biome-whitelist"));
            Set<String> t_et_wl = new HashSet<>(cfg.getStringList("entity-types"));
            Set<String> t_mm_wl = new HashSet<>(cfg.getStringList("mythic-mobs"));
            Set<String> t_reasons_bl = new HashSet<>(cfg.getStringList("prevent-from"));
            boolean t_lvl_penal = cfg.getBoolean("level-penalty.enabled");
            int t_lvl_penalVar = cfg.getInt("level-penalty.variance");
            Map<DropItem, Double> items = new HashMap<>();
            for (String id : cfg.getSection("items")) {
                String path = "items." + id + ".";
                double rate = cfg.getDouble(String.valueOf(path) + "chance");
                String item_id = cfg.getString(String.valueOf(path) + "item-id");
                if (item_id == null) {
                    log("Missing &fitem-id &7option for &f" + id + "&7 item!", LogType.ERROR);
                    continue;
                }
                String lvl_min = cfg.getString(String.valueOf(path) + "min-level", "-1");
                String lvl_max = cfg.getString(String.valueOf(path) + "max-level", "-1");
                List<String> conds = cfg.getStringList(String.valueOf(path) + "additional-conditions");
                List<String> acts = cfg.getStringList(String.valueOf(path) + "actions-on-drop");
                DropItem di = new DropItem(rate, item_id, lvl_min, lvl_max, conds, acts);
                items.put(di, Double.valueOf(di.getRate()));
            }
            items = Utils.sortByValue(items);
            DropTable table = new DropTable(
                    cfg.getFileName().replace(".yml", ""),
                    e,
                    t_rate,
                    t_once,

                    t_worlds_wl,
                    t_reg_bl,
                    t_biome_wl,
                    t_et_wl,
                    t_mm_wl,
                    t_reasons_bl,

                    t_lvl_penal,
                    t_lvl_penalVar,

                    items);
            if (this.drops.containsKey(e)) {
                list = this.drops.get(e);
            } else {
                list = new ArrayList<>();
            }
            list.add(table);
            this.drops.put(e, list);
        }
        log("Loaded &f" + this.drops.size() + "&7 drop tables!", LogType.INFO);
    }

    private boolean checkRegion(DropTable dm, LivingEntity li) {
        if (!EHook.WORLD_GUARD.isEnabled())
            return true;
        WorldGuardHook wg = (WorldGuardHook) EHook.WORLD_GUARD.getHook();
        for (String s : dm.getRegionsBlacklist()) {
            if (wg.isInRegion(li, s))
                return false;
        }
        return true;
    }

    private boolean checkBiome(DropTable dm, LivingEntity li) {
        String bio = li.getLocation().getBlock().getBiome().name();
        return !(!dm.getBiomesWhitelist().contains("ALL") && !dm.getBiomesWhitelist().contains(bio));
    }

    private int getEntityLevel(LivingEntity li) {
        if (EHook.MYTHIC_MOBS.isEnabled()) {
            MythicMobsHook mm = (MythicMobsHook) EHook.MYTHIC_MOBS.getHook();
            if (mm.isMythicMob((Entity) li))
                return mm.getLevel((Entity) li);
        }
        return 1;
    }

    private boolean checkEntity(DropTable dm, LivingEntity li) {
        if (EHook.MYTHIC_MOBS.isEnabled()) {
            MythicMobsHook mm = (MythicMobsHook) EHook.MYTHIC_MOBS.getHook();
            if (mm.isMythicMob((Entity) li)) {
                String name = mm.getMythicNameByEntity((Entity) li);
                if (dm.getMythicsWhitelist().contains("ALL"))
                    return true;
                if (dm.getMythicsWhitelist().contains(name))
                    return true;
                return false;
            }
        }
        if (dm.getEntitiesWhitelist().contains(li.getType().name()))
            return true;
        if (dm.getEntitiesWhitelist().contains("ALL"))
            return true;
        if (li instanceof org.bukkit.entity.Animals &&
                dm.getEntitiesWhitelist().contains("PASSIVE"))
            return true;
        if (li instanceof org.bukkit.entity.Monster &&
                dm.getEntitiesWhitelist().contains("HOSTILE"))
            return true;
        return false;
    }

    private boolean checkWorld(DropTable dm, LivingEntity li) {
        if (dm.getWorldsWhitelist().contains("ALL"))
            return true;
        return dm.getWorldsWhitelist().contains(li.getWorld().getName());
    }

    private boolean checkPenalty(Player p, LivingEntity li, DropTable dm) {
        if (dm.isLevelPenalty()) {
            int penal = dm.getPenaltyVariance();
            int mob_lvl = getEntityLevel(li);
            int pl_lvl = Config.getLevelPlugin().getLevel(p);
            if (pl_lvl > mob_lvl && pl_lvl - mob_lvl >= penal)
                return false;
        }
        return true;
    }

    private <T> List<T> getItemsByWeight(Map<T, Double> src, boolean once) {
        if (src.isEmpty())
            return Collections.emptyList();
        Set<T> set = new HashSet<>();
        for (Map.Entry<T, Double> en : src.entrySet()) {
            if (((Double) en.getValue()).doubleValue() <= 0.0D)
                set.add(en.getKey());
        }
        int amount = 1;
        if (!once)
            amount = src.size();
        for (int i = 0; i < amount; i++)
            set.add((T) Utils.getRandomItem(src, false));
        return new ArrayList<>(set);
    }

    private boolean isGlobal(Player p) {
        double loot = EntityAPI.getItemStat((LivingEntity) p, ItemStat.LOOT_RATE, null) / 100.0D;
        double global = Utils.getRandDouble(0.0D, 100.0D);
        global *= 1.0D - getMultiplier(p) - 1.0D;
        global *= 1.0D - loot;
        return (global <= this.global_chance);
    }

    public List<ItemStack> methodRoll(Player p, LivingEntity source) {
        List<ItemStack> loot = new ArrayList<>();
        if (!isGlobal(p))
            return loot;
        for (EModule mod : getItemsByWeight(this.module_rate, this.m_roll_once)) {
            if (!this.drops.containsKey(mod))
                continue;
            Map<DropTable, Double> map = new HashMap<>();
            List<DropTable> tables = this.drops.get(mod);
            for (DropTable table : tables) {
                if (source.hasMetadata("NO_DROP:" + table.getId()))
                    continue;
                if (!checkPenalty(p, source, table))
                    continue;
                if (!checkWorld(table, source))
                    continue;
                if (!checkEntity(table, source))
                    continue;
                if (!checkBiome(table, source))
                    continue;
                if (!checkRegion(table, source))
                    continue;
                map.put(table, Double.valueOf(table.getRate()));
            }
            tables = getItemsByWeight(map, true);
            for (DropTable table : tables) {
                List<DropItem> items = getItemsByWeight(table.getItemsMap(), table.isRollOnce());
                for (DropItem item : items) {
                    if (!checkConditions(p, source, item))
                        continue;
                    int lvl = item.getLevel(p, source);
                    ItemStack drop = ItemAPI.getItemByModule(mod, item.getItemId(), lvl, -1);
                    if (drop == null || drop.getType() == Material.AIR)
                        continue;
                    item.executeActions(p);
                    loot.add(drop);
                }
            }
        }
        return loot;
    }

    private double getMultiplier(Player p) {
        String group = HookUtils.getGroup(p).toLowerCase();
        if (this.mult.containsKey(group))
            return ((Double) this.mult.get(group)).doubleValue();
        return 1.0D;
    }

    private boolean checkConditions(Player p, LivingEntity droper, DropItem di) {
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
            if (at == null) {
                type = values[1];
                bo = Boolean.valueOf(values[2]).booleanValue();
                if (bo) {
                    if (!droper.getType().name().equalsIgnoreCase(type))
                        return false;
                    continue;
                }
                if (droper.getType().name().equalsIgnoreCase(type))
                    return false;
            } else {
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
                }
            }
        }
        return true;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrop(EntityDeathEvent e) {
        Player p = e.getEntity().getKiller();
        if (p == null)
            return;
        LivingEntity li = e.getEntity();
        if (e.getDrops() == null)
            return;
        e.getDrops().addAll(methodRoll(p, li));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawn(CreatureSpawnEvent e) {
        LivingEntity li = e.getEntity();
        String reason = e.getSpawnReason().name();
        for (EModule m : this.drops.keySet()) {
            List<DropTable> dm = this.drops.get(m);
            for (DropTable table : dm) {
                if (table.getReasonsBlacklist().contains(reason))
                    li.setMetadata("NO_DROP:" + table.getId(), (MetadataValue) new FixedMetadataValue((Plugin) this.plugin, "yeah"));
            }
        }
    }

    public class DropTable {
        private String id;

        private EModule e;

        private double rate;

        private boolean once;

        private Set<String> worlds_wl;

        private Set<String> reg_bl;

        private Set<String> biome_wl;

        private Set<String> et_wl;

        private Set<String> mm_wl;

        private Set<String> reasons_bl;

        private boolean lvl_penal;

        private int lvl_penalVar;

        private Map<DropManager.DropItem, Double> items;

        public DropTable(String id, EModule e, double rate, boolean once, Set<String> worlds_wl, Set<String> reg_bl, Set<String> biome_wl, Set<String> et_wl, Set<String> mm_wl, Set<String> reasons_bl, boolean lvl_penal, int lvl_penalVar, Map<DropManager.DropItem, Double> items) {
            this.id = id.toLowerCase();
            this.e = e;
            this.rate = rate;
            this.once = once;
            this.worlds_wl = worlds_wl;
            this.reg_bl = reg_bl;
            this.biome_wl = biome_wl;
            this.et_wl = et_wl;
            this.mm_wl = mm_wl;
            this.reasons_bl = reasons_bl;
            this.lvl_penal = lvl_penal;
            this.lvl_penalVar = lvl_penalVar;
            this.items = items;
        }

        public String getId() {
            return this.id;
        }

        public EModule getModule() {
            return this.e;
        }

        public double getRate() {
            return this.rate;
        }

        public boolean isRollOnce() {
            return this.once;
        }

        public Set<String> getWorldsWhitelist() {
            return this.worlds_wl;
        }

        public Set<String> getRegionsBlacklist() {
            return this.reg_bl;
        }

        public Set<String> getBiomesWhitelist() {
            return this.biome_wl;
        }

        public Set<String> getEntitiesWhitelist() {
            return this.et_wl;
        }

        public Set<String> getMythicsWhitelist() {
            return this.mm_wl;
        }

        public Set<String> getReasonsBlacklist() {
            return this.reasons_bl;
        }

        public boolean isLevelPenalty() {
            return this.lvl_penal;
        }

        public int getPenaltyVariance() {
            return this.lvl_penalVar;
        }

        public Collection<DropManager.DropItem> getItems() {
            return this.items.keySet();
        }

        public Map<DropManager.DropItem, Double> getItemsMap() {
            return this.items;
        }
    }

    public class DropItem {
        private double rate;

        private String id;

        private String lvl_min;

        private String lvl_max;

        private List<String> conds;

        private List<String> acts;

        public DropItem(double rate, String id, String lvl_min, String lvl_max, List<String> conds, List<String> acts) {
            this.rate = rate;
            this.id = id.toLowerCase();
            this.lvl_min = lvl_min;
            this.lvl_max = lvl_max;
            this.conds = conds;
            this.acts = acts;
        }

        public double getRate() {
            return this.rate;
        }

        public String getItemId() {
            return this.id;
        }

        public int getLvlMin(Player p, LivingEntity src) {
            int p_lvl = Config.g_LevelPlugin.getLevel(p);
            int e_lvl = DropManager.this.getEntityLevel(src);
            String ex = this.lvl_min.replace("%mob_lvl%", String.valueOf(e_lvl)).replace("%player_lvl%", String.valueOf(p_lvl));
            return (int) Math.max(1.0D, DamageUtils.eval(ex));
        }

        public int getLvlMax(Player p, LivingEntity src) {
            int p_lvl = Config.g_LevelPlugin.getLevel(p);
            int e_lvl = DropManager.this.getEntityLevel(src);
            String ex = this.lvl_max.replace("%mob_lvl%", String.valueOf(e_lvl)).replace("%player_lvl%", String.valueOf(p_lvl));
            return (int) Math.max(1.0D, DamageUtils.eval(ex));
        }

        public int getLevel(Player p, LivingEntity src) {
            int min = getLvlMin(p, src);
            int max = getLvlMax(p, src);
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
