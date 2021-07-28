package su.nightexpress.quantumrpg.modules.gems;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.api.ItemAPI;
import su.nightexpress.quantumrpg.config.Config;
import su.nightexpress.quantumrpg.config.JYML;
import su.nightexpress.quantumrpg.config.Lang;
import su.nightexpress.quantumrpg.config.MyConfig;
import su.nightexpress.quantumrpg.gui.GUIItem;
import su.nightexpress.quantumrpg.gui.GUIUtils;
import su.nightexpress.quantumrpg.modules.EModule;
import su.nightexpress.quantumrpg.modules.QModuleSocket;
import su.nightexpress.quantumrpg.modules.SocketItem;
import su.nightexpress.quantumrpg.modules.SocketSettings;
import su.nightexpress.quantumrpg.modules.cmds.MExecutor;
import su.nightexpress.quantumrpg.nbt.NBTItem;
import su.nightexpress.quantumrpg.stats.ItemStat;
import su.nightexpress.quantumrpg.types.ArmorType;
import su.nightexpress.quantumrpg.types.DamageType;
import su.nightexpress.quantumrpg.types.DestroyType;
import su.nightexpress.quantumrpg.types.QSlotType;
import su.nightexpress.quantumrpg.utils.Utils;
import su.nightexpress.quantumrpg.utils.logs.LogType;

import java.util.*;

public class GemManager extends QModuleSocket {
    private final String NBT_KEY_VAR_ATT = "DIVINE_VAR_ATT_";
    private final String NBT_KEY_ITEM_GEM = "GEM_";
    private final String NBT_KEY_GEM_ILD = "DIVINE_GEM_ILD";
    private MyConfig gemsCfg;

    public GemManager(QuantumRPG plugin, boolean enabled, MExecutor exe) {
        super(plugin, enabled, exe);
    }

    public EModule type() {
        return EModule.GEMS;
    }

    public String name() {
        return "Gems";
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
        this.gemsCfg = new MyConfig((JavaPlugin) this.plugin, "/modules/" + getId(), "gems.yml");
        setupSettings();
        setupGems();
        setupSlot();
    }

    public void shutdown() {
        this.ss = null;
    }

    private void setupSlot() {
        QSlotType st = QSlotType.GEM;
        st.setModule(this);
        st.setHeader(getSettings().getHeader());
        st.setEmpty(getSettings().getEmptySlot());
        st.setFilled(getSettings().getFilledSlot());
    }

    protected void setupSettings() {
        JYML jYML = this.cfg.getConfig();
        String path = "general.";
        boolean same = jYML.getBoolean(String.valueOf(path) + "allow-same-gems-in-one-item");
        path = "socketing.";
        DestroyType destroy = DestroyType.CLEAR;
        try {
            destroy = DestroyType.valueOf(jYML.getString(String.valueOf(path) + "destroy-type").toUpperCase());
        } catch (IllegalArgumentException ex) {
            log("Invalid 'destroy-type' in '/gems/settings.yml'", LogType.WARN);
        }
        path = "socketing.effects.";
        boolean eff_use = jYML.getBoolean(String.valueOf(path) + "enabled");
        String eff_de_value = jYML.getString(String.valueOf(path) + "failure");
        String eff_suc_value = jYML.getString(String.valueOf(path) + "success");
        path = "socketing.sounds.";
        boolean sound_use = jYML.getBoolean(String.valueOf(path) + "enabled");
        Sound sound_de_value = Sound.BLOCK_ANVIL_BREAK;
        try {
            sound_de_value = Sound.valueOf(jYML.getString(String.valueOf(path) + "failure"));
        } catch (IllegalArgumentException ex) {
            log("Invalid sound for 'sounds.failure' in '/gems/settings.yml'", LogType.WARN);
        }
        Sound sound_suc_value = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
        try {
            sound_suc_value = Sound.valueOf(jYML.getString(String.valueOf(path) + "success"));
        } catch (IllegalArgumentException ex) {
            log("Invalid sound for 'sounds.success' in '/gems/settings.yml'", LogType.WARN);
        }
        path = "socketing.lore-format.";
        String header = ChatColor.translateAlternateColorCodes('&', jYML.getString(String.valueOf(path) + "header"));
        String empty_slot = ChatColor.translateAlternateColorCodes('&', jYML.getString(String.valueOf(path) + "empty-socket"));
        String filled_slot = ChatColor.translateAlternateColorCodes('&', jYML.getString(String.valueOf(path) + "filled-socket"));
        path = "item.";
        String display = ChatColor.translateAlternateColorCodes('&', jYML.getString(String.valueOf(path) + "name"));
        List<String> lore = jYML.getStringList(String.valueOf(path) + "lore");
        this.ss = new GemSettings(
                same,

                display,
                lore,

                destroy,

                eff_use,
                eff_de_value,
                eff_suc_value,

                sound_use,
                sound_de_value,
                sound_suc_value,

                header,
                empty_slot,
                filled_slot);
        path = "gui.";
        String g_title = ChatColor.translateAlternateColorCodes('&', jYML.getString(String.valueOf(path) + "title"));
        int g_size = jYML.getInt(String.valueOf(path) + "size");
        int item_slot = jYML.getInt(String.valueOf(path) + "item-slot");
        int source_slot = jYML.getInt(String.valueOf(path) + "source-slot");
        int result_slot = jYML.getInt(String.valueOf(path) + "result-slot");
        LinkedHashMap<String, GUIItem> items = new LinkedHashMap<>();
        if (jYML.isConfigurationSection(String.valueOf(path) + "content"))
            for (String id : jYML.getConfigurationSection(String.valueOf(path) + "content").getKeys(false)) {
                GUIItem gi = GUIUtils.getItemFromSection((FileConfiguration) jYML, id, String.valueOf(path) + "content." + id + ".");
                items.put(id, gi);
            }
        this.gui = new GSocketGUI(this, g_title, g_size, items, item_slot, source_slot, result_slot);
    }

    private void setupGems() {
        JYML jYML = this.gemsCfg.getConfig();
        if (!jYML.isConfigurationSection("gems"))
            return;
        for (String o : jYML.getConfigurationSection("gems").getKeys(false)) {
            String path = "gems." + o + ".";
            boolean enabled = jYML.getBoolean(String.valueOf(path) + "enabled");
            if (!enabled)
                continue;
            String id = o;
            String ild = ChatColor.translateAlternateColorCodes('&', jYML.getString(String.valueOf(path) + "socket-display"));
            HashMap<String, Double> vars = new HashMap<>();
            if (jYML.isConfigurationSection(String.valueOf(path) + "variables"))
                for (String v : jYML.getConfigurationSection(String.valueOf(path) + "variables").getKeys(false)) {
                    double d = jYML.getDouble(String.valueOf(path) + "variables." + v);
                    vars.put(v, Double.valueOf(d));
                }
            HashMap<String, Double> vars_lvl = new HashMap<>();
            for (String v : jYML.getConfigurationSection(String.valueOf(path) + "variables-per-lvl").getKeys(false)) {
                double d = jYML.getDouble(String.valueOf(path) + "variables-per-lvl." + v);
                vars_lvl.put(v, Double.valueOf(d));
            }
            HashMap<String, String> vars_att = new HashMap<>();
            for (String v : jYML.getConfigurationSection(String.valueOf(path) + "variables-types").getKeys(false)) {
                String att = jYML.getString(String.valueOf(path) + "variables-types." + v).toUpperCase();
                vars_att.put(v, att);
            }
            HashMap<String, Boolean> vars_perc = new HashMap<>();
            for (String v : jYML.getConfigurationSection(String.valueOf(path) + "variables-percentage").getKeys(false)) {
                boolean b = jYML.getBoolean(String.valueOf(path) + "variables-percentage." + v);
                vars_perc.put(v, Boolean.valueOf(b));
            }
            Gem gem = new Gem(
                    id,
                    path,
                    (FileConfiguration) jYML,

                    ild,

                    vars,
                    vars_lvl,
                    vars_att,
                    vars_perc);
            this.items.put(gem.getId(), gem);
        }
        this.gemsCfg.save();
    }

    private HashMap<String, String> getGemValues(ItemStack item) {
        HashMap<String, String> map = new HashMap<>();
        if (item == null)
            return map;
        NBTItem nbt = new NBTItem(item);
        for (String s : nbt.getKeys()) {
            if (s.startsWith("DIVINE_VAR_ATT_")) {
                String[] s2 = nbt.getString(s).split("@");
                String type = s2[0];
                boolean perc = Boolean.valueOf(s2[1]).booleanValue();
                double val = Double.parseDouble(s2[2]);
                String mix = (new StringBuilder(String.valueOf(val))).toString();
                if (perc)
                    mix = String.valueOf(mix) + "%";
                if (map.containsKey(type))
                    mix = String.valueOf(map.get(type)) + "/" + mix;
                map.put(type, mix);
            }
        }
        return map;
    }

    public List<String> getFilledSocketKeys(ItemStack item) {
        List<String> list = new ArrayList<>();
        if (item == null || item.getType() == Material.AIR)
            return list;
        NBTItem nbt = new NBTItem(item);
        for (int i = 0; i < getItemGemsAmount(item); i++) {
            for (String s : nbt.getKeys()) {
                if (s.startsWith("GEM_" + i)) {
                    String[] a1 = s.split(":::")[0].split("_");
                    String id = a1[2];
                    int lvl = Integer.parseInt(s.split(":::")[1]);
                    list.add(String.valueOf(id) + ":" + lvl);
                }
            }
        }
        return list;
    }

    public ItemStack extractSocket(ItemStack item, int num) {
        List<String> has = getFilledSocketKeys(item);
        has.remove(num);
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        for (int i = 0; i < lore.size(); i++) {
            String s = lore.get(i);
            if (s.startsWith(this.ss.getFilledSlot()))
                lore.set(i, this.ss.getEmptySlot());
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        NBTItem item2 = new NBTItem(item);
        for (String ss : item2.getKeys()) {
            if (ss.startsWith("GEM_"))
                item2.removeKey(ss);
        }
        item = item2.getItem();
        for (String en : has) {
            String id = en.split(":")[0];
            int lvl = Integer.parseInt(en.split(":")[1]);
            ItemStack gem = ItemAPI.getItemByModule(type(), id, lvl, -1);
            item = insertSocket(item, gem);
        }
        item = this.plugin.getNMS().fixNBT(item);
        return item;
    }

    public ItemStack insertSocket(ItemStack item2, ItemStack src) {
        ItemStack target = new ItemStack(item2);
        String value = "";
        for (String at : getGemValues(src).keySet()) {
            String s = getGemValues(src).get(at);
            String s2 = String.valueOf(at) + "@" + s;
            if (!value.isEmpty())
                value = String.valueOf(value) + "|";
            value = String.valueOf(value) + s2;
        }
        int hasGems = getItemGemsAmount(target);
        NBTItem gem2 = new NBTItem(src);
        String name = gem2.getString("DIVINE_GEM_ILD");
        String fill = String.valueOf(this.ss.getFilledSlot()) + name;
        ItemMeta meta = target.getItemMeta();
        List<String> lore = meta.getLore();
        lore.set(getEmptySlotIndex(target), fill);
        meta.setLore(lore);
        target.setItemMeta(meta);
        NBTItem nnn = new NBTItem(target);
        String id = getItemId(src);
        nnn.setString("GEM_" + hasGems + "_" + id + ":::" + getLevel(src), value);
        target = nnn.getItem();
        target = this.plugin.getNMS().fixNBT(target);
        return target;
    }

    public Map<ItemStat, Double> getItemGemStats(ItemStack item, boolean percent) {
        Map<ItemStat, Double> map = new HashMap<>();
        if (item == null || item.getType() == Material.AIR)
            return map;
        byte b;
        int i;
        ItemStat[] arrayOfItemStat;
        for (i = (arrayOfItemStat = ItemStat.values()).length, b = 0; b < i; ) {
            ItemStat is = arrayOfItemStat[b];
            double d = getItemGemStat(item, is, percent);
            map.put(is, Double.valueOf(d));
            b++;
        }
        return map;
    }

    public double getItemGemStat(ItemStack item, ItemStat type, boolean percent) {
        double val = 0.0D;
        if (item == null || item.getType() == Material.AIR)
            return val;
        NBTItem nbt = new NBTItem(item);
        for (String s : nbt.getKeys()) {
            if (s.startsWith("GEM_")) {
                String key = nbt.getString(s);
                String[] a1 = key.split("\\|");
                byte b;
                int i;
                String[] arrayOfString1;
                for (i = (arrayOfString1 = a1).length, b = 0; b < i; ) {
                    String s1 = arrayOfString1[b];
                    try {
                        ItemStat type2 = ItemStat.valueOf(s1.split("@")[0]);
                        if (type2 == type) {
                            String[] a2 = s1.split("@")[1].split("\\/");
                            byte b1;
                            int j;
                            String[] arrayOfString2;
                            for (j = (arrayOfString2 = a2).length, b1 = 0; b1 < j; ) {
                                String s2 = arrayOfString2[b1];
                                double d = 0.0D;
                                if (s2.endsWith("%")) {
                                    if (percent) {
                                        d = Double.parseDouble(s2.replace("%", ""));
                                    } else {
                                        continue;
                                    }
                                } else {
                                    if (percent)
                                        continue;
                                    d = Double.parseDouble(s2);
                                }
                                val += d;
                                b1++;
                            }
                        }
                    } catch (IllegalArgumentException illegalArgumentException) {
                    }
                    b++;
                }
            }
        }
        return val;
    }

    public Map<DamageType, Double> getItemGemDamages(ItemStack item, boolean percent) {
        Map<DamageType, Double> map = new HashMap<>();
        if (item == null || item.getType() == Material.AIR)
            return map;
        for (DamageType dt : Config.getDamageTypes().values()) {
            double d = getItemGemDamage(item, dt.getId(), percent);
            map.put(dt, Double.valueOf(d));
        }
        return map;
    }

    public double getItemGemDamage(ItemStack item, String type, boolean percent) {
        double val = 0.0D;
        if (item == null || item.getType() == Material.AIR)
            return val;
        NBTItem nbt = new NBTItem(item);
        for (String s : nbt.getKeys()) {
            if (s.startsWith("GEM_")) {
                String key = nbt.getString(s);
                String[] a1 = key.split("\\|");
                byte b;
                int i;
                String[] arrayOfString1;
                for (i = (arrayOfString1 = a1).length, b = 0; b < i; ) {
                    String s1 = arrayOfString1[b];
                    String zz = s1.split("@")[0];
                    if (zz.endsWith("_DAMAGE_TYPE")) {
                        zz = zz.replace("_DAMAGE_TYPE", "");
                        if (zz.equalsIgnoreCase(type)) {
                            DamageType type2 = Config.getDamageTypeById(zz);
                            if (type2 != null) {
                                String[] a2 = s1.split("@")[1].split("\\/");
                                byte b1;
                                int j;
                                String[] arrayOfString2;
                                for (j = (arrayOfString2 = a2).length, b1 = 0; b1 < j; ) {
                                    String s2 = arrayOfString2[b1];
                                    double d = 0.0D;
                                    if (s2.endsWith("%")) {
                                        if (percent) {
                                            d = Double.parseDouble(s2.replace("%", ""));
                                        } else {
                                            continue;
                                        }
                                    } else {
                                        if (percent)
                                            continue;
                                        d = Double.parseDouble(s2);
                                    }
                                    val += d;
                                    b1++;
                                }
                            }
                        }
                    }
                    b++;
                }
            }
        }
        return val;
    }

    public Map<ArmorType, Double> getItemGemDefenses(ItemStack item, boolean percent) {
        Map<ArmorType, Double> map = new HashMap<>();
        if (item == null || item.getType() == Material.AIR)
            return map;
        for (ArmorType at : Config.getArmorTypes().values()) {
            double d = getItemGemDefense(item, at.getId(), percent);
            map.put(at, Double.valueOf(d));
        }
        return map;
    }

    public double getItemGemDefense(ItemStack item, String type, boolean percent) {
        double val = 0.0D;
        if (item == null || item.getType() == Material.AIR)
            return val;
        NBTItem nbt = new NBTItem(item);
        for (String s : nbt.getKeys()) {
            if (s.startsWith("GEM_")) {
                String key = nbt.getString(s);
                String[] a1 = key.split("\\|");
                byte b;
                int i;
                String[] arrayOfString1;
                for (i = (arrayOfString1 = a1).length, b = 0; b < i; ) {
                    String s1 = arrayOfString1[b];
                    String zz = s1.split("@")[0];
                    if (zz.endsWith("_ARMOR_TYPE") || zz.endsWith("_DEFENSE_TYPE")) {
                        zz = zz.replace("_ARMOR_TYPE", "").replace("_DEFENSE_TYPE", "");
                        if (zz.equalsIgnoreCase(type)) {
                            ArmorType type2 = Config.getArmorTypeById(zz);
                            if (type2 != null) {
                                String[] a2 = s1.split("@")[1].split("\\/");
                                byte b1;
                                int j;
                                String[] arrayOfString2;
                                for (j = (arrayOfString2 = a2).length, b1 = 0; b1 < j; ) {
                                    String s2 = arrayOfString2[b1];
                                    double d = 0.0D;
                                    if (s2.endsWith("%")) {
                                        if (percent) {
                                            d = Double.parseDouble(s2.replace("%", ""));
                                        } else {
                                            continue;
                                        }
                                    } else {
                                        if (percent)
                                            continue;
                                        d = Double.parseDouble(s2);
                                    }
                                    val += d;
                                    b1++;
                                }
                            }
                        }
                    }
                    b++;
                }
            }
        }
        return val;
    }

    public int getItemGemsAmount(ItemStack item) {
        NBTItem item2 = new NBTItem(item);
        int x = 0;
        for (String s : item2.getKeys()) {
            if (s.startsWith("GEM_"))
                x++;
        }
        return x;
    }

    public boolean hasGem(ItemStack item, String id) {
        NBTItem nbt = new NBTItem(item);
        for (String s : nbt.getKeys()) {
            if (s.startsWith("GEM_")) {
                String[] key = nbt.getString(s).split("_");
                if (key.length != 3)
                    return false;
                if (key[2].equalsIgnoreCase(id))
                    return true;
            }
        }
        return false;
    }

    public GemSettings getSettings() {
        return (GemSettings) this.ss;
    }

    @EventHandler
    public void onInvClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player))
            return;
        ItemStack gem = e.getCursor();
        if (!isItemOfThisModule(gem))
            return;
        ItemStack target = e.getCurrentItem();
        if (target == null || !target.hasItemMeta() || !target.getItemMeta().hasLore())
            return;
        if (e.getInventory().getType() != InventoryType.CRAFTING)
            return;
        if (e.getSlotType() == InventoryType.SlotType.CRAFTING)
            return;
        if (e.getSlotType() == InventoryType.SlotType.ARMOR || e.getSlot() == 40)
            return;
        Player p = (Player) e.getWhoClicked();
        String id = getItemId(gem);
        Gem gem3 = (Gem) getItemById(id, Gem.class);
        if (gem3 == null) {
            out((Entity) p, Lang.Other_Internal.toMsg());
            return;
        }
        if (!getSettings().allowMultSameGems() && hasGem(target, id)) {
            out((Entity) p, Lang.Gems_Enchanting_MultipleNotAllowed.toMsg().replace("%gem%", id));
            return;
        }
        if (!gem3.isValidType(target)) {
            out((Entity) p, Lang.Gems_Enchanting_InvalidType.toMsg());
            return;
        }
        if (!isInLevelRange(target, gem)) {
            out((Entity) p, Lang.Gems_Enchanting_BadLevel.toMsg());
            return;
        }
        if (target.getItemMeta().getLore().contains(this.ss.getEmptySlot())) {
            e.setCursor(null);
            startSocketing(p, target, gem);
            e.setCancelled(true);
        } else {
            out((Entity) p, Lang.Gems_Enchanting_NoSlots.toMsg());
        }
    }

    private String replaceVars(String to, Map<String, Double> vars) {
        for (String v : vars.keySet()) {
            String r = "%var_" + v + "%";
            String r2 = String.valueOf(vars.get(v));
            to = to.replace(r, r2);
        }
        return to;
    }

    public class Gem extends SocketItem {
        private String ild;

        private HashMap<String, Double> vars;

        private HashMap<String, Double> vars_lvl;

        private HashMap<String, String> vars_att;

        private HashMap<String, Boolean> vars_perc;

        public Gem(String id, String path, FileConfiguration cfg, String ild, HashMap<String, Double> vars, HashMap<String, Double> vars_lvl, HashMap<String, String> vars_att, HashMap<String, Boolean> vars_perc) {
            super(id, path, cfg, GemManager.this.type());
            setItemLoreDispaly(ild);
            setVariables(vars);
            setVariablesLvl(vars_lvl);
            setVariablesAttributes(vars_att);
            setVariablesPercentage(vars_perc);
        }

        public String getItemLoreDisplay() {
            return this.ild;
        }

        public void setItemLoreDispaly(String ild) {
            this.ild = ild;
        }

        public HashMap<String, Double> getVariables() {
            return this.vars;
        }

        public void setVariables(HashMap<String, Double> vars) {
            this.vars = vars;
        }

        public HashMap<String, Double> getVariablesLvl() {
            return this.vars_lvl;
        }

        public void setVariablesLvl(HashMap<String, Double> vars_lvl) {
            this.vars_lvl = vars_lvl;
        }

        public HashMap<String, String> getVariablesAttributes() {
            return this.vars_att;
        }

        public void setVariablesAttributes(HashMap<String, String> vars_att) {
            this.vars_att = vars_att;
        }

        public HashMap<String, Boolean> getVariablesPercentage() {
            return this.vars_perc;
        }

        public void setVariablesPercentage(HashMap<String, Boolean> vars_perc) {
            this.vars_perc = vars_perc;
        }

        protected ItemStack build(int lvl, int suc) {
            ItemStack item = super.build(lvl, suc);
            if (item.getType() == Material.AIR)
                return item;
            ItemMeta meta = item.getItemMeta();
            List<String> lore = new ArrayList<>();
            if (meta.hasLore())
                lore = meta.getLore();
            Map<String, Double> vars3 = new HashMap<>(getVariables());
            if (lvl > 1) {
                int lvl2 = lvl - 1;
                for (String s : getVariablesLvl().keySet()) {
                    double d = Utils.round3(((Double) getVariablesLvl().get(s)).doubleValue() * lvl2);
                    if (vars3.containsKey(s))
                        d = Utils.round3(d + ((Double) vars3.get(s)).doubleValue());
                    vars3.put(s, Double.valueOf(d));
                }
            }
            String ild = GemManager.this.replaceVars(getItemLoreDisplay(), vars3)
                    .replace("%level%", String.valueOf(lvl))
                    .replace("%rlevel%", Utils.IntegerToRomanNumeral(lvl));
            String display = replacePlaceholders(GemManager.this.ss.getDisplay()
                    .replace("%item_name%", meta.getDisplayName()), lvl);
            for (int i = 0; i < lore.size(); i++) {
                String s = lore.get(i);
                lore.set(i, GemManager.this.replaceVars(s, vars3));
            }
            List<String> lore3 = new ArrayList<>();
            for (String s : GemManager.this.ss.getLore()) {
                if (s.equals("%item_lore%")) {
                    for (String s2 : lore)
                        lore3.add(s2);
                    continue;
                }
                s = replacePlaceholders(s, lvl, suc);
                lore3.add(ChatColor.translateAlternateColorCodes('&', s));
            }
            meta.setDisplayName(GemManager.this.replaceVars(display, vars3));
            meta.setLore(lore3);
            item.setItemMeta(meta);
            NBTItem nbt = new NBTItem(item);
            for (String s : getVariablesAttributes().keySet()) {
                String a = getVariablesAttributes().get(s);
                boolean b = ((Boolean) getVariablesPercentage().get(s)).booleanValue();
                nbt.setString("DIVINE_VAR_ATT_" + s, String.valueOf(a) + "@" + b + "@" + vars3.get(s));
            }
            nbt.setString("DIVINE_GEM_ILD", ild);
            ItemStack item2 = nbt.getItem();
            return item2;
        }
    }

    public class GemSettings extends SocketSettings {
        private boolean same;

        public GemSettings(boolean same, String display, List<String> lore, DestroyType destroy, boolean eff_use, String eff_de_value, String eff_suc_value, boolean sound_use, Sound sound_de_value, Sound sound_suc_value, String header, String empty_slot, String filled_slot) {
            super(display, lore, destroy, eff_use, eff_de_value, eff_suc_value, sound_use, sound_de_value, sound_suc_value, header, empty_slot, filled_slot);
            setAllowMultSameGems(same);
        }

        public boolean allowMultSameGems() {
            return this.same;
        }

        public void setAllowMultSameGems(boolean same) {
            this.same = same;
        }
    }
}
