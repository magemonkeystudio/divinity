package su.nightexpress.quantumrpg.modules.sets;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.api.EntityAPI;
import su.nightexpress.quantumrpg.config.JYML;
import su.nightexpress.quantumrpg.config.MyConfig;
import su.nightexpress.quantumrpg.modules.EModule;
import su.nightexpress.quantumrpg.modules.QModule;
import su.nightexpress.quantumrpg.modules.cmds.MExecutor;
import su.nightexpress.quantumrpg.modules.refine.RefineManager;
import su.nightexpress.quantumrpg.types.BonusType;
import su.nightexpress.quantumrpg.utils.Utils;
import su.nightexpress.quantumrpg.utils.logs.LogType;

import java.util.*;

public class SetManager extends QModule {
    private static final String SP_HEAD = "§9§9§9§2§2§2§9§9§9§r";
    private MyConfig setsCfg;
    private int taskId;
    private SetSettings ss;

    private Map<String, ItemSet> sets;

    public SetManager(QuantumRPG plugin, boolean enabled, MExecutor exe) {
        super(plugin, enabled, exe);
    }

    public EModule type() {
        return EModule.SETS;
    }

    public String name() {
        return "Sets";
    }

    public String version() {
        return "1.0";
    }

    public boolean isResolvable() {
        return false;
    }

    public void updateCfg() {
    }

    public void setup() {
        this.sets = new HashMap<>();
        this.setsCfg = new MyConfig((JavaPlugin) this.plugin, "/modules/" + getId(), "sets.yml");
        setupMain();
        startTask();
    }

    public void shutdown() {
        stopTask();
        this.sets = null;
        this.setsCfg = null;
        this.ss = null;
    }

    private void setupMain() {
        setupSettings();
        setupSets();
    }

    private void setupSettings() {
        JYML cfg = this.cfg.getConfig();
        String path = "general.";
        String i_have = ChatColor.translateAlternateColorCodes('&', cfg.getString(String.valueOf(path) + "item-have"));
        String i_miss = ChatColor.translateAlternateColorCodes('&', cfg.getString(String.valueOf(path) + "item-miss"));
        List<String> lore = new ArrayList<>();
        for (String s : cfg.getStringList(String.valueOf(path) + "lore"))
            lore.add(ChatColor.translateAlternateColorCodes('&', s));
        this.ss = new SetSettings(i_have, i_miss, lore);
    }

    private void setupSets() {
        JYML cfg = this.setsCfg.getConfig();
        if (!cfg.isConfigurationSection("sets"))
            return;
        for (String id : cfg.getSection("sets")) {
            String path = "sets." + id + ".";
            String name = ChatColor.translateAlternateColorCodes('&', cfg.getString(String.valueOf(path) + "name"));
            String prefix = ChatColor.translateAlternateColorCodes('&', cfg.getString(String.valueOf(path) + "prefix"));
            String suffix = ChatColor.translateAlternateColorCodes('&', cfg.getString(String.valueOf(path) + "suffix"));
            String color_h = ChatColor.translateAlternateColorCodes('&', cfg.getString(String.valueOf(path) + "color.have"));
            String color_m = ChatColor.translateAlternateColorCodes('&', cfg.getString(String.valueOf(path) + "color.miss"));
            Map<PartType, SetPart> parts = new LinkedHashMap<>();
            for (String type : cfg.getSection(String.valueOf(path) + "parts")) {
                PartType pt;
                try {
                    pt = PartType.valueOf(type.toUpperCase());
                } catch (IllegalArgumentException ex) {
                    log("Invalid Part type: &f" + type + " &7in set: &f" + id, LogType.ERROR);
                    continue;
                }
                String path2 = String.valueOf(path) + "parts." + type + ".";
                boolean e = cfg.getBoolean(String.valueOf(path2) + "enabled");
                if (!e)
                    continue;
                Material mat = Material.getMaterial(cfg.getString(String.valueOf(path2) + "material").toUpperCase());
                if (mat == null) {
                    log("Invalid material for part of &f" + id + " &7set!", LogType.ERROR);
                    continue;
                }
                String p_name = ChatColor.translateAlternateColorCodes('&', cfg.getString(String.valueOf(path2) + "name"));
                p_name = p_name.replace("%suffix%", suffix).replace("%prefix%", prefix).trim().replaceAll("\\s+", " ");
                SetPart sp = new SetPart(pt, mat, p_name);
                parts.put(pt, sp);
            }
            TreeMap<Integer, SetPartBonus> parts_eff = new TreeMap<>();
            for (String o3 : cfg.getSection(String.valueOf(path) + "effects.parts")) {
                String path2 = String.valueOf(path) + "effects.parts." + o3 + ".";
                int amount = Integer.parseInt(o3.toString());
                List<String> e_lore = new ArrayList<>();
                for (String s3 : cfg.getStringList(String.valueOf(path2) + "lore"))
                    e_lore.add(ChatColor.translateAlternateColorCodes('&', s3));
                List<String> eff = cfg.getStringList(String.valueOf(path2) + "potion-effects");
                Map<PotionEffectType, Integer> potions = new HashMap<>();
                for (String line : eff) {
                    String[] pp = line.split(":");
                    PotionEffectType pet = PotionEffectType.getByName(pp[0].toUpperCase());
                    if (pet == null) {
                        log("Invalid potion effect &f" + pp[0] + " &7for set &f" + id, LogType.ERROR);
                        continue;
                    }
                    int level = 1;
                    if (pp.length == 2)
                        try {
                            level = Integer.parseInt(pp[1]);
                        } catch (IllegalArgumentException illegalArgumentException) {
                        }
                    potions.put(pet, Integer.valueOf(level));
                }
                Map<BonusType, List<SetBonus>> bonuses = new HashMap<>();
                for (String o4 : cfg.getSection(String.valueOf(path2) + "bonuses")) {
                    BonusType btype;
                    List<SetBonus> b_list;
                    String path3 = String.valueOf(path2) + "bonuses." + o4 + ".";
                    String bon = cfg.getString(String.valueOf(path3) + "type").toUpperCase();
                    try {
                        btype = BonusType.valueOf(bon);
                    } catch (IllegalArgumentException ex) {
                        log("Invalid Bonus type: &f" + bon + " &7in set: &f" + id, LogType.ERROR);
                        continue;
                    }
                    if (bonuses.containsKey(btype)) {
                        b_list = bonuses.get(btype);
                    } else {
                        b_list = new ArrayList<>();
                    }
                    String type_name = cfg.getString(String.valueOf(path3) + "name");
                    String value = cfg.getString(String.valueOf(path3) + "value");
                    SetBonus ea = new SetBonus(btype, type_name, value);
                    b_list.add(ea);
                    bonuses.put(btype, b_list);
                }
                SetPartBonus spe = new SetPartBonus(amount, e_lore, potions, bonuses);
                parts_eff.put(Integer.valueOf(amount), spe);
            }
            ItemSet set = new ItemSet(id, name, prefix, suffix, color_h, color_m, parts, parts_eff);
            this.sets.put(set.getId(), set);
        }
    }

    private void startTask() {
        this.taskId = this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask((Plugin) this.plugin, new Runnable() {
            public void run() {
                for (Player p : SetManager.this.plugin.getServer().getOnlinePlayers())
                    SetManager.this.applySetPotions(p);
            }
        }, 10L, 80L);
    }

    private void stopTask() {
        this.plugin.getServer().getScheduler().cancelTask(this.taskId);
    }

    private void applySetPotions(Player p) {
        for (ItemSet set : getSets()) {
            for (SetPartBonus spe : set.getPartsEffects()) {
                int need = spe.getPartsAmount();
                int have = getPartsOf((LivingEntity) p, set);
                if (have < need)
                    continue;
                spe.applyEffects(p);
            }
        }
    }

    private List<SetBonus> getActiveSetBonuses(LivingEntity p, ItemSet set, BonusType type, boolean perc) {
        List<SetBonus> list = new ArrayList<>();
        for (SetPartBonus spe : set.getPartsEffects()) {
            List<SetBonus> bonus = spe.getBonuses(type, perc);
            if (bonus.isEmpty())
                continue;
            int need = spe.getPartsAmount();
            int have = getPartsOf(p, set);
            if (have < need)
                continue;
            list.addAll(bonus);
        }
        return list;
    }

    public double getSetBonus(LivingEntity li, BonusType type, String name, boolean perc) {
        double v = 0.0D;
        for (ItemSet set : getSets())
            v += getSetBonus(li, set, type, name, perc);
        return v;
    }

    public double getSetBonus(LivingEntity li, ItemSet set, BonusType type, String name, boolean perc) {
        double v = 0.0D;
        for (SetBonus e : getActiveSetBonuses(li, set, type, perc)) {
            if (e.getTypeName().equalsIgnoreCase(name))
                v += e.getValue();
        }
        return v;
    }

    private String getClearName(String name) {
        if (!EModule.REFINE.isEnabled())
            return name;
        RefineManager rf = (RefineManager) this.plugin.getModule(RefineManager.class);
        return rf.getNameWithoutLevel(name);
    }

    public boolean isItemOfSet(ItemStack item) {
        return (getItemSet(item) != null);
    }

    public ItemSet getItemSet(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName() || !item.getItemMeta().hasLore())
            return null;
        ItemMeta meta = item.getItemMeta();
        String item_name = meta.getDisplayName();
        item_name = getClearName(item_name).trim();
        PartType pt = getItemPartType(item);
        for (ItemSet s : getSets()) {
            if (!s.isPartOfSet(item))
                continue;
            String set_name = s.getPart(pt).getName();
            if (item_name.equalsIgnoreCase(set_name))
                return s;
        }
        return null;
    }

    public ItemStack replaceLore(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore())
            return item;
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        for (String s : lore) {
            if (s.equalsIgnoreCase("%SET%")) {
                int pos = meta.getLore().indexOf(s);
                lore.remove(pos);
                ItemSet set = getItemSet(item);
                if (set == null) {
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                    return item;
                }
                for (String s2 : this.ss.getLore()) {
                    if (s2.contains("%parts%")) {
                        for (SetPart sp : set.getParts()) {
                            String name = sp.getName();
                            String name2 = ChatColor.stripColor(name);
                            String name3 = this.ss.getItemMissStr().replace("%c%", set.getColorMiss()).replace("%name%", name2);
                            pos = addToLore(lore, pos, "§9§9§9§2§2§2§9§9§9§r" + name3);
                        }
                        continue;
                    }
                    if (s2.contains("%effects%")) {
                        for (SetPartBonus spe : set.getPartsEffects()) {
                            for (String s3 : spe.getLore())
                                pos = addToLore(lore, pos, "§9§9§9§2§2§2§9§9§9§r" + s3.replace("%c%", set.getColorMiss()));
                        }
                        continue;
                    }
                    pos = addToLore(lore, pos, "§9§9§9§2§2§2§9§9§9§r" + s2.replace("%set%", set.getName()));
                }
                meta.setLore(lore);
                item.setItemMeta(meta);
                break;
            }
        }
        return item;
    }

    private int addToLore(List<String> lore, int pos, String value) {
        if (pos >= lore.size()) {
            lore.add(value);
        } else {
            lore.add(pos, value);
        }
        return pos + 1;
    }

    private void updateSets(Player p) {
        List<ItemSet> done = new ArrayList<>();
        int i = 0;
        byte b;
        int j;
        ItemStack[] arrayOfItemStack;
        for (j = (arrayOfItemStack = EntityAPI.getEquipment((LivingEntity) p, false)).length, b = 0; b < j; ) {
            ItemStack item1 = arrayOfItemStack[b];
            ItemStack item = this.plugin.getNMS().fixNBT(item1);
            ItemSet set = getItemSet(item);
            if (set != null && !done.contains(set)) {
                item = this.plugin.getNMS().fixNBT(p, item);
                done.add(set);
            }
            item = updateLore(p, item, false);
            if (!item1.isSimilar(item))
                if (i == 0) {
                    p.getInventory().setItemInMainHand(item);
                } else if (i == 1) {
                    p.getInventory().setItemInOffHand(item);
                } else if (i == 2) {
                    p.getInventory().setBoots(item);
                } else if (i == 3) {
                    p.getInventory().setLeggings(item);
                } else if (i == 4) {
                    p.getInventory().setChestplate(item);
                } else if (i == 5) {
                    p.getInventory().setHelmet(item);
                }
            i++;
            b++;
        }
        p.updateInventory();
    }

    public ItemStack updateLore(Player p, ItemStack item, boolean reset) {
        ItemSet set = getItemSet(item);
        if (set == null)
            return item;
        if (reset)
            item = this.plugin.getNMS().fixNBT(item);
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        int pos = -1;
        for (String line : lore) {
            if (line.startsWith("§9§9§9§2§2§2§9§9§9§r")) {
                if (pos < 0)
                    pos = lore.indexOf(line);
                lore.remove(line);
            }
        }
        if (pos < 0)
            return item;
        for (String s2 : this.ss.getLore()) {
            if (s2.contains("%parts%")) {
                for (SetPart sp : set.getParts()) {
                    String color, valid, name = sp.getName();
                    if (!reset && hasSetItem((LivingEntity) p, name)) {
                        valid = this.ss.getItemHaveStr();
                        color = set.getColorHave();
                    } else {
                        valid = this.ss.getItemMissStr();
                        color = set.getColorMiss();
                    }
                    String name3 = valid.replace("%c%", color).replace("%name%", ChatColor.stripColor(name));
                    pos = addToLore(lore, pos, "§9§9§9§2§2§2§9§9§9§r" + name3);
                }
                continue;
            }
            if (s2.contains("%effects%")) {
                for (SetPartBonus spe : set.getPartsEffects()) {
                    String color;
                    int a = spe.getPartsAmount();
                    if (!reset && getPartsOf((LivingEntity) p, set) >= a) {
                        color = set.getColorHave();
                    } else {
                        color = set.getColorMiss();
                    }
                    List<String> e_lore = spe.getLore();
                    for (String s3 : e_lore)
                        pos = addToLore(lore, pos, "§9§9§9§2§2§2§9§9§9§r" + s3.replace("%c%", color));
                }
                continue;
            }
            pos = addToLore(lore, pos, "§9§9§9§2§2§2§9§9§9§r" + s2.replace("%set%", set.getName()));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public int getPartsOf(LivingEntity p, ItemSet s) {
        int i = 0;
        byte b;
        int j;
        ItemStack[] arrayOfItemStack;
        for (j = (arrayOfItemStack = EntityAPI.getEquipment(p, false)).length, b = 0; b < j; ) {
            ItemStack item = arrayOfItemStack[b];
            ItemSet s2 = getItemSet(item);
            if (s2 != null && s2.getId().equalsIgnoreCase(s.getId()))
                i++;
            b++;
        }
        return i;
    }

    public boolean hasSetItem(LivingEntity p, String name) {
        name = getClearName(name);
        byte b;
        int i;
        ItemStack[] arrayOfItemStack;
        for (i = (arrayOfItemStack = EntityAPI.getEquipment(p, false)).length, b = 0; b < i; ) {
            ItemStack itemStack = arrayOfItemStack[b];
            if (itemStack != null && itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName() && itemStack.getItemMeta().hasLore()) {
                ItemMeta meta = itemStack.getItemMeta();
                String mname = getClearName(meta.getDisplayName());
                if (mname.equalsIgnoreCase(name))
                    return true;
            }
            b++;
        }
        return false;
    }

    public PartType getItemPartType(ItemStack item) {
        String name = item.getType().name();
        if (name.endsWith("_CHESTPLATE") || name.equals("ELYTRA"))
            return PartType.CHESTPLATE;
        if (name.endsWith("_LEGGINGS"))
            return PartType.LEGGINGS;
        if (name.endsWith("_BOOTS"))
            return PartType.BOOTS;
        if (name.endsWith("_HELMET") || name.contains("SKULL") || name.contains("HEAD"))
            return PartType.HELMET;
        if (name.equals("SHIELD"))
            return PartType.OFF_HAND;
        return PartType.MAIN_HAND;
    }

    public Collection<ItemSet> getSets() {
        if (!isActive())
            return new ArrayList<>();
        return this.sets.values();
    }

    public ItemSet getSetById(String id) {
        if (id.equalsIgnoreCase("random"))
            return (new ArrayList<>(getSets())).get(getSets().size() - 1);
        return this.sets.get(id.toLowerCase());
    }

    public List<String> getSetNames() {
        return new ArrayList<>(this.sets.keySet());
    }

    public SetSettings getSettings() {
        return this.ss;
    }

    @EventHandler(ignoreCancelled = true)
    public void onSetUpdateClick(InventoryClickEvent e) {
        if (e.getInventory().getType() != InventoryType.CRAFTING)
            return;
        if (e.getSlotType() == InventoryType.SlotType.CRAFTING)
            return;
        final Player p = (Player) e.getWhoClicked();
        if (p.getGameMode() == GameMode.CREATIVE)
            return;
        boolean b = false;
        ItemStack target = e.getCurrentItem();
        if (isItemOfSet(target)) {
            e.setCurrentItem(updateLore((Player) null, target, true));
            if (e.getSlotType() == InventoryType.SlotType.ARMOR || e.isShiftClick())
                b = true;
        }
        if (e.getSlotType() == InventoryType.SlotType.ARMOR) {
            ItemStack cur = e.getCursor();
            if (isItemOfSet(cur))
                b = true;
        }
        if (b)
            (new BukkitRunnable() {
                public void run() {
                    SetManager.this.updateSets(p);
                }
            }).runTaskLater((Plugin) this.plugin, 1L);
    }

    @EventHandler(ignoreCancelled = true)
    public void onSetUpdateSpawn(ItemSpawnEvent e) {
        Item item = e.getEntity();
        ItemStack stack = item.getItemStack();
        stack = replaceLore(stack);
        updateLore((Player) null, stack, true);
        item.setItemStack(stack);
    }

    public enum PartType {
        HELMET, CHESTPLATE, LEGGINGS, BOOTS, MAIN_HAND, OFF_HAND;
    }

    public class ItemSet {
        private String id;

        private String name;

        private String prefix;

        private String suffix;

        private String color_have;

        private String color_miss;

        private Map<SetManager.PartType, SetManager.SetPart> parts;

        private TreeMap<Integer, SetManager.SetPartBonus> parts_eff;

        public ItemSet(String id, String name, String prefix, String suffix, String color_have, String color_miss, Map<SetManager.PartType, SetManager.SetPart> parts, TreeMap<Integer, SetManager.SetPartBonus> parts_eff) {
            this.id = id.toLowerCase();
            setName(name);
            setPrefix(prefix);
            setSuffix(suffix);
            setColorHave(color_have);
            setColorMiss(color_miss);
            setParts(parts);
            setPartsEffects(parts_eff);
        }

        public String getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPrefix() {
            return this.prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public String getSuffix() {
            return this.suffix;
        }

        public void setSuffix(String suffix) {
            this.suffix = suffix;
        }

        public String getColorHave() {
            return this.color_have;
        }

        public void setColorHave(String color_have) {
            this.color_have = color_have;
        }

        public String getColorMiss() {
            return this.color_miss;
        }

        public void setColorMiss(String color_miss) {
            this.color_miss = color_miss;
        }

        public Collection<SetManager.SetPart> getParts() {
            return this.parts.values();
        }

        public void setParts(Map<SetManager.PartType, SetManager.SetPart> parts) {
            this.parts = parts;
        }

        public SetManager.SetPart getPart(SetManager.PartType type) {
            return this.parts.get(type);
        }

        public Collection<SetManager.SetPartBonus> getPartsEffects() {
            return this.parts_eff.values();
        }

        public void setPartsEffects(TreeMap<Integer, SetManager.SetPartBonus> parts_eff) {
            this.parts_eff = parts_eff;
        }

        public boolean isPartOfSet(ItemStack item) {
            SetManager.PartType pt = SetManager.this.getItemPartType(item);
            if (!this.parts.containsKey(pt))
                return false;
            SetManager.SetPart sp = this.parts.get(pt);
            if (sp.getMaterial() != item.getType())
                return false;
            return true;
        }

        public ItemStack create(String item) {
            SetManager.PartType type;
            if (item.equalsIgnoreCase("random")) {
                List<SetManager.PartType> list = new ArrayList<>(this.parts.keySet());
                type = list.get(Utils.r.nextInt(list.size()));
            } else {
                type = SetManager.PartType.valueOf(item.toUpperCase());
            }
            SetManager.SetPart sp = this.parts.get(type);
            ItemStack i = new ItemStack(sp.getMaterial());
            ItemMeta meta = i.getItemMeta();
            String name2 = sp.getName();
            meta.setDisplayName(name2);
            meta.setLore(Arrays.asList(new String[]{"%SET%"}));
            i.setItemMeta(meta);
            i = SetManager.this.replaceLore(i);
            return i;
        }
    }

    public class SetPart {
        private SetManager.PartType type;

        private Material mat;

        private String name;

        public SetPart(SetManager.PartType type, Material mat, String name) {
            this.type = type;
            this.mat = mat;
            setName(name);
        }

        public SetManager.PartType getType() {
            return this.type;
        }

        public Material getMaterial() {
            return this.mat;
        }

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name.trim();
        }
    }

    public class SetPartBonus {
        private int amount;

        private List<String> lore;

        private Map<PotionEffectType, Integer> potions;

        private Map<BonusType, List<SetManager.SetBonus>> bonuses;

        public SetPartBonus(int amount, List<String> lore, Map<PotionEffectType, Integer> potions, Map<BonusType, List<SetManager.SetBonus>> bonuses) {
            setPartsAmount(amount);
            setLore(lore);
            this.potions = potions;
            this.bonuses = bonuses;
        }

        public int getPartsAmount() {
            return this.amount;
        }

        public void setPartsAmount(int amount) {
            this.amount = amount;
        }

        public List<String> getLore() {
            return this.lore;
        }

        public void setLore(List<String> lore) {
            this.lore = lore;
        }

        public Map<PotionEffectType, Integer> getEffects() {
            return this.potions;
        }

        public void applyEffects(Player p) {
            for (Map.Entry<PotionEffectType, Integer> e : this.potions.entrySet()) {
                PotionEffectType pet = e.getKey();
                int lvl = ((Integer) e.getValue()).intValue();
                PotionEffect pe = new PotionEffect(pet, 100, ((Integer) e.getValue()).intValue());
                if (p.hasPotionEffect(pet)) {
                    int h_lvl = p.getPotionEffect(pet).getAmplifier();
                    if (h_lvl > lvl)
                        continue;
                    p.removePotionEffect(pet);
                }
                p.addPotionEffect(pe, true);
            }
        }

        public List<SetManager.SetBonus> getBonuses() {
            List<SetManager.SetBonus> list = new ArrayList<>();
            for (List<SetManager.SetBonus> en : this.bonuses.values())
                list.addAll(en);
            return list;
        }

        public List<SetManager.SetBonus> getBonuses(BonusType type) {
            if (this.bonuses.containsKey(type))
                return this.bonuses.get(type);
            return Collections.emptyList();
        }

        public List<SetManager.SetBonus> getBonuses(BonusType type, boolean percent) {
            List<SetManager.SetBonus> list = new ArrayList<>();
            for (SetManager.SetBonus b : getBonuses(type)) {
                if (b.isPercent() == percent)
                    list.add(b);
            }
            return list;
        }
    }

    public class SetBonus {
        private BonusType type;

        private String type_name;

        private double value;

        private boolean perc;

        public SetBonus(BonusType type, String type_name, String value) {
            this.type = type;
            this.type_name = type_name.toLowerCase();
            setValue(value);
        }

        public BonusType getType() {
            return this.type;
        }

        public String getTypeName() {
            return this.type_name;
        }

        public double getValue() {
            return this.value;
        }

        public void setValue(String value) {
            if (value.contains("%")) {
                this.perc = true;
                value = value.replace("%", "");
            }
            try {
                double val = Double.parseDouble(value);
                this.value = val;
            } catch (IllegalArgumentException ex) {
                this.value = 0.0D;
            }
        }

        public boolean isPercent() {
            return this.perc;
        }
    }

    public class SetSettings {
        private String i_have;

        private String i_miss;

        private List<String> lore;

        public SetSettings(String i_have, String i_miss, List<String> lore) {
            setItemHaveStr(i_have);
            setItemMissStr(i_miss);
            setLore(lore);
        }

        public String getItemHaveStr() {
            return this.i_have;
        }

        public void setItemHaveStr(String i_have) {
            this.i_have = i_have;
        }

        public String getItemMissStr() {
            return this.i_miss;
        }

        public void setItemMissStr(String i_miss) {
            this.i_miss = i_miss;
        }

        public List<String> getLore() {
            return this.lore;
        }

        public void setLore(List<String> lore) {
            this.lore = lore;
        }
    }
}
