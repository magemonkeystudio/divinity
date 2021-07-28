package su.nightexpress.quantumrpg.modules.active_items;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.api.DivineItemsAPI;
import su.nightexpress.quantumrpg.api.ItemAPI;
import su.nightexpress.quantumrpg.config.JYML;
import su.nightexpress.quantumrpg.config.Lang;
import su.nightexpress.quantumrpg.config.MyConfig;
import su.nightexpress.quantumrpg.modules.EModule;
import su.nightexpress.quantumrpg.modules.LeveledItem;
import su.nightexpress.quantumrpg.modules.QModuleLevel;
import su.nightexpress.quantumrpg.modules.active_items.events.QuantumPlayerActiveItemUseEvent;
import su.nightexpress.quantumrpg.modules.cmds.MExecutor;
import su.nightexpress.quantumrpg.nbt.NBTItem;
import su.nightexpress.quantumrpg.types.QClickType;
import su.nightexpress.quantumrpg.utils.Utils;
import su.nightexpress.quantumrpg.utils.logs.LogType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActiveItemManager extends QModuleLevel {
    private final String NBT_KEY_USES = "QRPG_ITEM_USES";
    private MyConfig itemsCfg;
    private HashMap<String, HashMap<String, List<ItemUsageCooldown>>> cds;

    public ActiveItemManager(QuantumRPG plugin, boolean enabled, MExecutor exe) {
        super(plugin, enabled, exe);
    }

    public EModule type() {
        return EModule.ACTIVE_ITEMS;
    }

    public String name() {
        return "Active Items";
    }

    public String version() {
        return "1.0";
    }

    public boolean isResolvable() {
        return true;
    }

    public void updateCfg() {
    }

    public void setup() {
        this.cds = new HashMap<>();
        this.itemsCfg = new MyConfig((JavaPlugin) this.plugin, "/modules/" + getId(), "items.yml");
        setupScrolls();
    }

    public void shutdown() {
        this.cds = null;
        this.itemsCfg = null;
    }

    private void setupScrolls() {
        JYML jYML = this.itemsCfg.getConfig();
        for (String o : jYML.getConfigurationSection("items").getKeys(false)) {
            String path = "items." + o + ".";
            HashMap<String, Object> vars = new HashMap<>();
            if (jYML.contains(String.valueOf(path) + "variables"))
                for (String s : jYML.getConfigurationSection(String.valueOf(path) + "variables").getKeys(false)) {
                    Object oj = jYML.get(String.valueOf(path) + "variables." + s);
                    vars.put(s, oj);
                }
            HashMap<String, Object> vars_lvl = new HashMap<>();
            if (jYML.contains(String.valueOf(path) + "variables-per-lvl"))
                for (String s : jYML.getConfigurationSection(String.valueOf(path) + "variables-per-lvl").getKeys(false)) {
                    Object oj = jYML.get(String.valueOf(path) + "variables-per-lvl." + s);
                    vars_lvl.put(s, oj);
                }
            int uses = jYML.getInt(String.valueOf(path) + "uses");
            HashMap<QClickType, ItemUsage> use = new HashMap<>();
            if (jYML.isConfigurationSection(String.valueOf(path) + "usage"))
                for (String en : jYML.getConfigurationSection(String.valueOf(path) + "usage").getKeys(false)) {
                    QClickType click = null;
                    try {
                        click = QClickType.valueOf(en.toUpperCase());
                    } catch (IllegalArgumentException ex) {
                        log("Invalid click type '" + en + "' in custom item '" + o + "'!", LogType.WARN);
                        continue;
                    }
                    String path2 = String.valueOf(path) + "usage." + en + ".";
                    int cd = jYML.getInt(String.valueOf(path2) + "cooldown");
                    List<String> actions = jYML.getStringList(String.valueOf(path2) + "actions");
                    ItemUsage iu = new ItemUsage(cd, actions);
                    use.put(click, iu);
                }
            ActiveItem sc = new ActiveItem(o, path, (FileConfiguration) jYML, type(),

                    vars,
                    vars_lvl,

                    use,
                    uses);
            this.items.put(sc.getId(), sc);
        }
        this.itemsCfg.save();
    }

    public ItemStack takeUse(ItemStack item) {
        NBTItem nbt = new NBTItem(item);
        if (!nbt.hasKey("QRPG_ITEM_USES").booleanValue())
            return item;
        int uses = nbt.getInteger("QRPG_ITEM_USES").intValue() - 1;
        if (uses <= 0) {
            item.setAmount(item.getAmount() - 1);
            return item;
        }
        String id = getItemId(item);
        int lvl = getLevel(item);
        ActiveItem s = new ActiveItem((ActiveItem) getItemById(id, ActiveItem.class));
        List<String> lore = new ArrayList<>();
        nbt.setInteger("QRPG_ITEM_USES", Integer.valueOf(uses));
        item = nbt.getItem();
        for (String s1 : s.getLore()) {
            for (String s2 : s.getVariables().keySet())
                s1 = replaceVars(s1, s2, s, lvl);
            lore.add(ChatColor.translateAlternateColorCodes('&', s1
                    .replace("%level%", String.valueOf(lvl))
                    .replace("%rlevel%", Utils.IntegerToRomanNumeral(lvl))
                    .replace("%uses%", String.valueOf(uses))));
        }
        ItemMeta meta = item.getItemMeta();
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private void cooldownNotify(Player p, ItemStack item, long left) {
        String name = Utils.getItemName(item);
        String time = Utils.getTime(left);
        String msg = Lang.CustomItems_Cooldown.toMsg().replace("%time%", time).replace("%item%", name);
        out((Entity) p, msg);
    }

    private boolean use(Player p, ItemStack item, QClickType type, ActiveItem s, int lvl, EquipmentSlot slot) {
        String id = getItemId(item);
        ActiveItem ci = (ActiveItem) getItemById(id, ActiveItem.class);
        if (ci == null || ci.getUsage(type) == null)
            return false;
        QuantumPlayerActiveItemUseEvent eve = new QuantumPlayerActiveItemUseEvent(item, p, ci, type);
        this.plugin.getPluginManager().callEvent((Event) eve);
        if (eve.isCancelled())
            return true;
        updateCooldown(p);
        if (isOnCooldown(p, item, type)) {
            long left = getCooldownLeft(p, item, type);
            cooldownNotify(p, item, left);
            return true;
        }
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
            ItemStack reuse = new ItemStack(item);
            reuse.setAmount(1);
            reuse = takeUse(reuse);
            Utils.addItem(p, reuse);
        } else {
            item = takeUse(item);
        }
        if (slot == EquipmentSlot.OFF_HAND) {
            p.getInventory().setItemInOffHand(item);
        } else {
            p.getInventory().setItemInMainHand(item);
        }
        ItemUsage iu = ci.getUsage(type);
        iu.use(p, item, s, lvl);
        setCooldown(p, item, type);
        return true;
    }

    public void updateCooldown(Player p) {
        String u = p.getUniqueId().toString();
        HashMap<String, List<ItemUsageCooldown>> map = null;
        HashMap<String, List<ItemUsageCooldown>> map_new = new HashMap<>();
        if (this.cds.containsKey(u))
            map = this.cds.get(u);
        if (map == null || map.isEmpty())
            return;
        for (Map.Entry<String, List<ItemUsageCooldown>> e : map.entrySet()) {
            String item_id = e.getKey();
            List<ItemUsageCooldown> list = e.getValue();
            for (ItemUsageCooldown i : list) {
                if (i.isExpired())
                    list.remove(i);
            }
            map_new.put(item_id, list);
        }
        this.cds.put(u, map_new);
    }

    public boolean isOnCooldown(Player p, ItemStack item, QClickType type) {
        if (!isItemOfThisModule(item))
            return false;
        String u = p.getUniqueId().toString();
        HashMap<String, List<ItemUsageCooldown>> map = null;
        if (this.cds.containsKey(u))
            map = this.cds.get(u);
        if (map == null)
            map = new HashMap<>();
        String id = getItemId(item);
        List<ItemUsageCooldown> list = map.get(id);
        if (list == null || list.isEmpty())
            return false;
        for (ItemUsageCooldown i : list) {
            if (i.getClickType() == type)
                return !i.isExpired();
        }
        return false;
    }

    public long getCooldownLeft(Player p, ItemStack item, QClickType type) {
        String u = p.getUniqueId().toString();
        HashMap<String, List<ItemUsageCooldown>> map = this.cds.get(u);
        String id = getItemId(item);
        for (ItemUsageCooldown i : map.get(id)) {
            if (i.getClickType() == type)
                return i.getTimeExpire() - System.currentTimeMillis();
        }
        return 0L;
    }

    public void setCooldown(Player p, ItemStack item, QClickType type) {
        if (!isItemOfThisModule(item))
            return;
        String u = p.getUniqueId().toString();
        HashMap<String, List<ItemUsageCooldown>> map = null;
        if (this.cds.containsKey(u))
            map = this.cds.get(u);
        if (map == null)
            map = new HashMap<>();
        String id = getItemId(item);
        ActiveItem ci = (ActiveItem) getItemById(id, ActiveItem.class);
        ItemUsage iu = ci.getUsage(type);
        if (iu == null || iu.getCooldown() <= 0)
            return;
        ItemUsageCooldown ic = new ItemUsageCooldown(id, type, iu.getCooldown());
        List<ItemUsageCooldown> list = map.get(id);
        if (list == null)
            list = new ArrayList<>();
        for (ItemUsageCooldown i : list) {
            if (i.getClickType() == type)
                list.remove(i);
        }
        list.add(ic);
        map.put(id, list);
        this.cds.put(u, map);
    }

    private String replaceVars(String action, String var, ActiveItem item, int lvl) {
        Object value = item.getVariables().get(var);
        String value_raw = value.toString();
        String value_lvl = item.getVariablesLvl().get(var).toString();
        if (item.getVariablesLvl().containsKey(var))
            if (value instanceof Integer) {
                int d = Integer.parseInt(value_raw);
                d += (lvl - 1) * Integer.parseInt(value_lvl);
                value_raw = String.valueOf(d);
            } else if (value instanceof Double) {
                double d = Double.parseDouble(value_raw);
                d += (lvl - 1) * Double.parseDouble(value_lvl);
                value_raw = String.valueOf(Utils.round3(d));
            } else {
                value_raw = value_lvl;
            }
        return action.replace("%var_" + var + "%", value_raw);
    }

    @EventHandler
    public void onUse(PlayerInteractEvent e) {
        if (e.isCancelled() && !e.getAction().name().contains("AIR"))
            return;
        ItemStack item = e.getItem();
        if (!isItemOfThisModule(item))
            return;
        Player p = e.getPlayer();
        if (!ItemAPI.canUse(item, p)) {
            e.setCancelled(true);
            return;
        }
        Action a = e.getAction();
        boolean shift = p.isSneaking();
        QClickType type = QClickType.getFromAction(a, shift);
        if (type == null)
            return;
        e.setCancelled(true);
        String id = getItemId(item);
        int lvl = getLevel(item);
        if (getItemById(id) == null) {
            p.sendMessage(String.valueOf(Lang.Prefix.toMsg()) + Lang.Other_Internal.toMsg());
            return;
        }
        ActiveItem s = new ActiveItem((ActiveItem) getItemById(id, ActiveItem.class));
        use(p, item, type, s, lvl, e.getHand());
    }

    public class ActiveItem extends LeveledItem {
        private HashMap<String, Object> vars;

        private HashMap<String, Object> vars_lvl;

        private HashMap<QClickType, ActiveItemManager.ItemUsage> use;

        private int uses;

        public ActiveItem(String id, String path, FileConfiguration cfg, EModule module, HashMap<String, Object> vars, HashMap<String, Object> vars_lvl, HashMap<QClickType, ActiveItemManager.ItemUsage> use, int uses) {
            super(id, path, cfg, module);
            setVariables(vars);
            setVariablesLvl(vars_lvl);
            this.use = use;
            setUses(uses);
        }

        public ActiveItem(ActiveItem s2) {
            super(s2.getId(), s2.getName(), s2.getLore(), ActiveItemManager.this.type(), s2.getMinLevel(), s2.getMaxLevel());
            this.material = s2.getMaterial();
            setVariables(s2.getVariables());
            setVariablesLvl(s2.getVariablesLvl());
            this.use = s2.getUsage();
            setUses(s2.getUses());
        }

        public HashMap<String, Object> getVariables() {
            return this.vars;
        }

        public void setVariables(HashMap<String, Object> vars) {
            this.vars = vars;
        }

        public HashMap<String, Object> getVariablesLvl() {
            return this.vars_lvl;
        }

        public void setVariablesLvl(HashMap<String, Object> vars_lvl) {
            this.vars_lvl = vars_lvl;
        }

        public HashMap<QClickType, ActiveItemManager.ItemUsage> getUsage() {
            return this.use;
        }

        public ActiveItemManager.ItemUsage getUsage(QClickType type) {
            return this.use.get(type);
        }

        public int getUses() {
            return this.uses;
        }

        public void setUses(int uses) {
            this.uses = uses;
        }

        protected ItemStack build(int lvl) {
            ItemStack item = super.build(lvl);
            if (item.getType() == Material.AIR)
                return item;
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(meta.getDisplayName());
            List<String> lor = new ArrayList<>();
            if (meta.hasLore())
                lor = meta.getLore();
            List<String> lore = new ArrayList<>();
            for (String s1 : lor) {
                for (String s2 : getVariables().keySet())
                    s1 = ActiveItemManager.this.replaceVars(s1, s2, this, lvl);
                s1 = replacePlaceholders(s1, lvl);
                lore.add(ChatColor.translateAlternateColorCodes('&', s1
                        .replace("%uses%", String.valueOf(getUses()))));
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
            NBTItem nbt = new NBTItem(item);
            nbt.setInteger("QRPG_ITEM_USES", Integer.valueOf(getUses()));
            item = nbt.getItem();
            return item;
        }
    }

    public class ItemUsageCooldown {
        private String id;

        private QClickType click;

        private long time;

        public ItemUsageCooldown(String id, QClickType click, int cd) {
            this.id = id;
            this.click = click;
            this.time = System.currentTimeMillis() + 1000L * cd;
        }

        public String getItemId() {
            return this.id;
        }

        public QClickType getClickType() {
            return this.click;
        }

        public long getTimeExpire() {
            return this.time;
        }

        public boolean isExpired() {
            return (System.currentTimeMillis() > this.time);
        }
    }

    public class ItemUsage {
        private int cd;

        private List<String> acts;

        public ItemUsage(int cd, List<String> acts) {
            this.cd = cd;
            this.acts = acts;
        }

        public int getCooldown() {
            return this.cd;
        }

        public List<String> getActions() {
            return this.acts;
        }

        public void use(Player p, ItemStack item, ActiveItemManager.ActiveItem s, int lvl) {
            List<String> actions_original = new ArrayList<>(this.acts);
            List<String> actions_replaced = new ArrayList<>();
            for (String s1 : actions_original) {
                for (String s2 : s.getVariables().keySet())
                    s1 = ActiveItemManager.this.replaceVars(s1, s2, s, lvl);
                actions_replaced.add(s1);
            }
            DivineItemsAPI.executeActions((Entity) p, actions_replaced, item);
        }
    }
}
